package br.edu.catalogo.util;

import br.edu.catalogo.exception.PersistenciaException;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Cria a tabela quando a aplicacao sobe e insere os filmes de demonstracao
 * somente na primeira execucao, evitando duplicidade a cada restart.
 */
@WebListener
public class InicializadorBanco implements ServletContextListener {

    private static final String DDL_TABELA = """
            CREATE TABLE IF NOT EXISTS filmes (
                id       BIGINT AUTO_INCREMENT PRIMARY KEY,
                titulo   VARCHAR(150) NOT NULL,
                diretor  VARCHAR(120) NOT NULL,
                ano      INT          NOT NULL,
                genero   VARCHAR(60),
                sinopse  VARCHAR(2000)
            )
            """;

    private static final String CONTAR = "SELECT COUNT(*) FROM filmes";

    private static final String INSERIR_DEMO =
            "INSERT INTO filmes (titulo, diretor, ano, genero, sinopse) VALUES (?, ?, ?, ?, ?)";

    private static final Object[][] FILMES_DEMO = {
            {"Cidade de Deus", "Fernando Meirelles", 2002, "Drama",
                    "A trajetoria de dois jovens em uma comunidade do Rio de Janeiro marcada pelo crescimento do crime organizado."},
            {"Central do Brasil", "Walter Salles", 1998, "Drama",
                    "Uma ex-professora escreve cartas na estacao de trem e acaba acompanhando um menino em busca do pai."},
            {"Tropa de Elite", "Jose Padilha", 2007, "Acao",
                    "Um capitao do BOPE procura um substituto enquanto enfrenta a rotina violenta do policiamento."}
    };

    @Override
    public void contextInitialized(ServletContextEvent evento) {
        try (Connection conexao = ConexaoFactory.obterConexao()) {
            criarTabela(conexao);
            if (tabelaVazia(conexao)) {
                inserirFilmesDemonstracao(conexao);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao preparar o banco de dados na inicializacao.", e);
        }
    }

    private void criarTabela(Connection conexao) throws SQLException {
        try (Statement comando = conexao.createStatement()) {
            comando.execute(DDL_TABELA);
        }
    }

    private boolean tabelaVazia(Connection conexao) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(CONTAR);
             ResultSet resultado = comando.executeQuery()) {
            return resultado.next() && resultado.getInt(1) == 0;
        }
    }

    private void inserirFilmesDemonstracao(Connection conexao) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement(INSERIR_DEMO)) {
            for (Object[] filme : FILMES_DEMO) {
                comando.setString(1, (String) filme[0]);
                comando.setString(2, (String) filme[1]);
                comando.setInt(3, (Integer) filme[2]);
                comando.setString(4, (String) filme[3]);
                comando.setString(5, (String) filme[4]);
                comando.addBatch();
            }
            comando.executeBatch();
        }
    }
}

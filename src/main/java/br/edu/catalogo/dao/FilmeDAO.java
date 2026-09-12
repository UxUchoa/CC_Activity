package br.edu.catalogo.dao;

import br.edu.catalogo.exception.PersistenciaException;
import br.edu.catalogo.model.Filme;
import br.edu.catalogo.util.ConexaoFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Concentra todo o acesso JDBC ao catalogo. Nenhuma outra classe do projeto
 * escreve SQL, o que elimina a duplicacao que existia na Servlet original.
 *
 * Os metodos sao sobrescreviveis para permitir um dublê em memoria nos testes
 * do FilmeService, sem precisar de banco nem de framework de mock.
 */
public class FilmeDAO {

    private static final String INSERIR =
            "INSERT INTO filmes (titulo, diretor, ano, genero, sinopse) VALUES (?, ?, ?, ?, ?)";

    private static final String LISTAR_TODOS =
            "SELECT id, titulo, diretor, ano, genero, sinopse FROM filmes ORDER BY titulo";

    private static final String BUSCAR_POR_ID =
            "SELECT id, titulo, diretor, ano, genero, sinopse FROM filmes WHERE id = ?";

    private static final String BUSCAR_POR_TITULO_OU_DIRETOR =
            "SELECT id, titulo, diretor, ano, genero, sinopse FROM filmes "
                    + "WHERE LOWER(titulo) LIKE ? OR LOWER(diretor) LIKE ? ORDER BY titulo";

    private static final String ATUALIZAR =
            "UPDATE filmes SET titulo = ?, diretor = ?, ano = ?, genero = ?, sinopse = ? WHERE id = ?";

    private static final String EXCLUIR = "DELETE FROM filmes WHERE id = ?";

    public Filme inserir(Filme filme) {
        try (Connection conexao = ConexaoFactory.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(INSERIR, Statement.RETURN_GENERATED_KEYS)) {

            preencherParametros(comando, filme);
            comando.executeUpdate();

            try (ResultSet chaves = comando.getGeneratedKeys()) {
                if (chaves.next()) {
                    filme.setId(chaves.getLong(1));
                }
            }
            return filme;
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao inserir o filme.", e);
        }
    }

    public List<Filme> listarTodos() {
        try (Connection conexao = ConexaoFactory.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(LISTAR_TODOS);
             ResultSet resultado = comando.executeQuery()) {
            return mapearLista(resultado);
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao listar os filmes.", e);
        }
    }

    public Optional<Filme> buscarPorId(Long id) {
        try (Connection conexao = ConexaoFactory.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(BUSCAR_POR_ID)) {

            comando.setLong(1, id);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next() ? Optional.of(mapear(resultado)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao buscar o filme informado.", e);
        }
    }

    public List<Filme> buscarPorTituloOuDiretor(String termo) {
        String filtro = "%" + termo.toLowerCase() + "%";
        try (Connection conexao = ConexaoFactory.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(BUSCAR_POR_TITULO_OU_DIRETOR)) {

            comando.setString(1, filtro);
            comando.setString(2, filtro);
            try (ResultSet resultado = comando.executeQuery()) {
                return mapearLista(resultado);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao pesquisar filmes.", e);
        }
    }

    public boolean atualizar(Filme filme) {
        try (Connection conexao = ConexaoFactory.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(ATUALIZAR)) {

            preencherParametros(comando, filme);
            comando.setLong(6, filme.getId());
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao atualizar o filme.", e);
        }
    }

    public boolean excluir(Long id) {
        try (Connection conexao = ConexaoFactory.obterConexao();
             PreparedStatement comando = conexao.prepareStatement(EXCLUIR)) {

            comando.setLong(1, id);
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new PersistenciaException("Falha ao excluir o filme.", e);
        }
    }

    private void preencherParametros(PreparedStatement comando, Filme filme) throws SQLException {
        comando.setString(1, filme.getTitulo());
        comando.setString(2, filme.getDiretor());
        comando.setInt(3, filme.getAno());
        comando.setString(4, filme.getGenero());
        comando.setString(5, filme.getSinopse());
    }

    private List<Filme> mapearLista(ResultSet resultado) throws SQLException {
        List<Filme> filmes = new ArrayList<>();
        while (resultado.next()) {
            filmes.add(mapear(resultado));
        }
        return filmes;
    }

    private Filme mapear(ResultSet resultado) throws SQLException {
        return new Filme(
                resultado.getLong("id"),
                resultado.getString("titulo"),
                resultado.getString("diretor"),
                resultado.getInt("ano"),
                resultado.getString("genero"),
                resultado.getString("sinopse"));
    }
}

package br.edu.catalogo.util;

import br.edu.catalogo.exception.PersistenciaException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Unico ponto do sistema que sabe como abrir uma conexao. Antes da refatoracao
 * essa logica estava repetida em varios trechos da Servlet.
 */
public final class ConexaoFactory {

    private static final String URL = "jdbc:h2:file:./data/catalogo-filmes;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1";
    private static final String USUARIO = "sa";
    private static final String SENHA = "";

    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new PersistenciaException("Driver do banco de dados nao encontrado.", e);
        }
    }

    private ConexaoFactory() {
    }

    public static Connection obterConexao() {
        try {
            return DriverManager.getConnection(URL, USUARIO, SENHA);
        } catch (SQLException e) {
            throw new PersistenciaException("Nao foi possivel conectar ao banco de dados.", e);
        }
    }
}

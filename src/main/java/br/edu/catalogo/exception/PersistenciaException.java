package br.edu.catalogo.exception;

/**
 * Envolve falhas de SQLException para que as camadas superiores nao precisem
 * conhecer detalhes de JDBC nem expo-los na interface.
 */
public class PersistenciaException extends RuntimeException {

    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}

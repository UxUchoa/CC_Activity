package br.edu.catalogo.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Reune todas as mensagens de validacao de uma operacao, para que a tela
 * consiga exibi-las de uma vez em vez de uma por vez.
 */
public class ValidacaoException extends Exception {

    private final List<String> erros;

    public ValidacaoException(List<String> erros) {
        super(String.join(" ", erros));
        this.erros = new ArrayList<>(erros);
    }

    public ValidacaoException(String erro) {
        this(List.of(erro));
    }

    public List<String> getErros() {
        return Collections.unmodifiableList(erros);
    }
}

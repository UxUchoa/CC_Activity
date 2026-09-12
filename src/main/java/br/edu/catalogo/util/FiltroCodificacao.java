package br.edu.catalogo.util;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Garante UTF-8 em toda requisicao e resposta, inclusive nos acentos dos formularios. */
@WebFilter("/*")
public class FiltroCodificacao implements Filter {

    @Override
    public void doFilter(ServletRequest requisicao, ServletResponse resposta, FilterChain cadeia)
            throws IOException, ServletException {
        requisicao.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resposta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        cadeia.doFilter(requisicao, resposta);
    }
}

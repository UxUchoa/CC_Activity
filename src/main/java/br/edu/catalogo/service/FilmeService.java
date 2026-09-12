package br.edu.catalogo.service;

import br.edu.catalogo.dao.FilmeDAO;
import br.edu.catalogo.exception.RegistroNaoEncontradoException;
import br.edu.catalogo.exception.ValidacaoException;
import br.edu.catalogo.model.Filme;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;

/**
 * Camada de regras de negocio. Valida os dados antes de chegarem ao banco e
 * decide o que caracteriza um registro inexistente. A Servlet nao repete nada
 * disso - apenas chama estes metodos.
 */
public class FilmeService {

    public static final int ANO_MINIMO = 1888;
    public static final int TOLERANCIA_ANOS_FUTUROS = 5;

    private static final int TAMANHO_MAXIMO_TITULO = 150;
    private static final int TAMANHO_MAXIMO_DIRETOR = 120;
    private static final int TAMANHO_MAXIMO_GENERO = 60;
    private static final int TAMANHO_MAXIMO_SINOPSE = 2000;

    private final FilmeDAO filmeDAO;

    public FilmeService() {
        this(new FilmeDAO());
    }

    public FilmeService(FilmeDAO filmeDAO) {
        this.filmeDAO = filmeDAO;
    }

    public Filme cadastrar(Filme filme) throws ValidacaoException {
        normalizar(filme);
        validar(filme);
        return filmeDAO.inserir(filme);
    }

    public List<Filme> listarTodos() {
        return filmeDAO.listarTodos();
    }

    public Filme buscarPorId(Long id) throws RegistroNaoEncontradoException {
        if (id == null) {
            throw new RegistroNaoEncontradoException("Filme nao informado.");
        }
        return filmeDAO.buscarPorId(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException(
                        "Nenhum filme encontrado para o codigo " + id + "."));
    }

    /** Busca por titulo ou diretor; termo vazio devolve o catalogo completo. */
    public List<Filme> buscar(String termo) {
        if (termo == null || termo.isBlank()) {
            return listarTodos();
        }
        return filmeDAO.buscarPorTituloOuDiretor(termo.trim());
    }

    public Filme atualizar(Filme filme) throws ValidacaoException, RegistroNaoEncontradoException {
        if (filme.getId() == null) {
            throw new RegistroNaoEncontradoException("Filme nao informado para atualizacao.");
        }
        normalizar(filme);
        validar(filme);

        if (!filmeDAO.atualizar(filme)) {
            throw new RegistroNaoEncontradoException(
                    "Nenhum filme encontrado para o codigo " + filme.getId() + ".");
        }
        return filme;
    }

    public void excluir(Long id) throws RegistroNaoEncontradoException {
        if (id == null) {
            throw new RegistroNaoEncontradoException("Filme nao informado para exclusao.");
        }
        if (!filmeDAO.excluir(id)) {
            throw new RegistroNaoEncontradoException(
                    "Nenhum filme encontrado para o codigo " + id + ".");
        }
    }

    private void normalizar(Filme filme) {
        filme.setTitulo(aparar(filme.getTitulo()));
        filme.setDiretor(aparar(filme.getDiretor()));
        filme.setGenero(aparar(filme.getGenero()));
        filme.setSinopse(aparar(filme.getSinopse()));
    }

    private void validar(Filme filme) throws ValidacaoException {
        List<String> erros = new ArrayList<>();

        if (vazio(filme.getTitulo())) {
            erros.add("O titulo e obrigatorio.");
        } else if (filme.getTitulo().length() > TAMANHO_MAXIMO_TITULO) {
            erros.add("O titulo deve ter no maximo " + TAMANHO_MAXIMO_TITULO + " caracteres.");
        }

        if (vazio(filme.getDiretor())) {
            erros.add("O diretor e obrigatorio.");
        } else if (filme.getDiretor().length() > TAMANHO_MAXIMO_DIRETOR) {
            erros.add("O diretor deve ter no maximo " + TAMANHO_MAXIMO_DIRETOR + " caracteres.");
        }

        validarAno(filme.getAno(), erros);

        if (filme.getGenero() != null && filme.getGenero().length() > TAMANHO_MAXIMO_GENERO) {
            erros.add("O genero deve ter no maximo " + TAMANHO_MAXIMO_GENERO + " caracteres.");
        }

        if (filme.getSinopse() != null && filme.getSinopse().length() > TAMANHO_MAXIMO_SINOPSE) {
            erros.add("A sinopse deve ter no maximo " + TAMANHO_MAXIMO_SINOPSE + " caracteres.");
        }

        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }
    }

    private void validarAno(Integer ano, List<String> erros) {
        if (ano == null) {
            erros.add("O ano de lancamento e obrigatorio.");
            return;
        }
        int anoMaximo = Year.now().getValue() + TOLERANCIA_ANOS_FUTUROS;
        if (ano < ANO_MINIMO || ano > anoMaximo) {
            erros.add("O ano de lancamento deve estar entre " + ANO_MINIMO + " e " + anoMaximo + ".");
        }
    }

    private boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }

    private String aparar(String valor) {
        return valor == null ? null : valor.trim();
    }
}

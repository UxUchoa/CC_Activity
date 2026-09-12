package br.edu.catalogo.controller;

import br.edu.catalogo.exception.PersistenciaException;
import br.edu.catalogo.exception.RegistroNaoEncontradoException;
import br.edu.catalogo.exception.ValidacaoException;
import br.edu.catalogo.model.Filme;
import br.edu.catalogo.service.FilmeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Controlador do catalogo. Sua unica responsabilidade e traduzir a requisicao
 * HTTP em uma chamada de servico e escolher a view - nao ha SQL nem regra de
 * negocio aqui, diferente da Servlet monolitica que originou o problema.
 */
@WebServlet(name = "filmeServlet", urlPatterns = {"/filmes", "/filmes/*"})
public class FilmeServlet extends HttpServlet {

    private static final String VIEW_LISTA = "/WEB-INF/views/filmes/lista.jsp";
    private static final String VIEW_FORMULARIO = "/WEB-INF/views/filmes/formulario.jsp";
    private static final String VIEW_DETALHES = "/WEB-INF/views/filmes/detalhes.jsp";
    private static final String VIEW_ERRO = "/WEB-INF/views/erro.jsp";

    private transient FilmeService filmeService;

    @Override
    public void init() {
        this.filmeService = new FilmeService();
    }

    @Override
    protected void doGet(HttpServletRequest requisicao, HttpServletResponse resposta)
            throws ServletException, IOException {

        String caminho = normalizarCaminho(requisicao.getPathInfo());
        try {
            if (caminho.isEmpty()) {
                listar(requisicao, resposta);
            } else if (caminho.equals("novo")) {
                abrirFormularioDeCadastro(requisicao, resposta);
            } else if (caminho.endsWith("/editar")) {
                abrirFormularioDeEdicao(requisicao, resposta, extrairId(caminho));
            } else if (caminho.endsWith("/excluir")) {
                confirmarExclusao(requisicao, resposta, extrairId(caminho));
            } else {
                detalhar(requisicao, resposta, extrairId(caminho));
            }
        } catch (RegistroNaoEncontradoException e) {
            encaminharErro(requisicao, resposta, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (NumberFormatException e) {
            encaminharErro(requisicao, resposta, HttpServletResponse.SC_BAD_REQUEST,
                    "O endereco informado nao corresponde a um filme valido.");
        } catch (PersistenciaException e) {
            tratarFalhaInterna(requisicao, resposta, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest requisicao, HttpServletResponse resposta)
            throws ServletException, IOException {

        String caminho = normalizarCaminho(requisicao.getPathInfo());
        try {
            if (caminho.isEmpty()) {
                cadastrar(requisicao, resposta);
            } else if (caminho.endsWith("/excluir")) {
                excluir(requisicao, resposta, extrairId(caminho));
            } else {
                atualizar(requisicao, resposta, extrairId(caminho));
            }
        } catch (RegistroNaoEncontradoException e) {
            encaminharErro(requisicao, resposta, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (NumberFormatException e) {
            encaminharErro(requisicao, resposta, HttpServletResponse.SC_BAD_REQUEST,
                    "O endereco informado nao corresponde a um filme valido.");
        } catch (PersistenciaException e) {
            tratarFalhaInterna(requisicao, resposta, e);
        }
    }

    private void listar(HttpServletRequest requisicao, HttpServletResponse resposta)
            throws ServletException, IOException {

        String busca = requisicao.getParameter("busca");
        requisicao.setAttribute("filmes", filmeService.buscar(busca));
        requisicao.setAttribute("busca", busca);
        requisicao.setAttribute("mensagemSucesso", traduzirSucesso(requisicao.getParameter("sucesso")));
        encaminhar(requisicao, resposta, VIEW_LISTA);
    }

    private void abrirFormularioDeCadastro(HttpServletRequest requisicao, HttpServletResponse resposta)
            throws ServletException, IOException {

        requisicao.setAttribute("filme", new Filme());
        requisicao.setAttribute("acao", requisicao.getContextPath() + "/filmes");
        requisicao.setAttribute("tituloPagina", "Cadastrar filme");
        encaminhar(requisicao, resposta, VIEW_FORMULARIO);
    }

    private void abrirFormularioDeEdicao(HttpServletRequest requisicao, HttpServletResponse resposta, Long id)
            throws ServletException, IOException, RegistroNaoEncontradoException {

        requisicao.setAttribute("filme", filmeService.buscarPorId(id));
        requisicao.setAttribute("acao", requisicao.getContextPath() + "/filmes/" + id);
        requisicao.setAttribute("tituloPagina", "Editar filme");
        encaminhar(requisicao, resposta, VIEW_FORMULARIO);
    }

    private void detalhar(HttpServletRequest requisicao, HttpServletResponse resposta, Long id)
            throws ServletException, IOException, RegistroNaoEncontradoException {

        requisicao.setAttribute("filme", filmeService.buscarPorId(id));
        requisicao.setAttribute("mensagemSucesso", traduzirSucesso(requisicao.getParameter("sucesso")));
        encaminhar(requisicao, resposta, VIEW_DETALHES);
    }

    private void confirmarExclusao(HttpServletRequest requisicao, HttpServletResponse resposta, Long id)
            throws ServletException, IOException, RegistroNaoEncontradoException {

        requisicao.setAttribute("filme", filmeService.buscarPorId(id));
        requisicao.setAttribute("confirmandoExclusao", Boolean.TRUE);
        encaminhar(requisicao, resposta, VIEW_DETALHES);
    }

    private void cadastrar(HttpServletRequest requisicao, HttpServletResponse resposta)
            throws ServletException, IOException {

        Filme filme = lerFormulario(requisicao, null);
        try {
            filmeService.cadastrar(filme);
            redirecionar(requisicao, resposta, "/filmes?sucesso=cadastro");
        } catch (ValidacaoException e) {
            reexibirFormulario(requisicao, resposta, filme, e,
                    requisicao.getContextPath() + "/filmes", "Cadastrar filme");
        }
    }

    private void atualizar(HttpServletRequest requisicao, HttpServletResponse resposta, Long id)
            throws ServletException, IOException, RegistroNaoEncontradoException {

        Filme filme = lerFormulario(requisicao, id);
        try {
            filmeService.atualizar(filme);
            redirecionar(requisicao, resposta, "/filmes/" + id + "?sucesso=edicao");
        } catch (ValidacaoException e) {
            reexibirFormulario(requisicao, resposta, filme, e,
                    requisicao.getContextPath() + "/filmes/" + id, "Editar filme");
        }
    }

    private void excluir(HttpServletRequest requisicao, HttpServletResponse resposta, Long id)
            throws IOException, RegistroNaoEncontradoException {

        filmeService.excluir(id);
        redirecionar(requisicao, resposta, "/filmes?sucesso=exclusao");
    }

    /** Monta o objeto a partir dos campos do formulario, sem aplicar regra de negocio. */
    private Filme lerFormulario(HttpServletRequest requisicao, Long id) {
        Filme filme = new Filme();
        filme.setId(id);
        filme.setTitulo(requisicao.getParameter("titulo"));
        filme.setDiretor(requisicao.getParameter("diretor"));
        filme.setAno(converterAno(requisicao.getParameter("ano")));
        filme.setGenero(requisicao.getParameter("genero"));
        filme.setSinopse(requisicao.getParameter("sinopse"));
        return filme;
    }

    /** Ano nao numerico vira null para que o FilmeService produza a mensagem ao usuario. */
    private Integer converterAno(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void reexibirFormulario(HttpServletRequest requisicao, HttpServletResponse resposta,
                                    Filme filme, ValidacaoException erro, String acao, String tituloPagina)
            throws ServletException, IOException {

        resposta.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        requisicao.setAttribute("filme", filme);
        requisicao.setAttribute("erros", erro.getErros());
        requisicao.setAttribute("acao", acao);
        requisicao.setAttribute("tituloPagina", tituloPagina);
        encaminhar(requisicao, resposta, VIEW_FORMULARIO);
    }

    private String traduzirSucesso(String codigo) {
        if (codigo == null) {
            return null;
        }
        return switch (codigo) {
            case "cadastro" -> "Filme cadastrado com sucesso.";
            case "edicao" -> "Filme atualizado com sucesso.";
            case "exclusao" -> "Filme excluido com sucesso.";
            default -> null;
        };
    }

    private String normalizarCaminho(String pathInfo) {
        if (pathInfo == null) {
            return "";
        }
        String caminho = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        return caminho.endsWith("/") ? caminho.substring(0, caminho.length() - 1) : caminho;
    }

    private Long extrairId(String caminho) {
        int separador = caminho.indexOf('/');
        String primeiroSegmento = separador >= 0 ? caminho.substring(0, separador) : caminho;
        return Long.valueOf(primeiroSegmento);
    }

    private void tratarFalhaInterna(HttpServletRequest requisicao, HttpServletResponse resposta,
                                    RuntimeException e) throws ServletException, IOException {

        getServletContext().log("Falha ao processar requisicao do catalogo de filmes", e);
        encaminharErro(requisicao, resposta, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Nao foi possivel concluir a operacao. Tente novamente em alguns instantes.");
    }

    private void encaminharErro(HttpServletRequest requisicao, HttpServletResponse resposta,
                                int status, String mensagem) throws ServletException, IOException {

        resposta.setStatus(status);
        requisicao.setAttribute("mensagemErro", mensagem);
        encaminhar(requisicao, resposta, VIEW_ERRO);
    }

    private void encaminhar(HttpServletRequest requisicao, HttpServletResponse resposta, String view)
            throws ServletException, IOException {
        requisicao.getRequestDispatcher(view).forward(requisicao, resposta);
    }

    private void redirecionar(HttpServletRequest requisicao, HttpServletResponse resposta, String destino)
            throws IOException {
        resposta.sendRedirect(requisicao.getContextPath() + destino);
    }
}

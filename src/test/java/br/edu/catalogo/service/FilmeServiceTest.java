package br.edu.catalogo.service;

import br.edu.catalogo.dao.FilmeDAO;
import br.edu.catalogo.exception.RegistroNaoEncontradoException;
import br.edu.catalogo.exception.ValidacaoException;
import br.edu.catalogo.model.Filme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testa as regras do FilmeService isoladamente. O DAO e substituido por um
 * dublê em memoria - prova pratica de que a separacao em camadas tornou o
 * codigo testavel, o que era impossivel na Servlet monolitica original.
 */
class FilmeServiceTest {

    private FilmeDAOEmMemoria filmeDAO;
    private FilmeService filmeService;

    @BeforeEach
    void prepararCenario() {
        filmeDAO = new FilmeDAOEmMemoria();
        filmeService = new FilmeService(filmeDAO);
    }

    @Test
    @DisplayName("Cadastro sem titulo e recusado")
    void tituloEObrigatorio() {
        Filme filme = filmeValido();
        filme.setTitulo("   ");

        ValidacaoException erro = assertThrows(ValidacaoException.class, () -> filmeService.cadastrar(filme));

        assertTrue(contem(erro, "titulo e obrigatorio"), "esperava mensagem sobre titulo: " + erro.getErros());
        assertTrue(filmeDAO.inseridos.isEmpty(), "nada deveria chegar ao banco");
    }

    @Test
    @DisplayName("Cadastro sem diretor e recusado")
    void diretorEObrigatorio() {
        Filme filme = filmeValido();
        filme.setDiretor(null);

        ValidacaoException erro = assertThrows(ValidacaoException.class, () -> filmeService.cadastrar(filme));

        assertTrue(contem(erro, "diretor e obrigatorio"), "esperava mensagem sobre diretor: " + erro.getErros());
        assertTrue(filmeDAO.inseridos.isEmpty());
    }

    @Test
    @DisplayName("Ano anterior ao primeiro filme da historia e recusado")
    void anoAnteriorAoMinimoEInvalido() {
        Filme filme = filmeValido();
        filme.setAno(FilmeService.ANO_MINIMO - 1);

        ValidacaoException erro = assertThrows(ValidacaoException.class, () -> filmeService.cadastrar(filme));

        assertTrue(contem(erro, "ano de lancamento deve estar entre"), erro.getErros().toString());
    }

    @Test
    @DisplayName("Ano muito no futuro e recusado")
    void anoFuturoDemaisEInvalido() {
        Filme filme = filmeValido();
        filme.setAno(Year.now().getValue() + FilmeService.TOLERANCIA_ANOS_FUTUROS + 1);

        ValidacaoException erro = assertThrows(ValidacaoException.class, () -> filmeService.cadastrar(filme));

        assertTrue(contem(erro, "ano de lancamento deve estar entre"), erro.getErros().toString());
    }

    @Test
    @DisplayName("Ano nao informado e recusado")
    void anoEObrigatorio() {
        Filme filme = filmeValido();
        filme.setAno(null);

        ValidacaoException erro = assertThrows(ValidacaoException.class, () -> filmeService.cadastrar(filme));

        assertTrue(contem(erro, "ano de lancamento e obrigatorio"), erro.getErros().toString());
    }

    @Test
    @DisplayName("Todos os erros sao reportados de uma vez")
    void acumulaErrosDeValidacao() {
        Filme filme = new Filme("", "", null, null, null);

        ValidacaoException erro = assertThrows(ValidacaoException.class, () -> filmeService.cadastrar(filme));

        assertEquals(3, erro.getErros().size(), "titulo, diretor e ano: " + erro.getErros());
    }

    @Test
    @DisplayName("Cadastro valido recebe id e chega ao DAO com os campos aparados")
    void cadastroValidoEPersistido() throws ValidacaoException {
        Filme filme = new Filme("  Cidade de Deus  ", "  Fernando Meirelles ", 2002, " Drama ", "  Sinopse  ");

        Filme salvo = filmeService.cadastrar(filme);

        assertNotNull(salvo.getId(), "o filme cadastrado deve receber um id");
        assertEquals(1, filmeDAO.inseridos.size());
        assertEquals("Cidade de Deus", salvo.getTitulo());
        assertEquals("Fernando Meirelles", salvo.getDiretor());
        assertEquals("Drama", salvo.getGenero());
        assertEquals("Sinopse", salvo.getSinopse());
    }

    @Test
    @DisplayName("Atualizar registro inexistente lanca RegistroNaoEncontradoException")
    void atualizarRegistroInexistente() {
        Filme filme = filmeValido();
        filme.setId(999L);

        RegistroNaoEncontradoException erro =
                assertThrows(RegistroNaoEncontradoException.class, () -> filmeService.atualizar(filme));

        assertTrue(erro.getMessage().contains("999"), erro.getMessage());
    }

    @Test
    @DisplayName("Atualizar sem id e tratado como registro inexistente")
    void atualizarSemIdERecusado() {
        assertThrows(RegistroNaoEncontradoException.class, () -> filmeService.atualizar(filmeValido()));
    }

    @Test
    @DisplayName("Validacao acontece antes de tentar gravar a atualizacao")
    void atualizacaoInvalidaNaoChegaAoDAO() throws ValidacaoException {
        Filme salvo = filmeService.cadastrar(filmeValido());
        salvo.setTitulo("");

        assertThrows(ValidacaoException.class, () -> filmeService.atualizar(salvo));
        assertEquals(0, filmeDAO.atualizacoes);
    }

    @Test
    @DisplayName("Buscar por id inexistente lanca RegistroNaoEncontradoException")
    void buscarPorIdInexistente() {
        assertThrows(RegistroNaoEncontradoException.class, () -> filmeService.buscarPorId(404L));
    }

    @Test
    @DisplayName("Excluir registro inexistente lanca RegistroNaoEncontradoException")
    void excluirRegistroInexistente() {
        assertThrows(RegistroNaoEncontradoException.class, () -> filmeService.excluir(123L));
    }

    @Test
    @DisplayName("Busca com termo vazio devolve o catalogo completo")
    void buscaSemTermoListaTudo() throws ValidacaoException {
        filmeService.cadastrar(filmeValido());

        assertEquals(1, filmeService.buscar("   ").size());
        assertEquals(1, filmeService.buscar(null).size());
    }

    @Test
    @DisplayName("Busca com termo delega ao DAO ja sem espacos")
    void buscaComTermoDelegaAoDAO() {
        filmeService.buscar("  meirelles  ");

        assertEquals("meirelles", filmeDAO.ultimoTermoBuscado);
    }

    private Filme filmeValido() {
        return new Filme("Cidade de Deus", "Fernando Meirelles", 2002, "Drama", "Sinopse de demonstracao.");
    }

    private boolean contem(ValidacaoException erro, String trecho) {
        return erro.getErros().stream().anyMatch(mensagem -> mensagem.toLowerCase().contains(trecho.toLowerCase()));
    }

    /** DAO de mentira: guarda tudo em uma lista, sem banco de dados. */
    private static class FilmeDAOEmMemoria extends FilmeDAO {

        private final List<Filme> inseridos = new ArrayList<>();
        private long proximoId = 1L;
        private int atualizacoes;
        private String ultimoTermoBuscado;

        @Override
        public Filme inserir(Filme filme) {
            filme.setId(proximoId++);
            inseridos.add(filme);
            return filme;
        }

        @Override
        public List<Filme> listarTodos() {
            return new ArrayList<>(inseridos);
        }

        @Override
        public Optional<Filme> buscarPorId(Long id) {
            return inseridos.stream().filter(filme -> filme.getId().equals(id)).findFirst();
        }

        @Override
        public List<Filme> buscarPorTituloOuDiretor(String termo) {
            ultimoTermoBuscado = termo;
            return new ArrayList<>(inseridos);
        }

        @Override
        public boolean atualizar(Filme filme) {
            atualizacoes++;
            return buscarPorId(filme.getId()).isPresent();
        }

        @Override
        public boolean excluir(Long id) {
            return inseridos.removeIf(filme -> filme.getId().equals(id));
        }
    }
}

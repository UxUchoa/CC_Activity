<%@ page isErrorPage="true" %>
<%@ include file="/WEB-INF/views/fragmentos/cabecalho.jspf" %>

<section class="painel painel--estreito painel--centro">
    <h1 class="titulo">Não foi possível concluir</h1>

    <p class="alerta alerta--erro">
        <c:out value="${empty mensagemErro
                ? 'Ocorreu um erro inesperado ao processar sua solicitação.'
                : mensagemErro}"/>
    </p>

    <p>Você pode voltar ao catálogo e tentar novamente.</p>

    <div class="formulario__acoes">
        <a class="botao botao--destaque" href="${pageContext.request.contextPath}/filmes">Ir para o catálogo</a>
    </div>
</section>

<%@ include file="/WEB-INF/views/fragmentos/rodape.jspf" %>

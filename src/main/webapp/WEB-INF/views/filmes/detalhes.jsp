<%@ include file="/WEB-INF/views/fragmentos/cabecalho.jspf" %>

<c:if test="${not empty mensagemSucesso}">
    <p class="alerta alerta--sucesso">${mensagemSucesso}</p>
</c:if>

<section class="painel painel--estreito">
    <p class="voltar"><a href="${pageContext.request.contextPath}/filmes">&larr; Voltar para a lista</a></p>

    <h1 class="titulo"><c:out value="${filme.titulo}"/></h1>

    <dl class="detalhes">
        <dt>Diretor</dt>
        <dd><c:out value="${filme.diretor}"/></dd>

        <dt>Ano de lançamento</dt>
        <dd><c:out value="${filme.ano}"/></dd>

        <dt>Gênero</dt>
        <dd><c:out value="${empty filme.genero ? 'Não informado' : filme.genero}"/></dd>

        <dt>Sinopse</dt>
        <dd><c:out value="${empty filme.sinopse ? 'Sem sinopse cadastrada.' : filme.sinopse}"/></dd>
    </dl>

    <c:choose>
        <c:when test="${confirmandoExclusao}">
            <div class="alerta alerta--erro">
                <p class="alerta__titulo">Excluir este filme?</p>
                <p>A exclusão é definitiva e o registro não poderá ser recuperado.</p>
            </div>
            <form method="post" action="${pageContext.request.contextPath}/filmes/${filme.id}/excluir"
                  onsubmit="return confirm('Confirma a exclusão do filme &quot;${filme.titulo}&quot;?');">
                <div class="formulario__acoes">
                    <button class="botao botao--perigo" type="submit">Sim, excluir</button>
                    <a class="botao botao--neutro"
                       href="${pageContext.request.contextPath}/filmes/${filme.id}">Cancelar</a>
                </div>
            </form>
        </c:when>
        <c:otherwise>
            <div class="formulario__acoes">
                <a class="botao botao--destaque"
                   href="${pageContext.request.contextPath}/filmes/${filme.id}/editar">Editar</a>
                <a class="botao botao--perigo"
                   href="${pageContext.request.contextPath}/filmes/${filme.id}/excluir">Excluir</a>
            </div>
        </c:otherwise>
    </c:choose>
</section>

<%@ include file="/WEB-INF/views/fragmentos/rodape.jspf" %>

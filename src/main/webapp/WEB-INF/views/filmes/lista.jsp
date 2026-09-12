<%@ include file="/WEB-INF/views/fragmentos/cabecalho.jspf" %>

<c:if test="${not empty mensagemSucesso}">
    <p class="alerta alerta--sucesso">${mensagemSucesso}</p>
</c:if>

<section class="painel">
    <h1 class="titulo">Filmes do catálogo</h1>

    <form class="busca" method="get" action="${pageContext.request.contextPath}/filmes">
        <label class="busca__campo">
            <span class="rotulo-oculto">Buscar por título ou diretor</span>
            <input type="search" name="busca" placeholder="Buscar por título ou diretor"
                   value="<c:out value='${busca}'/>">
        </label>
        <button class="botao" type="submit">Buscar</button>
        <c:if test="${not empty busca}">
            <a class="botao botao--neutro" href="${pageContext.request.contextPath}/filmes">Limpar</a>
        </c:if>
    </form>

    <c:choose>
        <c:when test="${empty filmes}">
            <p class="vazio">
                <c:choose>
                    <c:when test="${not empty busca}">
                        Nenhum filme encontrado para <strong><c:out value="${busca}"/></strong>.
                    </c:when>
                    <c:otherwise>
                        Ainda não há filmes cadastrados. Comece cadastrando o primeiro.
                    </c:otherwise>
                </c:choose>
            </p>
        </c:when>
        <c:otherwise>
            <p class="contador">${filmes.size()} filme(s) encontrado(s).</p>
            <div class="tabela-rolagem">
                <table class="tabela">
                    <thead>
                    <tr>
                        <th>Título</th>
                        <th>Diretor</th>
                        <th>Ano</th>
                        <th>Gênero</th>
                        <th class="tabela__acoes">Ações</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="filme" items="${filmes}">
                        <tr>
                            <td data-rotulo="Título"><c:out value="${filme.titulo}"/></td>
                            <td data-rotulo="Diretor"><c:out value="${filme.diretor}"/></td>
                            <td data-rotulo="Ano"><c:out value="${filme.ano}"/></td>
                            <td data-rotulo="Gênero">
                                <c:out value="${empty filme.genero ? '—' : filme.genero}"/>
                            </td>
                            <td data-rotulo="Ações" class="tabela__acoes">
                                <a class="acao" href="${pageContext.request.contextPath}/filmes/${filme.id}">Ver</a>
                                <a class="acao" href="${pageContext.request.contextPath}/filmes/${filme.id}/editar">Editar</a>
                                <a class="acao acao--perigo"
                                   href="${pageContext.request.contextPath}/filmes/${filme.id}/excluir">Excluir</a>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</section>

<%@ include file="/WEB-INF/views/fragmentos/rodape.jspf" %>

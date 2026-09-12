<%@ include file="/WEB-INF/views/fragmentos/cabecalho.jspf" %>

<section class="painel painel--estreito">
    <h1 class="titulo"><c:out value="${tituloPagina}"/></h1>

    <c:if test="${not empty erros}">
        <div class="alerta alerta--erro">
            <p class="alerta__titulo">Corrija os campos abaixo:</p>
            <ul>
                <c:forEach var="erro" items="${erros}">
                    <li><c:out value="${erro}"/></li>
                </c:forEach>
            </ul>
        </div>
    </c:if>

    <form class="formulario" method="post" action="${acao}">
        <div class="campo">
            <label for="titulo">Título *</label>
            <input type="text" id="titulo" name="titulo" maxlength="150" required
                   value="<c:out value='${filme.titulo}'/>">
        </div>

        <div class="campo">
            <label for="diretor">Diretor *</label>
            <input type="text" id="diretor" name="diretor" maxlength="120" required
                   value="<c:out value='${filme.diretor}'/>">
        </div>

        <div class="campo-duplo">
            <div class="campo">
                <label for="ano">Ano de lançamento *</label>
                <input type="number" id="ano" name="ano" min="1888" max="2100" required
                       value="<c:out value='${filme.ano}'/>">
            </div>
            <div class="campo">
                <label for="genero">Gênero</label>
                <input type="text" id="genero" name="genero" maxlength="60"
                       value="<c:out value='${filme.genero}'/>">
            </div>
        </div>

        <div class="campo">
            <label for="sinopse">Sinopse</label>
            <textarea id="sinopse" name="sinopse" rows="5" maxlength="2000"><c:out value="${filme.sinopse}"/></textarea>
        </div>

        <p class="ajuda">Campos marcados com * são obrigatórios.</p>

        <div class="formulario__acoes">
            <button class="botao botao--destaque" type="submit">Salvar</button>
            <a class="botao botao--neutro" href="${pageContext.request.contextPath}/filmes">Cancelar</a>
        </div>
    </form>
</section>

<%@ include file="/WEB-INF/views/fragmentos/rodape.jspf" %>

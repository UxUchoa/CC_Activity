# Catálogo Simples de Filmes

Projeto acadêmico da disciplina **Projetos Computacionais** — Ciência da Computação.
Resposta à **situação-problema 3**: refatorar um catálogo de filmes que havia sido escrito
inteiramente dentro de uma única Servlet.

---

## 1. Descrição do problema

Um aluno implementou todas as funcionalidades do catálogo — CRUD completo e busca —
diretamente em uma Servlet. O resultado foi:

- uma classe extensa, com dezenas de responsabilidades misturadas;
- SQL espalhado e repetido em vários pontos do arquivo;
- abertura e fechamento de conexão duplicados em cada operação;
- regras de validação copiadas entre cadastro e edição;
- código impossível de testar sem subir um servidor e um banco;
- qualquer mudança de regra exigindo mexer na mesma classe gigante.

## 2. Solução aplicada

O código foi redistribuído em camadas, cada uma com uma responsabilidade única:

| Antes (Servlet monolítica) | Depois |
|---|---|
| SQL dentro da Servlet | Todo SQL isolado no `FilmeDAO` |
| `DriverManager.getConnection` repetido | Um único `ConexaoFactory` |
| Validação duplicada em cadastro e edição | Um único `FilmeService.validar` |
| HTML montado em `out.println` | JSP + JSTL em `WEB-INF/views` |
| Impossível testar | `FilmeService` coberto por 14 testes JUnit |

Ganhos concretos:

- **Testabilidade** — o `FilmeService` recebe o DAO pelo construtor, então os testes
  injetam um DAO em memória e validam as regras sem banco e sem servidor.
- **Fim da duplicação** — inserir e atualizar compartilham o mesmo `preencherParametros`;
  leitura de resultado compartilha o mesmo `mapear`.
- **Segurança** — `PreparedStatement` em 100% das consultas, sem concatenar entrada do usuário.
- **Manutenção** — trocar o banco mexe só no `util`; mudar uma regra mexe só no `service`.

## 3. As camadas

```
Navegador
    │
    ▼
Controller  FilmeServlet          lê a requisição, chama o Service, escolhe a view
    │                             (não contém SQL nem regra de negócio)
    ▼
Service     FilmeService          validações e regras de negócio
    │
    ▼
DAO         FilmeDAO              todas as operações JDBC
    │
    ▼
Util        ConexaoFactory        único ponto que abre conexão
            InicializadorBanco    cria a tabela e a carga inicial
            FiltroCodificacao     garante UTF-8 em requisição e resposta

Model       Filme                 entidade de domínio, sem dependência de HTTP ou JDBC
Exception   ValidacaoException    agrupa as mensagens de validação
            RegistroNaoEncontradoException / PersistenciaException
View        JSP + JSTL            apresentação, protegida em WEB-INF
```

| Camada | Pacote | Papel |
|---|---|---|
| Model | `br.edu.catalogo.model` | Entidade `Filme` (`id`, `titulo`, `diretor`, `ano`, `genero`, `sinopse`) |
| DAO | `br.edu.catalogo.dao` | `inserir`, `listarTodos`, `buscarPorId`, `buscarPorTituloOuDiretor`, `atualizar`, `excluir` |
| Service | `br.edu.catalogo.service` | Campos obrigatórios, faixa do ano, tamanhos, detecção de registro inexistente |
| Controller | `br.edu.catalogo.controller` | Roteamento HTTP, leitura do formulário, escolha da view, Post/Redirect/Get |
| View | `src/main/webapp` | JSP com JSTL e CSS responsivo |
| Util | `br.edu.catalogo.util` | Conexão, inicialização do banco, filtro de codificação |

## 4. Tecnologias utilizadas

- Java 17
- Maven
- Jakarta Servlet 6.0
- JSP + JSTL 3.0 (`jakarta.tags.core`)
- JDBC puro
- Banco H2 (arquivo local)
- Apache Tomcat 10.1
- HTML5 e CSS3 (sem framework)
- JUnit 5

Sem Spring, sem Hibernate, sem Lombok — os conceitos de Servlet, JDBC e orientação a
objetos ficam visíveis no código.

## 5. Pré-requisitos

- JDK 17 instalado e `JAVA_HOME` apontando para ele
- Maven 3.8 ou superior
- Apache Tomcat 10.1 (necessário apenas para executar; não é necessário para compilar e testar)

Verificação rápida:

```bash
java -version
```

```bash
mvn -v
```

## 6. Compilar e testar

Rodar apenas os testes:

```bash
mvn test
```

Compilar, testar e gerar o pacote:

```bash
mvn clean test package
```

O WAR é gerado em:

```
target/catalogo-filmes.war
```

## 7. Executar no Tomcat 10

1. Gere o WAR com `mvn clean test package`.
2. Copie `target/catalogo-filmes.war` para a pasta `webapps` do Tomcat 10.
3. Defina `CATALINA_HOME` e inicie o Tomcat.

   Windows (PowerShell):

   ```powershell
   $env:CATALINA_HOME="C:\caminho\para\apache-tomcat-10.1.59"
   & "$env:CATALINA_HOME\bin\startup.bat"
   ```

   Linux/macOS:

   ```bash
   export CATALINA_HOME=/caminho/para/apache-tomcat-10.1.59
   "$CATALINA_HOME/bin/startup.sh"
   ```

   > **Atenção:** sem `CATALINA_HOME` definido, o `startup.bat` procura o Tomcat no
   > **diretório atual** do terminal, não na pasta onde o script está. Chamar o script pelo
   > caminho completo a partir de outra pasta falha com
   > `The CATALINA_HOME environment variable is not defined correctly`.

4. Aguarde o Tomcat expandir o WAR (surge a pasta `webapps/catalogo-filmes`).
5. Para parar, use `shutdown.bat` (ou `shutdown.sh`) com a mesma variável definida.

O `InicializadorBanco` cria a tabela `filmes` no start e insere três filmes de
demonstração apenas quando a tabela está vazia — reiniciar o servidor não duplica registros.

### Endereço de acesso

```
http://localhost:8080/catalogo-filmes/
```

A raiz redireciona para `/catalogo-filmes/filmes`.

### Credenciais

A aplicação **não possui login** — nenhuma credencial é necessária.

O banco H2 é criado em `data/catalogo-filmes.mv.db`, relativo ao diretório de trabalho do
Tomcat, com usuário `sa` e senha vazia (padrão de desenvolvimento local).

## 8. Estrutura de diretórios

```
catalogo-filmes/
├── pom.xml
├── README.md
└── src
    ├── main
    │   ├── java/br/edu/catalogo
    │   │   ├── controller/FilmeServlet.java
    │   │   ├── dao/FilmeDAO.java
    │   │   ├── exception
    │   │   │   ├── PersistenciaException.java
    │   │   │   ├── RegistroNaoEncontradoException.java
    │   │   │   └── ValidacaoException.java
    │   │   ├── model/Filme.java
    │   │   ├── service/FilmeService.java
    │   │   └── util
    │   │       ├── ConexaoFactory.java
    │   │       ├── FiltroCodificacao.java
    │   │       └── InicializadorBanco.java
    │   └── webapp
    │       ├── index.jsp
    │       ├── assets/css/style.css
    │       └── WEB-INF
    │           ├── web.xml
    │           └── views
    │               ├── erro.jsp
    │               ├── fragmentos
    │               │   ├── cabecalho.jspf
    │               │   └── rodape.jspf
    │               └── filmes
    │                   ├── detalhes.jsp
    │                   ├── formulario.jsp
    │                   └── lista.jsp
    └── test
        └── java/br/edu/catalogo/service/FilmeServiceTest.java
```

## 9. Funcionalidades disponíveis

| # | Funcionalidade | Rota |
|---|---|---|
| 1 | Listar todos os filmes | `GET /filmes` |
| 2 | Buscar por título ou diretor | `GET /filmes?busca=termo` |
| 3 | Abrir formulário de cadastro | `GET /filmes/novo` |
| 4 | Cadastrar filme | `POST /filmes` |
| 5 | Ver detalhes | `GET /filmes/{id}` |
| 6 | Abrir formulário de edição | `GET /filmes/{id}/editar` |
| 7 | Salvar edição | `POST /filmes/{id}` |
| 8 | Tela de confirmação de exclusão | `GET /filmes/{id}/excluir` |
| 9 | Excluir filme | `POST /filmes/{id}/excluir` |

Comportamentos de apoio:

- **Validações** — título e diretor obrigatórios; ano obrigatório e entre 1888 e o ano
  atual mais 5; limites de tamanho em todos os campos. Todas as mensagens aparecem de uma vez.
- **Mensagens amigáveis** — confirmação verde após cadastrar, editar e excluir; lista
  vermelha de erros no formulário.
- **Exclusão com dupla confirmação** — página dedicada mais um `confirm` no navegador.
- **Página de erro** — registro inexistente (404), URL inválida (400) e falha interna (500)
  caem em `erro.jsp`, sempre com texto amigável; o detalhe técnico vai para o log do servidor.
- **Post/Redirect/Get** — após gravar, o navegador é redirecionado, então atualizar a
  página não reenvia o formulário.
- **Interface responsiva** — em telas estreitas a tabela vira cartões empilhados.

## 10. Testes

`FilmeServiceTest` cobre as regras de negócio com um DAO em memória, sem banco e sem servidor:

- título obrigatório;
- diretor obrigatório;
- ano obrigatório, ano abaixo do mínimo e ano muito no futuro;
- acúmulo de vários erros em uma única resposta;
- cadastro válido, com id atribuído e campos aparados;
- atualização de registro inexistente e atualização sem id;
- validação executada antes de tentar gravar;
- busca por id inexistente e exclusão de registro inexistente;
- busca com termo vazio devolvendo o catálogo completo.

Resultado da última execução:

```
Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

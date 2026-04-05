# Projeto de Automacao de Testes

Este projeto automatiza testes do site "https://blogdoagi.com.br/" com foco na validação da funcionalidade de busca de artigos. 
A suite foi estruturada para executar cenários BDD com Cucumber usando Selenium WebDriver, orquestração com TestNG e gerenciamento de build pelo Maven.

## Objetivo

O objetivo da automacao e validar o comportamento da busca de artigos no site, cobrindo cenarios positivos e negativos, variacoes de entrada do usuario e diferentes formas de disparar a pesquisa pela interface.

## Cenários mapeados

Feature: Busca de artigos no BlogAgi

Background:
Given que o usuário acessa a página inicial do blog

@E2E @positive
Scenario Outline: Busca <Nome do Caso de Teste>
When o usuário digita <termo> no campo de busca
And pressiona Enter
Then deve visualizar uma lista de artigos
And os resultados devem conter a palavra "<resultado>" no título ou conteúdo

    Examples:
      | Nome do Caso de Teste                  | termo | resultado |
      | por palavra-chave válida usando enter  | "pix"     | pix       |
      | é case insensitive                     | "PIX"     | PIX       |
      | com espaços extras                     | " pix "   | pix       |

@E2E @negative @regression
Scenario Outline: Tentativa de Busca - <Nome do Caso de Teste>
When o usuário digita "<termo>" no campo de busca
And pressiona Enter
Then deve visualizar mensagem de "<resultado>"

    Examples:
      | Nome do Caso de Teste          | termo       | resultado                                                                                  |
      | Busca sem resultados           | xyzabc123 | Lamentamos, mas nada foi encontrado para sua pesquisa, tente novamente com outras palavras.  |
      | Busca com caracteres especiais | %$#@!     | Lamentamos, mas nada foi encontrado para sua pesquisa, tente novamente com outras palavras.  |
      | Busca com termo parcial        | emprés    | Lamentamos, mas nada foi encontrado para sua pesquisa, tente novamente com outras palavras.  |

@E2E
Scenario: Busca usando clique na lupa
When o usuário digita "empréstimo" no campo de busca
And clica no ícone de busca
Then deve visualizar uma lista de artigos

@E2E
Scenario: Busca ignora acentuação
When o usuário digita "emprestimo" no campo de busca
And pressiona Enter
Then os resultados devem incluir conteúdos de "empréstimo"


## Arquitetura do Projeto

A automacao foi organizada em camadas para separar responsabilidade e facilitar manutencao:

- `src/test/resources/features`
Contem os arquivos `.feature` com os cenarios escritos em Gherkin. E a camada de especificacao funcional do comportamento esperado.

- `src/test/java/steps`
Contem os step definitions que fazem a ponte entre os passos descritos nas features e a implementacao em Java.

- `src/test/java/page`
Implementa o padrao Page Object Model. As classes encapsulam elementos e acoes da interface:
`BasePage` centraliza driver, waits, actions e helper compartilhado.
`BlogPage` representa a home e o fluxo de busca.
`BlogSearchPage` representa a pagina de resultados e suas validacoes.

- `src/test/java/factory`
Centraliza a criacao e o gerenciamento do WebDriver.
`DriverFactory` cria instancias de browser.
`DriverManager` usa `ThreadLocal<WebDriver>` para isolar o driver por thread e suportar execucao paralela.

- `src/test/java/hooks`
Implementa os hooks do Cucumber.
`Hooks` inicializa o navegador antes de cada cenario, le o browser via propriedade de sistema, captura evidencias e encerra o driver ao final.

- `src/test/java/runners`
Contem o runner principal da suite.
`TestRunner` integra Cucumber com TestNG e expoe os cenarios via `DataProvider(parallel = true)`, permitindo paralelismo.

- `src/test/java/utils`
Contem classes utilitarias como apoio a interacoes, screenshots, logs e representacao de artigos retornados.

- `src/test/java/config`
Contem suporte a leitura de parametros de execucao.

- `pom.xml`
Centraliza dependencias, plugins de build, configuracao do Surefire, geracao de JavaDoc e geracao de relatorios Allure.

## Fluxo de Execucao

O fluxo implementado hoje funciona assim:

1. O `TestRunner` localiza as features e os pacotes de `steps` e `hooks`.
2. O `Hooks @Before` identifica o browser informado por propriedade de sistema (`-Dbrowser`) e cria o driver correspondente.
3. Os steps chamam os page objects para executar a busca e validar resultados.
4. O `Hooks @After` captura screenshot, registra logs, anexa evidencias e finaliza o driver.
5. O Maven Surefire executa os testes e respeita a configuracao de paralelismo do `DataProvider`.

## Bibliotecas Utilizadas no POM

As principais bibliotecas declaradas no `pom.xml` sao:

- `org.seleniumhq.selenium:selenium-java:4.41.0`
Biblioteca principal para automacao de navegadores e interacao com a interface web.

- `io.cucumber:cucumber-java:7.15.0`
Implementacao do Cucumber para definicao e execucao de cenarios BDD em Java.

- `io.cucumber:cucumber-testng:7.15.0`
Integracao entre Cucumber e TestNG.

- `org.testng:testng:7.9.0`
Framework de execucao de testes usado como base para orquestracao e paralelismo.

- `io.github.bonigarcia:webdrivermanager:6.3.3`
Gerencia automaticamente os binarios dos navegadores, evitando configuracao manual de drivers.

- `io.qameta.allure:allure-testng:2.25.0`
Integracao do TestNG com Allure para geracao de evidencias e relatorios.

- `io.qameta.allure:allure-cucumber7-jvm:2.24.0`
Integracao do Cucumber com Allure.

- `org.slf4j:slf4j-api:2.0.7`
API de logging usada pela automacao.

- `ch.qos.logback:logback-classic:1.5.13`
Implementacao concreta de logging via SLF4J.

## Plugins Maven Utilizados

- `maven-compiler-plugin:3.8.1`
Compila o projeto com Java 17 e encoding UTF-8.

- `maven-surefire-plugin:3.2.5`
Executa os testes automatizados. Esta configurado com `parallel=tests` e com propriedade `dataproviderthreadcount` padrao igual a `2`.

- `maven-clean-plugin:3.2.0`
Remove artefatos anteriores, incluindo diretorios de resultados e relatorios Allure.

- `allure-maven:2.12.0`
Gera relatorios Allure na fase `verify`.

- `maven-javadoc-plugin:3.4.1`
Gera documentacao JavaDoc para o codigo de testes.

## Execucao Local

### Pre-requisitos locais

Para executar o projeto localmente, o ambiente deve possuir:

- Java 17
- Maven instalado e configurado no `PATH`
- Google Chrome ou Microsoft Edge instalados na maquina
- Conectividade com a internet para download de drivers pelo WebDriverManager
- Allure CLI instalado, caso voce queira abrir o relatorio localmente

### Comando base de execucao

```bash
mvn clean test -Dcucumber.filter.tags=@AGI -Dbrowser=chrome -Ddataproviderthreadcount=3
```

### Explicacao dos parametros

- `clean`
Limpa os artefatos da execucao anterior.

- `test`
Executa a suite automatizada.

- `-Dcucumber.filter.tags=@AGI`
Permite selecionar a TAG dos testes que serao executados.

- `-Dbrowser=chrome`
Define o navegador da execucao. No estado atual do projeto, os browsers suportados pela `DriverFactory` sao:
`chrome`
`edge`

- `-Ddataproviderthreadcount=3`
Define a quantidade de testes em paralelo no `DataProvider`.

- `-Dheadless=true` ou `-Dheadless=false`
Controla a execucao em modo headless. Para CI, recomenda-se `true`.

### Exemplos de execucao local

```bash
mvn clean test -Dcucumber.filter.tags=@AGI -Dbrowser=chrome -Ddataproviderthreadcount=2 -Dheadless=false
```

```bash
mvn clean test -Dcucumber.filter.tags=@negative -Dbrowser=edge -Ddataproviderthreadcount=1 -Dheadless=false
```

### Geracao local do relatorio Allure

Depois da execucao dos testes, os resultados ficam em:

```text
target/allure-results
```

Para abrir o relatorio localmente:

```bash
allure serve target/allure-results
```

Ou para gerar o HTML localmente:

```bash
allure generate target/allure-results --clean -o target/allure-report
```

### Geracao local do JavaDoc!

Para gerar a documentacao JavaDoc localmente:

```bash
mvn -DskipTests javadoc:javadoc
```

O resultado sera gerado em:

```text
target/apidocs
```

### Logs locais

Os logs da execucao sao gravados na pasta:

```text
logs/
```

Cada execucao gera um arquivo unico no formato:

```text
logs/test_yyyy-MM-dd_HH-mm-ss.log
```

## Execucao Local com Docker

O projeto possui uma stack Docker para rodar a automacao em Linux com sessao grafica virtual exposta por VNC e noVNC. Esse e o caminho mais simples para reproduzir localmente o mesmo modo usado no GitHub Actions hospedado.

Arquivos envolvidos:

- `Dockerfile`
- `docker-compose.yml`
- `docker/entrypoint.sh`

### Pre-requisitos

- Docker Desktop instalado
- Docker Compose habilitado

### Como executar

Na raiz do projeto:

```bash
docker compose up --build --abort-on-container-exit --exit-code-from automacao-gui
```

Exemplo 1, com valores padrao do `docker-compose.yml`:

```cmd
cd C:\Users\admin\IdeaProjects\demo_automacao1.1
docker compose up --build --abort-on-container-exit --exit-code-from automacao-gui
```

Exemplo 2, executando com `Edge` no `cmd`:

```cmd
cd C:\Users\admin\IdeaProjects\demo_automacao1.1
set SELENIUM_IMAGE=selenium/standalone-edge && set BROWSER=edge && set CUCUMBER_TAG=@AGI && set PARALLEL_COUNT=1 && set HEADLESS=false && docker compose up --build --abort-on-container-exit --exit-code-from automacao-gui
```

Esse comando cria a imagem, sobe a sessao grafica virtual e executa a suite com Maven dentro do container.

### Parametros padrao usados pelo container

O servico `automacao-gui` usa por padrao:

- `BROWSER=chrome`
- `CUCUMBER_TAG=@AGI`
- `PARALLEL_COUNT=1`
- `HEADLESS=false`
- `SCREEN_WIDTH=1920`
- `SCREEN_HEIGHT=1080`

Esses valores podem ser alterados diretamente no `docker-compose.yml` antes da execucao.

### Resultado e evidencias

Os volumes abaixo sao montados para persistir o resultado fora do container:

- `./target:/workspace/target`
- `./logs:/workspace/logs`

Depois da execucao, consulte:

- `target/allure-results`
- `target/surefire-reports`
- `logs/`

### Acesso a interface grafica

Durante a execucao, a sessao grafica pode ser acompanhada por:

- navegador: `http://localhost:6080`
- cliente VNC: `localhost:5900`

### Observacoes importantes

- O container suporta `chrome` e `edge`, desde que a imagem Selenium correspondente seja informada em `SELENIUM_IMAGE`.
- Para `chrome`, o valor padrao e `selenium/standalone-chrome`.
- Para `edge`, use `selenium/standalone-edge`.
- O workflow `tests-allure-pages.yml` grava um arquivo `.env` com `BROWSER`, `SELENIUM_IMAGE`, `CUCUMBER_TAG`, `PARALLEL_COUNT` e `HEADLESS` antes de chamar `docker compose`.
- A interface grafica no Docker e um display virtual Linux, nao uma sessao nativa do Windows.

## Execucao via GitHub Actions

O workflow principal do projeto esta em `.github/workflows/tests-allure-pages.yml`.

Ele cobre:

- execucao automatizada dos testes
- upload dos artefatos de teste
- geracao do relatorio Allure
- geracao do JavaDoc
- publicacao do conteudo no GitHub Pages

### Quando o workflow roda

O workflow e disparado:

- automaticamente em `push` para `main`
- automaticamente em `push` para `master`
- manualmente por `workflow_dispatch`

### Modos de execucao disponiveis

No disparo manual, o input `execution_mode` aceita:

- `github-hosted-docker-gui`
- `self-hosted-windows-gui`

#### `github-hosted-docker-gui`

Executa em `ubuntu-latest` usando a mesma stack Docker do projeto. O job roda:

```bash
docker compose up --build --abort-on-container-exit --exit-code-from automacao-gui
```

Inputs disponiveis no disparo manual:

- `cucumber_tag` com default `@AGI`
- `browser` com default `chrome`
- `parallel_count` com default `2`
- `execution_mode` com default `github-hosted-docker-gui`

Observacao: no job Docker hospedado, o workflow fixa `HEADLESS=false` e repassa `BROWSER`, `CUCUMBER_TAG` e `PARALLEL_COUNT` como variaveis de ambiente do container.

#### `self-hosted-windows-gui`

Executa em um runner com labels:

- `self-hosted`
- `windows`
- `win10-gui`

Esse modo existe para cenarios em que e necessario browser visivel em sessao grafica real do Windows.

Pre-requisitos do runner:

- Windows com sessao interativa ativa
- Java 17
- Maven no `PATH`
- Chrome e/ou Edge instalados
- acesso a internet para download de drivers

Comando executado nesse modo:

```bash
mvn clean test -DLOG_LEVEL=INFO -Dcucumber.filter.tags=@AGI -Dbrowser=chrome -Ddataproviderthreadcount=2 -Dscreen.width=1920 -Dscreen.height=1080 -Dheadless=false -Dmaven.clean.failOnError=false
```

No workflow real, os valores de `@AGI`, `chrome` e `2` sao substituidos pelos inputs recebidos no disparo manual.

### Como rodar manualmente no GitHub

1. Abra a aba `Actions` do repositorio.
2. Selecione `Tests And Allure Pages`.
3. Clique em `Run workflow`.
4. Preencha `cucumber_tag`, `browser`, `parallel_count` e `execution_mode`.
5. Execute o workflow.

### Artefatos gerados

Ao final dos jobs de teste, o pipeline publica:

- `allure-results`
- `surefire-reports`
- `execution-logs`

Esses artefatos ficam disponiveis na propria execucao do GitHub Actions.

## GitHub Pages

O job `deploy-pages` publica:

- `README.md` em `target/pages/README.md`
- relatorio Allure em `target/pages/reports/`
- JavaDoc em `target/pages/javadoc/`
- `index.html` na raiz da publicacao

### Como acessar

Depois que o workflow `Tests And Allure Pages` concluir com sucesso e o GitHub Pages estiver habilitado em `Settings > Pages` com `Source: GitHub Actions`, os links deste repositorio ficam assim:

- pagina inicial do GitHub Pages: `https://thiagosantana99.github.io/demo_automacao1.1/`
- README publicado: `https://thiagosantana99.github.io/demo_automacao1.1/README.md`
- relatorio Allure: `https://thiagosantana99.github.io/demo_automacao1.1/reports/`
- JavaDoc: `https://thiagosantana99.github.io/demo_automacao1.1/javadoc/`

Se preferir, voce tambem pode acessar o deploy mais recente pela aba `Actions`, abrindo a execucao do workflow e consultando o job `deploy-pages`.

### Observacao sobre a pagina inicial

O `index.html` gerado pelo workflow faz redirecionamento para o README hospedado no repositorio GitHub. Se quiser transformar a raiz do Pages em uma home propria do projeto, ajuste a etapa `Prepare GitHub Pages content` no workflow.

## Geracao do JavaDoc via Workflow

No job de publicacao do GitHub Pages, o workflow executa:

```bash
mvn -DskipTests javadoc:javadoc
```

Depois disso, o conteudo gerado em `target/apidocs` e copiado para:

```text
/javadoc/
```

no GitHub Pages.

## Paralelismo

O projeto suporta execucao paralela de duas formas:

- Via linha de comando
Use `-Ddataproviderthreadcount=QTDE` para informar quantos cenarios devem rodar em paralelo.

- Via configuracao padrao do projeto
No `pom.xml`, o plugin `maven-surefire-plugin` ja possui a propriedade `dataproviderthreadcount` configurada com valor padrao `2`. Se o parametro nao for informado na linha de comando, esse valor padrao sera utilizado.

Tambem existe configuracao paralela em `testng.xml`, com `parallel="tests"` e `thread-count="2"`, que pode ser ajustada conforme a estrategia de execucao desejada.

## BDD

Os cenarios existentes no arquivo `src/test/resources/features/buscaartigosblog.feature` cobrem os seguintes comportamentos:

- `Busca <Nome do Caso de Teste>`
Valida a busca positiva via teclado. O usuario informa um termo no campo de pesquisa, pressiona `Enter`, visualiza a lista de artigos e confirma que os resultados correspondem ao termo pesquisado.

- `por palavra-chave valida usando enter`
Verifica que a busca por `"pix"` retorna artigos relacionados quando a submissao ocorre com `Enter`.

- `e case insensitive`
Valida que a busca nao diferencia maiusculas e minusculas, retornando resultados tambem para `"PIX"`.

- `com espacos extras`
Valida que a busca continua funcionando quando o termo possui espacos no inicio e no fim, como `" pix "`.

- `Tentativa de Busca - <Nome do Caso de Teste>`
Valida o comportamento negativo da funcionalidade, garantindo que o sistema apresente a mensagem correta quando nao existirem resultados validos para o termo pesquisado.

- `Busca sem resultados`
Verifica que um termo inexistente, como `"xyzabc123"`, nao retorna artigos e exibe a mensagem de busca sem retorno.

- `Busca com caracteres especiais`
Verifica que a busca por `%$#@!` nao gera resultados indevidos e apresenta a mensagem adequada de nenhum resultado encontrado.

- `Busca com termo parcial`
Valida o comportamento quando o usuario informa um termo parcial acentuado, como `"emprés"`, e o sistema retorna a mensagem de nao encontrado.

- `Busca usando clique na lupa`
Verifica que a funcionalidade de busca tambem funciona quando a submissao e feita pelo icone da lupa, sem uso da tecla `Enter`.

- `Busca ignora acentuacao`
Valida que a busca trata equivalencia entre termos com e sem acento, retornando conteudos de `"empréstimo"` mesmo quando o usuario pesquisa por `"emprestimo"`.

## Estrutura Resumida

```text
src
  test
    java
      config
      factory
      hooks
      page
      runners
      steps
      utils
    resources
      features
pom.xml
testng.xml
.github/workflows/tests-allure-pages.yml
```

## Requisitos

Para executar o projeto corretamente, o ambiente deve possuir os seguintes requisitos instalados ou considerados:

- Java 17
- Maven
- Google Chrome ou Microsoft Edge instalados na maquina
- Allure CLI instalado para gerar e abrir relatorios localmente

### Bibliotecas utilizadas no projeto

- `org.seleniumhq.selenium:selenium-java:4.41.0`
- `io.cucumber:cucumber-java:7.15.0`
- `io.cucumber:cucumber-testng:7.15.0`
- `org.testng:testng:7.9.0`
- `io.github.bonigarcia:webdrivermanager:6.3.3`
- `io.qameta.allure:allure-testng:2.25.0`
- `io.qameta.allure:allure-cucumber7-jvm:2.24.0`
- `org.slf4j:slf4j-api:2.0.7`
- `ch.qos.logback:logback-classic:1.5.13`

### Plugins Maven utilizados

- `org.apache.maven.plugins:maven-compiler-plugin:3.8.1`
- `org.apache.maven.plugins:maven-surefire-plugin:3.2.5`
- `org.apache.maven.plugins:maven-clean-plugin:3.2.0`
- `io.qameta.allure:allure-maven:2.12.0`
- `org.apache.maven.plugins:maven-javadoc-plugin:3.4.1`

## Observacao

Como a criacao do driver depende do parametro `-Dbrowser`, recomenda-se manter os valores suportados pela `DriverFactory` para evitar fallback automatico para Chrome quando um valor invalido for informado.

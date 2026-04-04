# Projeto de Automacao de Testes

Este projeto automatiza testes do site XPTO com foco na validacao da funcionalidade de busca de artigos. A suite foi estruturada para executar cenarios BDD com Cucumber, navegacao web com Selenium WebDriver, orquestracao com TestNG e gerenciamento de build pelo Maven.

## Objetivo

O objetivo da automacao e validar o comportamento da busca de artigos no site, cobrindo cenarios positivos e negativos, variacoes de entrada do usuario e diferentes formas de disparar a pesquisa pela interface.

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

### Geracao local do JavaDoc

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

## Execucao em Container com sessao grafica

O projeto agora possui uma stack Docker para rodar a automacao em Linux com sessao grafica virtual exposta por VNC e noVNC. Isso permite acompanhar o browser em tempo real sem depender de area de trabalho local.

Arquivos adicionados:

- `Dockerfile`
- `docker-compose.yml`
- `docker/entrypoint.sh`

### Como funciona

O container sobe os seguintes componentes:

- `Xvfb` para criar o display virtual
- `Fluxbox` como window manager leve
- `x11vnc` para expor a sessao via VNC
- `noVNC` para acessar a sessao grafica pelo navegador
- Maven para executar a suite

### Pre-requisitos

- Docker Desktop instalado
- Docker Compose disponivel

### Subir a automacao no container

```bash
docker compose up --build
```

O comando sobe o ambiente grafico e executa:

```bash
mvn clean test -Dcucumber.filter.tags=@AGI -Dbrowser=chrome -Ddataproviderthreadcount=1 -Dscreen.width=1920 -Dscreen.height=1080 -Dheadless=false
```

### Acessar a sessao grafica

Depois de subir o container, abra no navegador:

```text
http://localhost:6080
```

Se preferir cliente VNC tradicional, conecte em:

```text
localhost:5900
```

### Parametros configuraveis

No `docker-compose.yml`, ajuste as variaveis conforme necessario:

- `BROWSER`
- `CUCUMBER_TAG`
- `PARALLEL_COUNT`
- `HEADLESS`
- `SCREEN_WIDTH`
- `SCREEN_HEIGHT`
- `CHROME_BIN`
- `CHROMEDRIVER_PATH`

### Observacoes importantes

- O modo recomendado no container e `chrome`.
- A `DriverFactory` aceita o binario do navegador por `CHROME_BIN` ou `CHROMIUM_BIN` e o driver por `CHROMEDRIVER_PATH`, o que permite usar Chromium no Linux do container sem depender apenas de download em tempo de execucao.
- Sessao grafica em container aqui significa display virtual acessivel por VNC/noVNC. Nao e uma sessao nativa do Windows.
- Para UI real em Windows 10, continue usando runner `self-hosted` com sessao logada.

## Execucao via GitHub Actions

O projeto possui um workflow versionado em [`.github/workflows/tests-allure-pages.yml`](/C:/Users/admin/IdeaProjects/demo_automacao/.github/workflows/tests-allure-pages.yml) para:

- executar os testes com Maven
- gerar o relatorio Allure
- gerar o JavaDoc
- publicar os artefatos de documentacao no GitHub Pages
- disponibilizar logs e relatorios como artefatos da execucao

### Pre-requisitos no GitHub

Para a execucao via GitHub Actions funcionar corretamente, o repositorio deve possuir:

- repositorio publicado no GitHub
- GitHub Actions habilitado em `Settings > Actions`
- GitHub Pages habilitado em `Settings > Pages`
- `Source: GitHub Actions` configurado em `Settings > Pages`
- permissao de escrita em Pages para o workflow

### Como o workflow executa

O workflow executa automaticamente em `push` nas branches:

- `main`
- `master`

Tambem pode ser executado manualmente pela aba `Actions`, usando `workflow_dispatch`.

### Parametros do workflow manual

Ao executar manualmente pelo GitHub Actions, o workflow permite informar:

- `cucumber_tag`
Define a TAG a ser executada. Exemplo: `@AGI`, `@E2E`, `@negative`.

- `browser`
Define o navegador da execucao. Exemplo: `chrome` ou `edge`.

- `parallel_count`
Define a quantidade de execucoes paralelas do `DataProvider`.

- `execution_mode`
Define o ambiente de execucao do workflow:
`github-hosted-docker-gui` para runner Linux hospedado executando a stack Docker com display virtual
`self-hosted-windows-gui` para runner Windows com sessao grafica ativa

### Comando executado no pipeline

O job de testes executa a suite com base neste comando:

```bash
docker compose up --build --abort-on-container-exit --exit-code-from automacao-gui
```

No CI hospedado pelo GitHub, a execucao usa a stack Docker do projeto, que sobe `Xvfb`, `Fluxbox`, `x11vnc` e `noVNC` dentro do container. No modo Windows com interface grafica real, a execucao depende de um runner `self-hosted`.

### Runner Windows 10 com sessao grafica

Se voce precisa executar o browser em uma sessao grafica real no Windows 10, o caminho suportado e usar um runner `self-hosted`. GitHub-hosted runners nao oferecem desktop interativo em Windows.

Pre-requisitos da maquina:

- Windows 10 com usuario local ou corporativo
- Sessao do Windows iniciada e mantida ativa durante a execucao
- Java 17 instalado
- Maven instalado e disponivel no `PATH`
- Google Chrome e/ou Microsoft Edge instalados
- Acesso a internet para o WebDriverManager baixar drivers

Passo a passo:

1. No repositorio GitHub, abra `Settings > Actions > Runners`.
2. Clique em `New self-hosted runner`.
3. Selecione `Windows`.
4. Siga os comandos fornecidos pelo GitHub na maquina Windows 10.
5. Adicione os labels `self-hosted`, `windows` e `win10-gui` ao runner.
6. Mantenha a maquina logada com a area de trabalho ativa durante a execucao dos testes.

Observacoes importantes:

- Evite bloquear a sessao com `Win + L`, pois isso costuma quebrar automacoes com browser visivel.
- Se a maquina entrar em hibernacao, suspensao ou screensaver agressivo, a execucao pode falhar.
- Para browser maximizado de forma consistente, o workflow envia `-Dscreen.width=1920` e `-Dscreen.height=1080`, e a `DriverFactory` ainda chama `maximize()`.

Como disparar no GitHub Actions:

1. Abra `Actions`.
2. Selecione o workflow `Tests And Allure Pages`.
3. Clique em `Run workflow`.
4. Preencha:
`execution_mode=self-hosted-windows-gui`
`browser=chrome` ou `browser=edge`
`cucumber_tag=@AGI`
`parallel_count=1` ou outro valor adequado
5. Execute o workflow.

Recomendacao pratica:

- Para validacao funcional e execucao barata, use `github-hosted-docker-gui`.
- Para casos que realmente exigem janela visivel, foco, rendering grafico ou comportamento especifico de Windows, use `self-hosted-windows-gui`.

### Artefatos publicados pelo GitHub Actions

Ao final da execucao, o workflow publica:

- `allure-results`
- `surefire-reports`
- `execution-logs`

Os logs podem ser acessados assim:

1. Abra a aba `Actions`
2. Selecione a execucao desejada
3. Role ate a secao `Artifacts`
4. Baixe o artefato `execution-logs`

## GitHub Pages

O workflow prepara o conteudo do GitHub Pages com a seguinte estrutura:

- `README.md` na raiz publicada
- relatorio Allure em `/report/`
- JavaDoc em `/javadoc/`

### Links de acesso no GitHub Pages

Substitua `SEU-USUARIO` e `SEU-REPO` pelos valores reais do repositorio:

- Arquivo README publicado:
  `https://SEU-USUARIO.github.io/SEU-REPO/README.md`

- Relatorio Allure:
  `https://SEU-USUARIO.github.io/SEU-REPO/report/`

- JavaDoc:
  `https://SEU-USUARIO.github.io/SEU-REPO/javadoc/`

Observacao importante:
- a raiz `https://SEU-USUARIO.github.io/SEU-REPO/` publica o arquivo `README.md`, mas o GitHub Pages nao o renderiza automaticamente como pagina inicial
- para uma home navegavel na raiz, seria necessario publicar um `index.html`

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

Tambem existe configuracao paralela em [`testng.xml`](/C:/Users/admin/IdeaProjects/demo_automacao/testng.xml), com `parallel="tests"` e `thread-count="2"`, que pode ser ajustada conforme a estrategia de execucao desejada.

## BDD

Os cenarios existentes no arquivo [`buscaartigosblog.feature`](/C:/Users/admin/IdeaProjects/demo_automacao/src/test/resources/features/buscaartigosblog.feature) cobrem os seguintes comportamentos:

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

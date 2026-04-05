package page;

import io.qameta.allure.Allure;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Article;
import utils.ScreenshotUtil;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe BlogSearchPage para representar a pagina de resultados de busca do blog.
 * <p>
 * Responsabilidades:
 * <p>
 * - Validar o carregamento da pagina de resultados;
 * <p>
 * - Localizar e consolidar os artigos retornados na busca;
 * <p>
 * - Extrair titulo, link e data dos artigos encontrados;
 * <p>
 * - Registrar evidencias e anexos no relatorio Allure.
 * <p>
 * @author Thiago Santana
 * @version 1.0
 */
public class BlogSearchPage extends BasePage {

    private static final Logger logger = LoggerFactory.getLogger(BlogSearchPage.class);

    private static final String ARTICLES_PARENT_XPATH = "//*[@id='main']//article";
    private static final String ARTICLE_TITLE_XPATH = "//*[@class=\"entry-title ast-blog-single-element\"]//a[@rel=\"bookmark\"]";
    private static final String ARTICLE_DATE_XPATH = "//*[@class=\"published\"]";

    @FindBy(xpath = "//*[@id=\"primary\"]/section/h1")
    private WebElement searchTitle;

    @FindBy(xpath = "//*[@id=\"main\"]")
    private WebElement searchResult;

    public BlogSearchPage() {
        PageFactory.initElements(driver, this);
    }

    /**
     * Obtem o titulo exibido na pagina de resultados da busca.
     * <p>
     * Responsabilidades:
     * <p>
     * - Posicionar a tela no titulo da pagina;
     * <p>
     * - Registrar evidencia visual do titulo;
     * <p>
     * - Retornar o texto visivel ao usuario.
     * <p>
     * <p>
     * @return titulo exibido na pagina de resultados
     */
    public String getSearchTitle() {
        helper.scrollIntoView(searchTitle);
        helper.scrollToElementPic(searchTitle, "Titulo Pagina Pesquisa");
        return searchTitle.getText();
    }

    /**
     * Localiza os artigos retornados para o termo pesquisado e converte o resultado
     * em objetos de dominio.
     * <p>
     * Responsabilidades:
     * <p>
     * - Validar se a pagina de resultados foi carregada corretamente;
     * <p>
     * - Localizar os artigos exibidos no resultado da busca;
     * <p>
     * - Extrair os dados principais de cada artigo;
     * <p>
     * - Anexar evidencias de cada item processado no Allure.
     * <p>     *
     * @param termoBusca termo informado pelo usuario para realizar a pesquisa
     * @return lista de artigos encontrados na pagina de resultados
     * @throws RuntimeException quando ocorrer falha na URL, na localizacao dos artigos
     * ou na localizacao do titulo de um artigo
     */
    public List<Article> findResults(String termoBusca) {
        try {
            validateSearchResultsPageLoaded(termoBusca);
            helper.scrollIntoView(searchTitle);
            helper.reloadPage();
            helper.waitForPageLoaded();
            helper.scrollToBottom();
            helper.scrollIntoView(searchTitle);
            helper.scrollToTop();
            helper.scrollToBottom();
            helper.waitForPageLoaded();
            helper.scrollToTop();
            helper.waitForPageLoaded();
            List<WebElement> articles = getArticlesOrThrow();
            List<Article> articlesList = new ArrayList<>();
            ScreenshotUtil.Esperar(500);
            ScreenshotUtil.attachScreenshot("Evidencia Resultado Pesquisa");
            Allure.step("Artigos Encontrados: Total de Artigos = " + articles.size());

            for (int index = 0; index < articles.size(); index++) {
                WebElement article = articles.get(index);
                int articleNumber = index + 1;

                helper.scrollToElement(article);

                WebElement titleElement = getTitleElementOrThrow(article, articleNumber);
                String title = titleElement.getText();
                String link = titleElement.getAttribute("href");
                String date = getDateText(article);

                articlesList.add(new Article(title, link, date));
                attachArticleEvidence(article, articleNumber, title, date, link);
            }

            helper.scrollIntoView(searchTitle);
            return articlesList;
        } catch (RuntimeException exception) {
            logger.error("Falha de negocio ao processar os resultados da busca para o termo: {}", termoBusca, exception);
            throw exception;
        } catch (Exception exception) {
            logger.error("Erro inesperado ao processar os resultados da busca para o termo: {}", termoBusca, exception);
            throw new RuntimeException("Erro ao processar os resultados da busca para o termo: " + termoBusca, exception);
        }
    }

    /**
     * Valida se a pagina atual corresponde ao resultado do termo pesquisado.
     * <p>
     * Responsabilidades:
     * <p>
     * - Obter a URL corrente da aplicacao;
     * <p>
     * - Normalizar o termo pesquisado;
     * <p>
     * - Garantir que a URL contenha o termo utilizado na busca.
     * <p>
     * @param termoBusca termo informado pelo usuario para a pesquisa
     * @throws IllegalStateException quando a URL atual nao contem o termo pesquisado
     * @throws RuntimeException quando ocorrer falha inesperada durante a validacao
     */
    private void validateSearchResultsPageLoaded(String termoBusca) {
        try {
            String currentUrl = driver.getCurrentUrl();
            String decodedCurrentUrl = currentUrl == null
                    ? ""
                    : URLDecoder.decode(currentUrl, StandardCharsets.UTF_8);
            String normalizedSearchTerm = termoBusca == null ? "" : termoBusca.trim();

            if (!decodedCurrentUrl.contains(normalizedSearchTerm)) {
                throw new IllegalStateException(
                        "Pagina nao carregada corretamente. A URL atual nao contem o termo pesquisado: " + normalizedSearchTerm
                );
            }
        } catch (IllegalStateException exception) {
            logger.error("URL de resultados invalida para o termo pesquisado: {}", termoBusca, exception);
            throw exception;
        } catch (Exception exception) {
            logger.error("Falha ao validar o carregamento da pagina de resultados.", exception);
            throw new RuntimeException("Falha ao validar o carregamento da pagina de resultados.", exception);
        }
    }

    /**
     * Localiza os artigos exibidos na pagina de resultados.
     * <p>
     * Responsabilidades:
     * <p>
     * - Buscar todos os containers de artigo na area principal;
     * <p>
     * - Validar que a lista retornada nao esta vazia;
     * <p>
     * - Retornar os elementos para processamento posterior.
     * <p>
     * @return lista de elementos representando os artigos encontrados
     * @throws IllegalStateException quando nenhum artigo for encontrado
     * @throws RuntimeException quando ocorrer falha inesperada na busca dos elementos
     */
    private List<WebElement> getArticlesOrThrow() {
        try {
            List<WebElement> articles = helper.findAll(By.xpath(ARTICLES_PARENT_XPATH));
            if (articles == null || articles.isEmpty()) {
                throw new IllegalStateException("Nenhum artigo foi encontrado na pagina de resultados.");
            }
            return articles;
        } catch (IllegalStateException exception) {
            logger.error("Nenhum artigo foi encontrado na pagina de resultados.", exception);
            throw exception;
        } catch (Exception exception) {
            logger.error("Falha ao localizar os artigos na pagina de resultados.", exception);
            throw new RuntimeException("Falha ao localizar os artigos na pagina de resultados.", exception);
        }
    }

    /**
     * Localiza o titulo de um artigo especifico dentro do card processado.
     * <p>
     * Responsabilidades:
     * <p>
     * - Buscar os elementos de titulo dentro do artigo atual;
     * <p>
     * - Validar que ao menos um titulo foi encontrado;
     * <p>
     * - Retornar o primeiro titulo valido para leitura dos dados.
     * <p>
     * @param article elemento pai que representa o artigo atual
     * @param articleNumber indice logico do artigo na listagem
     * @return elemento de titulo do artigo
     * @throws IllegalStateException quando nenhum titulo for encontrado
     * @throws RuntimeException quando ocorrer falha inesperada na localizacao do titulo
     */
    private WebElement getTitleElementOrThrow(WebElement article, int articleNumber) {
        try {
            List<WebElement> titleElements = helper.findChildElements(article, By.xpath(ARTICLE_TITLE_XPATH));
            if (titleElements == null || titleElements.isEmpty()) {
                throw new IllegalStateException("Titulo nao encontrado para o artigo de indice " + articleNumber + ".");
            }
            return titleElements.get(0);
        } catch (IllegalStateException exception) {
            logger.error("Titulo nao encontrado para o artigo de indice {}.", articleNumber, exception);
            throw exception;
        } catch (Exception exception) {
            logger.error("Falha ao localizar o titulo do artigo de indice {}.", articleNumber, exception);
            throw new RuntimeException("Falha ao localizar o titulo do artigo de indice " + articleNumber + ".", exception);
        }
    }

    /**
     * Obtem o texto da data publicada para um artigo.
     * <p>
     * Responsabilidades:
     * <p>
     * - Buscar os elementos de data dentro do artigo;
     * <p>
     * - Retornar o texto da primeira data encontrada;
     * <p>
     * - Devolver string vazia quando o campo nao estiver disponivel.
     * <p>
     * @param article elemento pai que representa o artigo atual
     * @return texto da data do artigo ou string vazia quando inexistente
     */
    private String getDateText(WebElement article) {
        List<WebElement> dateElements = helper.findChildElements(article, By.xpath(ARTICLE_DATE_XPATH));
        if (dateElements == null || dateElements.isEmpty()) {
            return "";
        }
        return dateElements.get(0).getText();
    }

    /**
     * Verifica se algum resultado contem o termo informado.
     *
     * @param term termo esperado nos resultados
     * @return true quando algum resultado contiver o termo
     */
    public boolean resultsContainTerm(String term) {
        helper.moveScreenUI();
        List<WebElement> elements = driver.findElements(By.xpath(ARTICLE_TITLE_XPATH));
        ScreenshotUtil.Esperar(500);
        ScreenshotUtil.attachScreenshot("Resultado da Busca");
        for (WebElement el : elements) {
            if (el.getText().toLowerCase().contains(term.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Registra evidencias do artigo processado no relatorio Allure.
     * <p>
     * Responsabilidades:
     * <p>
     * - Criar um passo descritivo para o artigo atual;
     * <p>
     * - Anexar titulo, data e link ao relatorio;
     * <p>
     * - Capturar screenshot do artigo exibido na tela.
     * <p>
     * @param article elemento visual do artigo corrente
     * @param articleNumber indice logico do artigo na listagem
     * @param title titulo extraido do artigo
     * @param date data extraida do artigo
     * @param link link extraido do artigo
     */
    private void attachArticleEvidence(WebElement article, int articleNumber, String title, String date, String link) {
        Allure.step("Artigo -> " + articleNumber + " -> " + title, () -> {
            Allure.addAttachment("Titulo", title);
            Allure.addAttachment("Data", date);
            Allure.addAttachment("Link", link);
            helper.scrollToElementPic(article, "Artigo " + articleNumber + " " + title);
        });
    }

}

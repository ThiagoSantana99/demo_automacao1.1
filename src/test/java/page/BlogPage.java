package page;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import utils.ScreenshotUtil;

import java.util.List;

/**
 * Classe BlogPage para representar a pagina principal do blog.
 * <p>
 * Responsabilidades:
 * <p>
 * - Mapear os elementos de busca disponiveis na pagina inicial;
 * <p>
 * - Realizar interacoes com campo e icone de pesquisa;
 * <p>
 * - Abrir a home do blog e registrar evidencias da navegacao;
 * <p>
 * - Validar resultados e mensagens retornadas pela busca.
 * <p>
 * @author Thiago Santana
 * @version 1.0
 */
public class BlogPage extends BasePage {

    private static final Logger logger = LoggerFactory.getLogger(BlogPage.class);

    private final By results = By.cssSelector("article");
    private final By searchInputLocator = By.cssSelector("#search-field");

    @FindBy(css = "#search-field")
    private WebElement searchInput;

    @FindBy(name = "submit")
    private WebElement submitBtn;

    // Seletor do container do carousel
    By carouselContainer = By.xpath("//*[@id='main']//*[@id='post-4102']//*[@class=\"slick-track\"]");
    // Seletor dos slides internos
    By carouselSlides = By.xpath("//*[@id='main']//*[@id='post-4102']//*[@class=\"slick-track\"]//*[@class=\"slick-slide\"]");

    @FindBy(css = ".slick-slider")
    private WebElement postCarousel;

    @FindBy(css = "slick-initialized")
    private WebElement postCarouselLoaded;

    @FindBy(css = "uagb-post__title uagb-post__text")
    private WebElement carroceu;

    @FindBy(xpath = "//*[contains(@class,'ast-search-menu-icon') and contains(@class,'slide-search')]")
    private WebElement searchIcon;

    @FindBy(xpath = "//*[@id=\"astra-footer-menu\"]/li[1]/a")
    private WebElement agiLink;

    @FindBy(css = ".no-results")
    private WebElement emptyMessage;


    private static final String CAROUSEL_PARENT_XPATH = "//*[@id=\"post-4102\"]/div/nav/div";
    private static final String HOMEPAGE = "https://blog.agibank.com.br/";

    /**
     * Inicializa os elementos mapeados da pagina inicial do blog.
     */
    public BlogPage() {
        PageFactory.initElements(driver, this);
    }

    /**
     * Clica no icone de busca da pagina inicial.
     */
    public void clickSearchIcon() {
        helper.click(searchIcon);
        helper.waitForVisibility(searchInputLocator);
    }

    /**
     * Executa duplo clique no icone de busca e registra evidencia.
     *
     * @param nomeEvid nome da evidencia a ser anexada
     */
    public void doubleclickSearchIcon(String nomeEvid) {
        helper.doubleClick(searchIcon, nomeEvid);
    }

    /**
     * Valida se o elemento de destaque da pagina esta habilitado.
     *
     * @return true quando o elemento estiver visivel e habilitado
     */
    public boolean getcarroceu() {
        return helper.waitForCarouselReady(carouselContainer, carouselSlides);
    }

    /**
     * Localiza os artigos exibidos na pagina incial.
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
            List<WebElement> articles = helper.findAll(By.xpath(CAROUSEL_PARENT_XPATH));
            if (articles == null || articles.isEmpty()) {
                throw new IllegalStateException("Nenhum artigo foi encontrado na pagina principal.");
            }
            return articles;
        } catch (IllegalStateException exception) {
            logger.error("Nenhum artigo foi encontrado na pagina principal.", exception);
            throw exception;
        } catch (Exception exception) {
            logger.error("Falha ao localizar os artigos na pagina principal.", exception);
            throw new RuntimeException("Falha ao localizar os artigos na pagina principal.", exception);
        }
    }

    /**
     * Abre a home do blog e registra evidencia de carregamento.
     */
    public void openHomePage() {
        driver.get(HOMEPAGE);
        helper.waitForFullLoad();
        ScreenshotUtil.Esperar(300);
        ScreenshotUtil.attachScreenshot("Pagina Carregada com Sucesso");
    }

    /**
     * Submete a busca usando a tecla Enter no campo de pesquisa.
     */
    public void submitWithEnter() {
        helper.waitForVisibility(searchInputLocator);
        helper.pressEnter(helper.waitForVisibility(searchInputLocator));
        ScreenshotUtil.attachScreenshot("Pesquisando");
    }

    /**
     * Informa se existem elementos de resultado na pagina.
     *
     * @return true quando existir ao menos um elemento mapeado pelo locator
     */
    public boolean hasResults() {
        return results.hashCode() > 0;
    }

    /**
     * Verifica se algum resultado contem o termo informado.
     *
     * @param term termo esperado nos resultados
     * @return true quando algum resultado contiver o termo
     */
    public boolean resultsContainTerm(String term) {
        List<WebElement> elements = driver.findElements(results);
        ScreenshotUtil.attachScreenshot("Resultado da Busca");
        for (WebElement el : elements) {
            if (el.getText().toLowerCase().contains(term.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Compara o resultado da busca com o termo informado.
     *
     * @param term termo esperado nos resultados
     * @return true quando algum resultado contiver o termo
     */
    public boolean compareResultsWith(String term) {
        return resultsContainTerm(term);
    }

    /**
     * Obtem a mensagem exibida quando a busca nao retorna resultados.
     *
     * @return mensagem de busca sem resultados
     */
    public String getEmptyMessage() {
        helper.waitForFullLoad();
        helper.scrollIntoView(searchIcon);
        ScreenshotUtil.Esperar(300);
        ScreenshotUtil.attachScreenshot("Evidencia Sem Resultado na Busca");
        return emptyMessage.getText();
    }

    /**
     * Preenche o campo de busca com o texto informado.
     *
     * @param searchText texto a ser pesquisado
     */
    public void enterSearch(String searchText) {
        helper.click(searchIcon);
        helper.waitForVisibility(searchInputLocator);
        helper.scrollIntoView(searchInputLocator);
        helper.type(searchInputLocator, searchText);
        ScreenshotUtil.Esperar(300);
        ScreenshotUtil.attachScreenshot("Realizando Busca do termo: " + searchText);
    }
}

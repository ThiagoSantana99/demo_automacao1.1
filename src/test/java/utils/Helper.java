package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Classe Helper para reunir utilitarios de interacao e espera com Selenium.
 * <p>
 * Responsabilidades:
 * <p>
 * - Localizar elementos e colecoes na pagina;
 * <p>
 * - Executar acoes de clique, digitacao e teclado;
 * <p>
 * - Realizar esperas explicitas de carregamento e visibilidade;
 * <p>
 * - Apoiar rolagem, evidencias e validacoes auxiliares.
 * <p>
 * @author Thiago Santana
 * @version 1.0
 */
public class Helper {

    private static final Logger logger = LoggerFactory.getLogger(Helper.class);

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final Actions actions;

    /**
     * Inicializa os recursos utilitarios usados pelas paginas.
     *
     * @param driver instancia ativa do WebDriver
     * @param wait instancia de espera explicita
     * @param actions instancia de acoes do Selenium
     */
    public Helper(WebDriver driver, WebDriverWait wait, Actions actions) {
        this.driver = driver;
        this.wait = wait;
        this.actions = actions;
    }

    /**
     * Localiza um elemento individual pela estrategia informada.
     *
     * @param locator localizador do elemento
     * @return elemento encontrado na pagina
     */
    public WebElement find(By locator) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    /**
     * Localiza todos os elementos associados ao localizador informado.
     *
     * @param locator localizador da colecao de elementos
     * @return lista de elementos encontrados
     */
    public List<WebElement> findAll(By locator) {
        wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        return driver.findElements(locator);
    }

    /**
     * Localiza elementos filhos dentro de um elemento pai.
     *
     * @param parent elemento pai para a busca
     * @param childLocator localizador do elemento filho
     * @return lista de elementos filhos encontrados
     */
    public List<WebElement> findChildElements(WebElement parent, By childLocator) {
        return parent.findElements(childLocator);
    }

    /**
     * Verifica a visibilidade de um elemento localizado por By.
     *
     * @param locator localizador do elemento
     * @return true quando o elemento estiver visivel
     */
    public boolean isVisible(By locator) {
        try {
            WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            return el.isDisplayed();
        } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
            logger.error("Falha ao validar visibilidade do elemento localizado por By.", e);
            return false;
        }
    }

    /**
     * Verifica a visibilidade de um WebElement ja mapeado.
     *
     * @param element elemento a ser validado
     * @return true quando o elemento estiver visivel
     */
    public boolean isVisible(WebElement element) {
        try {

            wait.until(ExpectedConditions.visibilityOf(element));
            return element.isDisplayed();



        } catch (TimeoutException | StaleElementReferenceException e) {
            logger.error("Falha ao validar visibilidade do elemento informado.", e);
            return false;
        }
    }

    /**
     * Espera o carousel estar carregado e clicável
     * @param carouselSelector Seletor do container do carousel
     * @param slideSelector Seletor dos slides dentro do carousel
     * @return true se o carousel estiver pronto
     */
    public boolean waitForCarouselReady(By carouselSelector, By slideSelector) {
        waitForPageLoaded();
        scrollToElement(find(carouselSelector));
        // Espera container existir
        WebElement carousel = wait.until(
                ExpectedConditions.presenceOfElementLocated(carouselSelector)
        );

        // Espera pelo menos 1 slide carregado
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(slideSelector, 0));

        // Espera que o primeiro slide esteja visível
        List<WebElement> slides = driver.findElements(slideSelector);
        wait.until(ExpectedConditions.visibilityOf(slides.get(0)));

        // 4Espera o carousel estar clicável (opcional, se precisar interagir)
        wait.until(ExpectedConditions.elementToBeClickable(slides.get(0)));

        // Verifica se o primeiro slide realmente está na tela
        return slides.get(0).isDisplayed();
    }


    /**
     * Verifica se existe ao menos um elemento para o locator informado.
     *
     * @param locator localizador do elemento
     * @return true quando houver elementos encontrados
     */
    public boolean exists(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    /**
     * Executa clique fora de componentes ativos para fechar overlays ou focos.
     */
    public void clickOutside() {
        try {
            driver.findElement(By.tagName("body")).click();
        } catch (Exception e) {
            logger.error("Falha ao clicar no body. Executando fallback via JavaScript.", e);
            ((JavascriptExecutor) driver).executeScript("document.body.click();");
        }
    }

    /**
     * Realiza duplo clique resiliente, com fallback em JavaScript.
     *
     * @param element elemento alvo do duplo clique
     * @param nomeEvid nome da evidencia a ser registrada
     */
    public void doubleClick(WebElement element, String nomeEvid) {
        try {
            clickOutside();
            scrollIntoView(element);
            actions.moveToElement(element)
                    .pause(Duration.ofMillis(200))
                    .doubleClick()
                    .perform();
            ScreenshotUtil.attachScreenshot("Evidencia-> " + nomeEvid);
        } catch (Exception e1) {
            logger.error("Falha no double click padrao. Executando fallback via JavaScript.", e1);
            try {
                clickOutside();
                scrollIntoView(element);
                jsDoubleClick(element);
                ScreenshotUtil.attachScreenshot("EvidenciaJS-> " + nomeEvid);
            } catch (Exception e2) {
                logger.error("Falha ao executar double click via JavaScript.", e2);
                throw new RuntimeException("Falha ao executar double click", e2);
            }
        }
    }

    /**
     * Realiza clique simples resiliente, com fallback em JavaScript.
     *
     * @param element elemento alvo do clique
     */
    public void click(WebElement element) {
        try {
            waitUntilClickable(element);
            scrollIntoView(element);
            element.click();
        } catch (Exception e1) {
            logger.error("Falha no clique padrao. Executando fallback via JavaScript.", e1);
            try {
                jsClick(element);
            } catch (Exception e2) {
                logger.error("Falha ao executar clique via JavaScript.", e2);
                throw new RuntimeException("Falha ao clicar no elemento", e2);
            }
        }
    }

    /**
     * Aguarda o elemento tornar-se clicavel.
     *
     * @param element elemento a ser aguardado
     */
    private void waitUntilClickable(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    /**
     * Move a visualizacao para o elemento informado.
     *
     * @param element elemento alvo do scroll
     */
    public void scrollIntoView(WebElement element) {
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
    }

    /**
     * Executa clique via JavaScript.
     *
     * @param element elemento alvo do clique
     */
    public void jsClick(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    /**
     * Executa duplo clique via JavaScript.
     *
     * @param element elemento alvo do duplo clique
     */
    private void jsDoubleClick(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
                "var evt = new MouseEvent('dblclick', {bubbles: true, cancelable: true}); arguments[0].dispatchEvent(evt);",
                element
        );
    }

    /**
     * Verifica se um elemento esta clicavel.
     *
     * @param locator localizador do elemento
     * @return true quando o elemento estiver clicavel
     */
    public boolean isClickable(By locator) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(locator));
            return true;
        } catch (Exception e) {
            logger.error("Falha ao validar se o elemento esta clicavel.", e);
            return false;
        }
    }

    /**
     * Executa duplo clique em fluxo alternativo de UI.
     *
     * @param element elemento alvo do duplo clique
     * @param nomeEvid nome da evidencia a ser registrada
     */
    public void doubleclickUI(WebElement element, String nomeEvid) {
        waitForFullLoad();
        WebElement body = driver.findElement(By.tagName("body"));
        body.click();
        ScreenshotUtil.attachScreenshot("Evidencia-> " + nomeEvid);
        actions.moveToElement(element)
                .pause(Duration.ofMillis(200))
                .doubleClick()
                .perform();
        waitForFullLoad();
        body = driver.findElement(By.tagName("body"));
        body.click();
    }

    /**
     * Digita um texto em elemento localizado por By.
     *
     * @param locator localizador do campo
     * @param text texto a ser informado
     */
    public void type(By locator, String text) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        el.clear();
        el.sendKeys(text);
    }

    /**
     * Digita um texto em elemento ja mapeado.
     *
     * @param element campo alvo da digitacao
     * @param text texto a ser informado
     */
    public void type(WebElement element, String text) {
        wait.until(ExpectedConditions.visibilityOf(element));
        element.clear();
        element.sendKeys(text);
    }

    /**
     * Envia a tecla Enter para o elemento informado.
     *
     * @param element elemento que recebera a tecla
     */
    public void pressEnter(WebElement element) {
        element.sendKeys(Keys.ENTER);
    }

    /**
     * Obtem o texto de um elemento localizado por By.
     *
     * @param locator localizador do elemento
     * @return texto do elemento encontrado
     */
    public String getText(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getText();
    }

    /**
     * Obtem o texto de um WebElement ja mapeado.
     *
     * @param element elemento a ser lido
     * @return texto do elemento
     */
    public String getText(WebElement element) {
        wait.until(ExpectedConditions.visibilityOf(element));
        return element.getText();
    }

    /**
     * Obtem o valor de um atributo de elemento localizado por By.
     *
     * @param locator localizador do elemento
     * @param attribute nome do atributo desejado
     * @return valor do atributo informado
     */
    public String getAttribute(By locator, String attribute) {
        return find(locator).getAttribute(attribute);
    }

    /**
     * Aguarda o carregamento completo da pagina.
     *
     * @return true quando o documento estiver em estado complete
     */
    public boolean waitForPageLoaded() {
        try {
            ExpectedCondition<Boolean> pageLoadCondition = currentDriver -> {
                assert currentDriver != null;
                return ((JavascriptExecutor) currentDriver)
                        .executeScript("return document.readyState")
                        .equals("complete");
            };

            return wait.until(pageLoadCondition);
        } catch (TimeoutException e) {
            logger.error("Timeout ao aguardar carregamento completo da pagina.", e);
            return false;
        }
    }

    /**
     * Aguarda a finalizacao das requisicoes Ajax, quando houver jQuery.
     *
     * @return true quando nao houver requisicoes Ajax pendentes
     */
    public boolean waitForAjaxComplete() {
        try {
            ExpectedCondition<Boolean> ajaxCondition = currentDriver -> {
                assert currentDriver != null;
                return (Boolean) ((JavascriptExecutor) currentDriver)
                        .executeScript("return window.jQuery != undefined && jQuery.active === 0");
            };

            return wait.until(ajaxCondition);
        } catch (Exception e) {
            logger.error("Falha ao aguardar finalizacao das requisicoes Ajax. Fluxo seguira adiante.", e);
            return true;
        }
    }

    public boolean waitForDocumentLoaded() {
        try {
    wait.until(driver ->
            ((JavascriptExecutor) driver)
            .executeScript("return document.readyState")
        .equals("complete"));
        } catch (TimeoutException e) {
            logger.error("Timeout ao aguardar document.readyState = complete.", e);
            return false;
        }
        return true;
    }

    /**
     * Aguarda o carregamento completo da pagina e de requisicoes Ajax.
     */
    public void waitForFullLoad() {
        waitForPageLoaded();
//        waitForAjaxComplete();
        waitForDocumentLoaded();
    }

    /**
     * Move a tela ate o elemento utilizando scroll tradicional.
     *
     * @param element elemento alvo do scroll
     */
    public void scrollToElement(WebElement element) {
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView(true);", element);
    }

    /**
     * Move a tela ate o elemento e registra screenshot.
     *
     * @param element elemento alvo do scroll
     * @param nomeEvid nome da evidencia a ser registrada
     */
    public void scrollToElementPic(WebElement element, String nomeEvid) {
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView(true);", element);
        ScreenshotUtil.Esperar(100);
        ScreenshotUtil.attachScreenshot("Evidencia-> " + nomeEvid);
    }

    /**
     * Aguarda a invisibilidade do elemento localizado.
     *
     * @param locator localizador do elemento
     */
    public void waitForInvisibility(By locator) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    /**
     * Aguarda a presenca de um texto em elemento localizado.
     *
     * @param locator localizador do elemento
     * @param text texto esperado
     */
    public void waitForText(By locator, String text) {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    /**
     * Conta a quantidade de elementos encontrados para o locator informado.
     *
     * @param locator localizador da colecao
     * @return quantidade de elementos encontrados
     */
    public int count(By locator) {
        return driver.findElements(locator).size();
    }

    /**
     * Executa scroll de ida e volta na interface.
     */
    public void moveScreenUI() {
        actions.scrollByAmount(0, 1000).perform();
        actions.scrollByAmount(0, -1000).perform();
    }

    /**
     * Executa scroll com captura de evidencia da tela.
     */
    public void moveScreenPicUI() {
        actions.scrollByAmount(0, 1000).perform();
        ScreenshotUtil.Esperar(200);
        ScreenshotUtil.attachScreenshot("Pagina Carregada com Sucesso");
        actions.scrollByAmount(0, -1000).perform();
    }

    /**
     * Move a tela para o topo da pagina.
     */
    public void moveScreenTopUI() {
        actions.scrollByAmount(0, 0).perform();
    }

    /**
     * Move a tela para o fim da pagina.
     */
    public void scrollToBottom() {
        Actions action = new Actions(driver);
        // Pressiona CTRL, END e depois solta as teclas
        action.keyDown(Keys.CONTROL).sendKeys(Keys.END).keyUp(Keys.CONTROL).perform();
    }

    /**
     * Move a tela para o topo da pagina.
     */
    public void scrollToTop() {
        Actions action = new Actions(driver);
        // Pressiona CTRL, HOME e depois solta as teclas
        action.keyDown(Keys.CONTROL).sendKeys(Keys.HOME).keyUp(Keys.CONTROL).perform();
    }

}

package factory;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

/**
 * Classe DriverFactory para criar instancias de WebDriver conforme o navegador informado.
 * <p>
 * Responsabilidades:
 * <p>
 * - Configurar automaticamente os drivers dos navegadores suportados;
 * <p>
 * - Instanciar WebDriver para Chrome e Edge;
 * <p>
 * - Aplicar opcoes de inicializacao e modo headless quando necessario;
 * <p>
 * - Retornar o navegador pronto para execucao dos testes.
 * <p>
 * @author Thiago Santana
 * @version 1.0
 */
public class DriverFactory {

    private static final int DEFAULT_SCREEN_WIDTH = 1980;
    private static final int DEFAULT_SCREEN_HEIGHT = 1024;

    /**
     * Enum Browser para representar os navegadores suportados pela automacao.
     */
    public enum Browser {
        CHROME, EDGE
    }

    /**
     * Define se a execucao deve ocorrer em modo headless.
     * <p>
     * Responsabilidades:
     * - Ler a propriedade de sistema `headless`;
     * - Considerar a variavel de ambiente `CI`;
     * - Retornar o comportamento apropriado para execucao local ou em pipeline.
     *
     * @return true quando a execucao deve ser headless
     */
    private static boolean isHeadlessEnabled() {
        String headlessProperty = System.getProperty("headless", "true");
        String ciEnvironment = System.getenv("CI");
        return Boolean.parseBoolean(headlessProperty) || "true".equalsIgnoreCase(ciEnvironment);
    }

    /**
     * Recupera a largura configurada para a janela do browser.
     *
     * @return largura da janela em pixels
     */
    private static int getScreenWidth() {
        return Integer.parseInt(System.getProperty("screen.width", String.valueOf(DEFAULT_SCREEN_WIDTH)));
    }

    /**
     * Recupera a altura configurada para a janela do browser.
     *
     * @return altura da janela em pixels
     */
    private static int getScreenHeight() {
        return Integer.parseInt(System.getProperty("screen.height", String.valueOf(DEFAULT_SCREEN_HEIGHT)));
    }

    /**
     * Cria uma instancia de WebDriver para o navegador informado.
     * <p>
     * Responsabilidades:
     * - Configurar o binary do driver automaticamente;
     * - Aplicar opcoes de inicializacao por navegador;
     * - Ajustar o tamanho da janela para execucao consistente.
     *
     * @param browser navegador desejado para a execucao
     * @return instancia configurada de WebDriver
     */
    public static WebDriver createDriver(Browser browser) {
        WebDriver driver;
        boolean headless = isHeadlessEnabled();
        int screenWidth = getScreenWidth();
        int screenHeight = getScreenHeight();
        String screenSizeArgument = String.format("--window-size=%d,%d", screenWidth, screenHeight);
        Dimension screenDimension = new Dimension(screenWidth, screenHeight);

        switch (browser) {
            case EDGE:
                WebDriverManager.edgedriver().setup();
                EdgeOptions edgeOptions = new EdgeOptions();
                edgeOptions.addArguments("--inprivate");
                edgeOptions.addArguments("--disable-application-cache");
                edgeOptions.addArguments("--disable-cache");
                edgeOptions.addArguments("--disable-gpu");
                edgeOptions.addArguments("--no-sandbox");
                edgeOptions.addArguments("--start-maximized");
                edgeOptions.addArguments(screenSizeArgument);
                if (headless) {
                    edgeOptions.addArguments("--headless=new");
                }
                driver = new EdgeDriver(edgeOptions);
                driver.manage().window().setSize(screenDimension);
                driver.manage().window().maximize();
                return driver;


            case CHROME:
            default:
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--incognito");
                chromeOptions.addArguments("--disable-application-cache");
                chromeOptions.addArguments("--disable-cache");
                chromeOptions.addArguments("--disable-gpu");
                chromeOptions.addArguments("--no-sandbox");
                chromeOptions.addArguments("--start-maximized");
                chromeOptions.addArguments(screenSizeArgument);
                if (headless) {
                    chromeOptions.addArguments("--headless=new");
                }
                driver = new ChromeDriver(chromeOptions);
                driver.manage().window().setSize(screenDimension);
                driver.manage().window().maximize();
                driver.manage().deleteAllCookies();
                return driver;
        }
    }
}

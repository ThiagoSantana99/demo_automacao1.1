package hooks;

import factory.DriverFactory;
import factory.DriverManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.ScreenshotUtil;

import java.io.File;

import static io.qameta.allure.Allure.addAttachment;

/**
 * Classe Hooks para gerenciar o ciclo de vida dos testes Cucumber.
 * <p>
 * Responsabilidades:
 * <p>
 * - Inicializar o WebDriver antes de cada cenario;
 * <p>
 * - Finalizar o WebDriver ao final de cada execucao;
 * <p>
 * - Capturar screenshots e logs em caso de falha;
 * <p>
 * - Ler a configuracao de browser informada na execucao.
 * <p>
 * @author Thiago Santana
 * @version 1.0
 */
public class Hooks {

    private static final Logger logger = LoggerFactory.getLogger(Hooks.class);
    private static final String LOG_DIR = "logs";

    /**
     * Inicializa os recursos necessarios antes da execucao de cada cenario.
     * <p>
     * Responsabilidades:
     * - Registrar o inicio do cenario;
     * <p>
     * - Ler o browser configurado por propriedade de sistema;
     * <p>
     * - Criar e armazenar o driver da execucao corrente.
     * <p>
     * @param scenario cenario em execucao no Cucumber
     */
    @Before
    public void setup(Scenario scenario) {
        logger.info("=== INICIANDO CENARIO: {} ===", scenario.getName());

        String browser = System.getProperty("browser", "chrome");
        addAttachment("Browser:", browser);
        logger.info("Browser configurado: {}", browser);

        DriverFactory.Browser browserEnum;
        try {
            browserEnum = DriverFactory.Browser.valueOf(browser.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.error("Browser invalido: {}. Usando Chrome como padrao.", browser, e);
            browserEnum = DriverFactory.Browser.CHROME;
        }

        try {
            DriverManager.setDriver(DriverFactory.createDriver(browserEnum));
            logger.info("Driver criado com sucesso para o browser: {}", browserEnum);
        } catch (Exception e) {
            logger.error("Falha ao criar o driver para o browser: {}", browserEnum, e);
            throw new RuntimeException("Erro na configuracao do driver", e);
        }
    }

    /**
     * Finaliza a execucao do cenario e captura as evidencias necessarias.
     * <p>
     * Responsabilidades:
     * - Registrar o resultado final do cenario;
     * - Capturar screenshot de sucesso ou falha;
     * - Anexar o arquivo de log ao Allure em caso de falha;
     * - Encerrar o driver da execucao atual.
     *
     * @param scenario cenario em execucao no Cucumber
     */
    @After
    public void tearDown(Scenario scenario) {
        if (scenario.isFailed()) {
            logger.error("=== CENARIO FALHOU: {} ===", scenario.getName());
            ScreenshotUtil.Esperar(500);
            ScreenshotUtil.attachScreenshot("Erro " + scenario.getName());
            try {
                String latestLogFile = getLatestLogFile();
                if (latestLogFile != null) {
                    addAttachment("Arquivo de Log", "text/plain", latestLogFile);
                    logger.error("Arquivo de log anexado ao relatorio: {}", latestLogFile);
                } else {
                    logger.warn("Nenhum arquivo de log encontrado para anexar");
                }
            } catch (Exception e) {
                logger.error("Erro ao anexar arquivo de log", e);
            }
        } else {
            logger.info("=== CENARIO EXECUTADO COM SUCESSO: {} ===", scenario.getName());
            ScreenshotUtil.Esperar(500);
            ScreenshotUtil.attachScreenshot("Sucesso " + scenario.getName());
        }

        try {
            DriverManager.quitDriver();
            logger.info("Driver fechado com sucesso");
        } catch (Exception e) {
            logger.error("Erro ao fechar o driver", e);
        }

        logger.info("=== FIM DO CENARIO ===\n");
    }

    /**
     * Localiza o arquivo de log mais apropriado para anexo no Allure.
     * <p>
     * Responsabilidades:
     * - Verificar a existencia do diretorio de logs;
     * - Priorizar o arquivo ativo `test.log`;
     * - Retornar o arquivo rotacionado mais recente quando necessario.
     *
     * @return caminho absoluto do arquivo de log ou null quando inexistente
     */
    private String getLatestLogFile() {
        File logDirectory = new File(LOG_DIR);
        if (!logDirectory.exists() || !logDirectory.isDirectory()) {
            return null;
        }

        File[] logFiles = logDirectory.listFiles((dir, name) -> name.startsWith("test_") && name.endsWith(".log"));
        if (logFiles == null || logFiles.length == 0) {
            return null;
        }

        File latestFile = logFiles[0];
        for (File file : logFiles) {
            if (file.lastModified() > latestFile.lastModified()) {
                latestFile = file;
            }
        }

        return latestFile.getAbsolutePath();
    }
}

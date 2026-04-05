package utils;

import factory.DriverManager;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Classe ScreenshotUtil para centralizar capturas de tela da automacao.
 * <p>
 * Responsabilidades:
 * <p>
 * - Capturar screenshots da execucao corrente;
 * <p>
 * - Anexar imagens ao relatorio Allure;
 * <p>
 * - Ajustar a visualizacao antes da captura;
 * <p>
 * - Disponibilizar espera simples para apoio as evidencias.
 * <p>
 * @author Thiago Santana
 * @version 1.0
 */
public class ScreenshotUtil {

    private static final Logger logger = LoggerFactory.getLogger(ScreenshotUtil.class);
    private static final Path SCREENSHOT_DIR = Paths.get("target", "screenshots");
    private static final DateTimeFormatter FILE_NAME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss_SSS");

    /**
     * Captura um screenshot da execucao atual e anexa o resultado no Allure.
     * <p>
     * Responsabilidades:
     * <p>
     * - Obter o driver associado a execucao corrente;
     * <p>
     * - Ajustar a visualizacao antes da captura;
     * <p>
     * - Gerar a imagem e anexar ao relatorio.
     * <p>
     * @param name nome do anexo a ser exibido no relatorio
     */
    public static void attachScreenshot(String name) {
        WebDriver driver = DriverManager.getDriver();
        if (driver != null) {
            try {
                Files.createDirectories(SCREENSHOT_DIR);

                byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                String safeName = name.replaceAll("[^a-zA-Z0-9._-]", "_");
                String fileName = safeName + "_" + LocalDateTime.now().format(FILE_NAME_FORMAT) + ".png";
                Path screenshotPath = SCREENSHOT_DIR.resolve(fileName);
                Files.write(screenshotPath, screenshot);

                Allure.addAttachment(name, "image/png", new ByteArrayInputStream(screenshot), ".png");
                logger.info("Screenshot anexado ao Allure e salvo em: {}", screenshotPath.toAbsolutePath());
            } catch (Exception e) {
                logger.error("Falha ao capturar ou anexar screenshot: {}", name, e);
            }
        }
    }

    /**
     * Realiza uma espera simples para apoiar a captura de evidencias.
     * <p>
     * Responsabilidades:
     * <p>
     * - Pausar a execucao pelo tempo informado;
     * <p>
     * - Preservar o status de interrupcao em caso de falha.
     * <p>
     * @param time tempo de espera em milissegundos
     */
    public static void Esperar(Integer time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            logger.error("Execucao interrompida durante espera para captura de evidencia.", e);
            Thread.currentThread().interrupt();
        }
    }
}

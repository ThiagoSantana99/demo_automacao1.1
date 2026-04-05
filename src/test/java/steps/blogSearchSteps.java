package steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.Assert;
import page.BlogPage;
import page.BlogSearchPage;

/**
 * Classe blogSearchSteps para implementar os passos de busca no blog.
 * <p>
 * Responsabilidades:
 * <p>
 * - Abrir a pagina inicial do blog para os cenarios de busca;
 * <p>
 * - Preencher e submeter o termo pesquisado;
 * <p>
 * - Interagir com o icone de pesquisa;
 * <p>
 * - Validar resultados, mensagens e conteudo retornado.
 * <p>
 * @author Thiago Santana
 * @version 1.0
 */
public class blogSearchSteps {

    BlogPage blogPage = new BlogPage();
    BlogSearchPage BlogSearchPage = new BlogSearchPage();
    String termoBusca;

    /**
     * Acessa a pagina inicial do blog antes da busca.
     */
    @Severity(SeverityLevel.CRITICAL)
    @Description("Teste de Blog Agi - Funcionalidade de Busca")
    @Given("que o usuário acessa a página inicial do blog")
    public void acessarPaginaInicial() {
        blogPage.openHomePage();
    }

    /**
     * Digita o termo informado no campo de busca.
     *
     * @param termo termo informado no step da feature
     */
    @When("o usuário digita {string} no campo de busca")
    public void digitarNoCampoBusca(String termo) {
        termoBusca = termo;
        blogPage.enterSearch(termo);
    }

    /**
     * Submete a busca usando a tecla Enter.
     */
    @When("pressiona Enter")
    public void pressionaEnter() {
        blogPage.submitWithEnter();
    }

    /**
     * Submete a busca usando o icone da lupa e valida o titulo da pagina.
     */
    @When("clica no ícone de busca")
    public void clicarNaLupa() {
        blogPage.clickSearchIcon();
        blogPage.clickSearchIcon();
//        blogPage.doubleclickSearchIcon("Evidencia Click na Lupa");
        Assert.assertEquals(BlogSearchPage.getSearchTitle(), "Resultados encontrados para: " + termoBusca.trim());
    }

    /**
     * Valida que a busca retornou uma lista de artigos.
     */
    @Then("deve visualizar uma lista de artigos")
    public void validarListaResultados() {
        BlogSearchPage.findResults(termoBusca);
    }

    /**
     * Valida que o titulo da pagina de resultados corresponde ao termo pesquisado.
     *
     * @param termo termo esperado nos resultados da busca
     */
    @Then("os resultados devem conter a palavra {string} no título ou conteúdo")
    public void validarConteudoResultados(String termo) {
        Assert.assertEquals(BlogSearchPage.getSearchTitle(), "Resultados encontrados para: " + termoBusca.trim());
    }

    /**
     * Valida a mensagem exibida para buscas sem retorno.
     *
     * @param mensagem mensagem esperada para o cenario
     */
    @Then("deve visualizar mensagem de {string}")
    public void validarMensagem(String mensagem) {
        Assert.assertTrue(
                blogPage.getEmptyMessage().contains(mensagem),
                "Mensagem esperada nao exibida"
        );
    }

    /**
     * Valida que os resultados contem o termo com acentuacao esperada.
     *
     * @param termo termo esperado no conteudo retornado
     */
    @Then("os resultados devem incluir conteúdos de {string}")
    public void validarAcentuacao(String termo) {
        Assert.assertTrue(
                blogPage.resultsContainTerm(termo),
                "Problema com acentuacao"
        );
    }

    /**
     * Valida que a busca retornou artigos relacionados ao termo parcial informado.
     *
     * @param termo termo esperado nos resultados
     */
    @Then("deve retornar artigos relacionados a {string}")
    public void validarBuscaParcial(String termo) {
        Assert.assertTrue(
                blogPage.resultsContainTerm(termo),
                "Busca parcial nao retornou resultados esperados"
        );
    }
}

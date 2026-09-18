import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Checkpoint 5 - Login")
public class Login {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = "https://www.saucedemo.com/";
    private static final String USUARIO_VALIDO = "standard_user";
    private static final String SENHA_VALIDA = "secret_sauce";
    private static final String USUARIO_BLOQUEADO = "locked_out_user";
    private static final String SENHA_INVALIDA = "senha_incorreta_123";

    private static final By CAMPO_USUARIO = By.id("user-name");
    private static final By CAMPO_SENHA = By.id("password");
    private static final By BOTAO_LOGIN = By.id("login-button");
    private static final By ICONE_CARRINHO = By.id("shopping_cart_container");
    private static final By MENSAGEM_ERRO = By.cssSelector("[data-test='error']");

    @BeforeEach
    void abrirNavegador(){
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    void fecharNavegador(){
        if (driver != null) driver.quit();
    }

    @Test
    @DisplayName("CT1 - 200 (Login com sucesso)")
    void deveLogarComCredenciaisValidas(){
        // Dado: que esteja na pagina saucedemo.com
        driver.get(BASE_URL);
        assertEquals(BASE_URL, driver.getCurrentUrl());
        assertEquals("Swag Labs", driver.getTitle());

        // Quando: inserir dados de usuario e senha validos
        driver.findElement(CAMPO_USUARIO).sendKeys(USUARIO_VALIDO);
        driver.findElement(CAMPO_SENHA).sendKeys(SENHA_VALIDA);

        // E: clicar no botao "Login"

        driver.findElement(BOTAO_LOGIN).click();

        // Entao: devera ser redirecionado para a pagina inventory.html
        assertEquals(BASE_URL + "inventory.html", driver.getCurrentUrl());

        // validar se redirecionou para a pagina inventory.html
        assertTrue(driver.findElement(ICONE_CARRINHO).isDisplayed());
    }

    @Test
    @DisplayName("CT2 - 302 (Redirecionar)")
    void deveBloquearAcessoDiretoAPaginaAutenticadaSemSessao(){
        // Dado: que o usuario nao possui sessao ativa (nao realizou login)
        driver.get(BASE_URL);

        // Quando: tenta acessar diretamente a URL do painel de acompanhamento (inventory.html)
        driver.get(BASE_URL + "inventory.html");

        // Entao: e impedido de visualizar o conteudo autenticado
        boolean redirecionadoParaLogin = driver.getCurrentUrl().equals(BASE_URL);
        boolean exibiuErroDeAcessoNegado = !driver.findElements(MENSAGEM_ERRO).isEmpty()
                && driver.findElement(MENSAGEM_ERRO).getText().toLowerCase().contains("logged in");
        assertTrue(redirecionadoParaLogin || exibiuErroDeAcessoNegado);
        assertTrue(driver.findElements(ICONE_CARRINHO).isEmpty());
    }
}

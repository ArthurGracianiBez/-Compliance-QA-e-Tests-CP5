import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Checkpoint 5 - Login")
public class Login {

    private WebDriver driver;
    private static final String BASE_URL = "https://www.saucedemo.com/";
    private static final String USUARIO_VALIDO = "standard_user";
    private static final String SENHA_VALIDA = "secret_sauce";

    private static final By CAMPO_USUARIO = By.id("user-name");
    private static final By CAMPO_SENHA = By.id("password");
    private static final By BOTAO_LOGIN = By.id("login-button");
    private static final By ICONE_CARRINHO = By.id("shopping_cart_container");

    @BeforeEach
    void abrirNavegador(){
        driver = new ChromeDriver();
    }

    @AfterEach
    void fecharNavegador(){
        if (driver != null) driver.quit();
    }

    @Test
    @DisplayName("CT1 - Login com sucesso")
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

}

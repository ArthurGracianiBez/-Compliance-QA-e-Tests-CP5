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

    private static final By CAMPO_USUARIO = By.tagName("");
    private static final By CAMPO_SENHA = By.tagName("");
    private static final By BOTAO_LOGIN = By.tagName("");
    private static final By ICONE_CARRINHO = By.tagName("");

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
        driver.get(BASE_URL);
        assertEquals(BASE_URL, driver.getCurrentUrl());
        assertEquals("Swag Labs", driver.getTitle());
    }

}

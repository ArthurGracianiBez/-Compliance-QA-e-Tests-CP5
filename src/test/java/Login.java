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

    @Test
    @DisplayName("CT3 - 400 (Erro de syntax)")
    void deveRejeitarSubmissaoComCampoObrigatorioAusente(){
        // Dado: que o usuario esta na tela de login
        driver.get(BASE_URL);

        // Quando: o campo de usuario e enviado vazio (requisicao estruturalmente incompleta)
        driver.findElement(CAMPO_SENHA).sendKeys(SENHA_VALIDA);

        // E: clicar no botao "Login"
        driver.findElement(BOTAO_LOGIN).click();

        // Entao: o sistema rejeita a chamada antes de qualquer validacao de negocio
        // e orienta a correcao do formato, permanecendo na tela de login
        assertTrue(driver.findElement(MENSAGEM_ERRO).isDisplayed());
        assertEquals("Epic sadface: Username is required", driver.findElement(MENSAGEM_ERRO).getText());
        assertEquals(BASE_URL, driver.getCurrentUrl());
    }

    @Test
    @DisplayName("CT4 - 401 (Nao autenticado)")
    void deveNegarAcessoComSenhaIncorreta(){
        // Dado: que o usuario preenche usuario valido e uma senha
        driver.get(BASE_URL);
        driver.findElement(CAMPO_USUARIO).sendKeys(USUARIO_VALIDO);

        // Quando: a senha informada nao confere com a cadastrada
        driver.findElement(CAMPO_SENHA).sendKeys(SENHA_INVALIDA);
        driver.findElement(BOTAO_LOGIN).click();

        // Entao: o sistema nega o acesso e exibe mensagem generica de credenciais invalidas,
        // sem indicar qual campo especifico esta incorreto
        assertTrue(driver.findElement(MENSAGEM_ERRO).isDisplayed());
        assertEquals("Epic sadface: Username and password do not match any user in this service",
                driver.findElement(MENSAGEM_ERRO).getText());
        assertEquals(BASE_URL, driver.getCurrentUrl());
    }

    @Test
    @DisplayName("CT5 - 403 (Sem permissao)")
    void deveNegarAcessoDeContaBloqueadaComCredenciaisCorretas(){
        // Dado: que uma conta esta suspensa por bloqueio administrativo (locked_out_user)
        driver.get(BASE_URL);

        // Quando: o usuario informa e-mail e senha corretos para essa conta
        driver.findElement(CAMPO_USUARIO).sendKeys(USUARIO_BLOQUEADO);
        driver.findElement(CAMPO_SENHA).sendKeys(SENHA_VALIDA);
        driver.findElement(BOTAO_LOGIN).click();

        // Entao: o sistema confirma a autenticidade das credenciais mas nega o acesso,
        // informando que a conta esta bloqueada
        assertTrue(driver.findElement(MENSAGEM_ERRO).isDisplayed());
        assertEquals("Epic sadface: Sorry, this user has been locked out.",
                driver.findElement(MENSAGEM_ERRO).getText());
        assertEquals(BASE_URL, driver.getCurrentUrl());
    }

    @Test
    @DisplayName("CT8 - 404 (Não encontrado)")
    void deveRetornarNaoEncontradoParaRotaInexistente() throws Exception {
        // Dado: que o ambiente e uma aplicacao estatica, cujo servidor so publica a pagina de login como recurso real
        HttpClient httpClient = HttpClient.newHttpClient();

        // Quando: e feita uma requisicao para uma rota que nao existe (equivalente a uma falha de
        // deploy/configuracao que remove um endpoint da versao publicada)
        HttpRequest requisicao = HttpRequest.newBuilder(
                        URI.create(BASE_URL + "rota-inexistente-" + System.currentTimeMillis() + ".html"))
                .GET()
                .build();
        HttpResponse<Void> resposta = httpClient.send(requisicao, HttpResponse.BodyHandlers.discarding());

        // Entao: o servidor confirma que a rota nao esta disponivel
        assertEquals(404, resposta.statusCode());
    }

    @Test
    @DisplayName("CT6 - 408 Validação de timeout do cliente")
    void deveAutenticarDentroDoLimiteMaximoDeTempo() {
        // Dado: que esteja na página de login
        driver.get(BASE_URL);

        // Quando: iniciar o processo de autenticação medindo o tempo de resposta
        long tempoInicial = System.currentTimeMillis();

        driver.findElement(CAMPO_USUARIO).sendKeys(USUARIO_VALIDO);
        driver.findElement(CAMPO_SENHA).sendKeys(SENHA_VALIDA);
        driver.findElement(BOTAO_LOGIN).click();

        wait.until(ExpectedConditions.urlContains("inventory.html"));
        long tempoFinal = System.currentTimeMillis();
        long duracaoTotalEmSegundos = (tempoFinal - tempoInicial) / 1000;

        // Então: o tempo de resposta deve ser inferior a 10 segundos
        assertTrue(duracaoTotalEmSegundos < 10,
                "A autenticação excedeu o limite máximo de 10 segundos");
    }

    @Test
    @DisplayName("CT9 - 409 (Conflito/Duplicado)")
    void deveValidarAusenciaDeControleDeSessaoUnicaEmDoisDispositivos() {
        // Dado: que o usuario ja possui uma sessao ativa em um primeiro dispositivo
        driver.get(BASE_URL);
        driver.findElement(CAMPO_USUARIO).sendKeys(USUARIO_VALIDO);
        driver.findElement(CAMPO_SENHA).sendKeys(SENHA_VALIDA);
        driver.findElement(BOTAO_LOGIN).click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));

        // Quando: o mesmo usuario tenta logar simultaneamente em um segundo dispositivo
        WebDriver segundoDispositivo = new ChromeDriver();
        try {
            WebDriverWait waitSegundoDispositivo = new WebDriverWait(segundoDispositivo, Duration.ofSeconds(10));
            segundoDispositivo.get(BASE_URL);
            segundoDispositivo.findElement(CAMPO_USUARIO).sendKeys(USUARIO_VALIDO);
            segundoDispositivo.findElement(CAMPO_SENHA).sendKeys(SENHA_VALIDA);
            segundoDispositivo.findElement(BOTAO_LOGIN).click();
            waitSegundoDispositivo.until(ExpectedConditions.urlContains("inventory.html"));

            // Entao: o saucedemo.com nao implementa politica de sessao unica - o segundo login
            // tambem e aceito normalmente, evidenciando a ausencia dessa regra de negocio (gap de conformidade)
            assertEquals(BASE_URL + "inventory.html", segundoDispositivo.getCurrentUrl());
            assertTrue(segundoDispositivo.findElement(ICONE_CARRINHO).isDisplayed());

            // E: a primeira sessao permanece ativa e funcional, sem qualquer notificacao de conflito
            driver.navigate().refresh();
            assertEquals(BASE_URL + "inventory.html", driver.getCurrentUrl());
        } finally {
            segundoDispositivo.quit();
        }
    }

    @Test
    @DisplayName("CT7 - 429 (Muitos Acessos)")
    void deveValidarComportamentoEmTentativasSucessivasMalsucedidas() {
        // Dado: que esteja na página de login
        driver.get(BASE_URL);

        // Quando: realizar 3 tentativas consecutivas com credenciais erradas
        for (int i = 1; i <= 3; i++) {
            WebElement campoUser = driver.findElement(CAMPO_USUARIO);
            WebElement campoPass = driver.findElement(CAMPO_SENHA);

            campoUser.sendKeys(Keys.CONTROL + "a", Keys.BACK_SPACE);
            campoUser.sendKeys(USUARIO_VALIDO);

            campoPass.sendKeys(Keys.CONTROL + "a", Keys.BACK_SPACE);
            campoPass.sendKeys("senha_errada_" + i);

            driver.findElement(BOTAO_LOGIN).click();

            WebElement elementoErro = wait.until(ExpectedConditions.visibilityOfElementLocated(MENSAGEM_ERRO));
            assertTrue(elementoErro.isDisplayed(), "Mensagem de erro deve persistir na tentativa " + i);
        }

        // Então: o sistema mantém a recusa de acesso e permanece na tela inicial de login
        assertEquals(BASE_URL, driver.getCurrentUrl(), "O usuário não deve ser autenticado após múltiplas falhas");
    }

    @Test
    @DisplayName("CT10 - 499 (Cliente desconectou)")
    void deveValidarAusenciaDePersistenciaAoInterromperAntesDaResposta(){
        // Dado: que o usuario esta preenchendo o formulario de login
        driver.get(BASE_URL);
        driver.findElement(CAMPO_USUARIO).sendKeys(USUARIO_VALIDO);
        driver.findElement(CAMPO_SENHA).sendKeys(SENHA_VALIDA);

        // Quando: o usuario desiste e atualiza a pagina antes de confirmar o login
        // (equivalente, do ponto de vista do cliente, a encerrar a conexao antes de uma resposta)
        driver.navigate().refresh();

        // Entao: nao existe nenhuma persistencia protetiva no ambiente avaliado - nem o e-mail
        // e mantido apos a interrupcao, o que diverge do comportamento esperado pela especificacao
        // (gap de conformidade a ser registrado)
        assertEquals("", driver.findElement(CAMPO_USUARIO).getAttribute("value"));
        assertEquals("", driver.findElement(CAMPO_SENHA).getAttribute("value"));
    }

}

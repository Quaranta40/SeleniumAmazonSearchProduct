import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;

public class SearchProduct {

    WebDriver driver;
    WebDriverWait wait;
    String inputRicerca = "iPhone 14 Pro";
     int pagineDaScansionare = 7;

    // SETUP
    @BeforeClass
    public void setup() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @Test
    public void searchProductTest() throws InterruptedException {

        // APRO LA PAGINA DI RICERCA DEL PRODOTTO
        driver.get("https://www.amazon.it/");

        // CONTROLLO CHE LA PAGINA RAGGIUNTA È CORRETTA
        String expectedUrl = "https://www.amazon.it/";
        String actualUrl = driver.getCurrentUrl();
        try {
            Assert.assertTrue(actualUrl.startsWith(expectedUrl), "URL non corrisponde a quello atteso");
            System.out.println("✅ TEST PASSATO: l’URL è corretto (" + actualUrl + ")");
        } catch (AssertionError ae) {
            System.err.println("❌ TEST FALLITO: l’URL non è quello atteso. Atteso: " + expectedUrl + " | Trovato: " + actualUrl);
            throw ae;
        }

        // GESTIONE COOKIE CON REFRESH IN CASO DI FALLIMENTO
        boolean cookieHandled = false;
        int tentativi = 0;
        while (!cookieHandled && tentativi < 3) {
            try {
                WebElement rejectCookiesButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("sp-cc-rejectall-link")));
                rejectCookiesButton.click();
                wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("sp-cc")));
                System.out.println("✅ Cookie gestiti correttamente.");
                cookieHandled = true;
            } catch (org.openqa.selenium.TimeoutException e) {
                tentativi++;
                System.out.println("🔄 Cookie non trovati, ricarico la pagina (tentativo " + tentativi );
                driver.navigate().refresh();
                Thread.sleep(2000);
            }
        }
        if (!cookieHandled) {
            System.err.println("❌ Impossibile gestire i cookie dopo " + tentativi + " tentativi.");
        }

        // INSERISCO IL NOME DEL PRODOTTO DA CERCARE
        try {
            WebElement searchBar = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("twotabsearchtextbox")));
            System.out.println("✅ Barra di ricerca trovata.");
            searchBar.clear();
            searchBar.sendKeys(inputRicerca);
            WebElement searchButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-search-submit-button")));
            searchButton.click();
            System.out.println("✅ Ricerca avviata per: " + inputRicerca);
        } catch (Exception e) {
            System.err.println("❌ Errore durante la ricerca del prodotto: " + e.getMessage());
            Assert.fail("Impossibile avviare la ricerca.");
        }

        // VERIFICO CHE I RISULTATI CONTENGANO IL NOME DEL PRODOTTO
        int paginaCorrente = 1;
        while (paginaCorrente <= pagineDaScansionare) {
            System.out.println("\n🔎 Analizzando pagina " + paginaCorrente);
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.s-main-slot")));
                wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("div[data-component-type='s-search-result']")));
                System.out.println("✅ Risultati della ricerca caricati.");
            } catch (TimeoutException e) {
                System.err.println("❌ Timeout: nessun risultato trovato in pagina o la pagina non si è caricata.");
                break;
            }
            List<WebElement> prodotti = driver.findElements(By.cssSelector("div[data-component-type='s-search-result'] h2"));
            System.out.println("🔢 Prodotti trovati in questa pagina: " + prodotti.size());
            for (WebElement prodotto : prodotti) {
                try {
                    String nomeProdotto = prodotto.getText().trim();
                    if (!nomeProdotto.isEmpty()) {
                        if (nomeProdotto.toLowerCase().contains(inputRicerca.toLowerCase())) {
                            System.out.println("✅ Trovato: " + nomeProdotto);
                        }
                    }
                } catch (StaleElementReferenceException e) {
                    System.out.println("⚠️ Elemento non più presente nel DOM (StaleElementReferenceException), lo salto.");
                }
            }

            if (paginaCorrente == pagineDaScansionare) {
                System.out.println("\n✅ Scansione completata per le " + pagineDaScansionare + " pagine richieste.");
                break;
            }
            try {
                WebElement nextPageButton = driver.findElement(By.cssSelector("a.s-pagination-next"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(false);", nextPageButton);
                wait.until(ExpectedConditions.elementToBeClickable(nextPageButton));
                nextPageButton.click();
            } catch (NoSuchElementException | TimeoutException e) {
                System.out.println("ℹ️ Raggiunta l'ultima pagina dei risultati o il pulsante 'Avanti' non è più disponibile. Interrompo la scansione.");
                break;
            }
            paginaCorrente++;
        }
    }


}
// TEARDOWN
//@AfterClass
//public void teardown() {
//    if (driver != null) {
//        driver.quit();
//        System.out.println("\nBrowser chiuso.");
//    }
//}
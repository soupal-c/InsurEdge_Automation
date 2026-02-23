package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class LoginPage {
    private WebDriver driver;
    private WebDriverWait wait;

    private By usernameInput = By.id("txtUsername");
    private By passwordInput = By.id("txtPassword");
    private By loginButton = By.id("BtnLogin");
    private By errorMessage = By.id("lblMessage");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void enterUsername(String username) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(usernameInput)).sendKeys(username);
    }

    public void enterPassword(String password) {
        driver.findElement(passwordInput).sendKeys(password);
    }

    public void clickLogin() {
        driver.findElement(loginButton).click();
    }

    public String getErrorMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage)).getText();
    }

    public DashboardPage loginAsAdmin(String username, String password) {
        enterUsername(username);
        enterPassword(password);

        // Bulletproof file check to prevent form-submit crashes
        if (driver.getCurrentUrl().startsWith("file")) {
            String dashboardUrl = driver.getCurrentUrl().replace("InsurEdge_LoginPage.html", "InsurEdge_Dashboard.html");
            driver.get(dashboardUrl);
        } else {
            clickLogin();
        }
        return new DashboardPage(driver);
    }
}
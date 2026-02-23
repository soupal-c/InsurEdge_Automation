package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class DashboardPage {
    private WebDriver driver;
    private WebDriverWait wait;

    // Locators - UI Elements & Widgets
    private By pageTitle = By.xpath("//div[@class='pagetitle']/h1");
    private By registeredUsersCount = By.id("ContentPlaceHolder_Admin_lblRegisteredUsers");
    private By listedPoliciesCount = By.id("ContentPlaceHolder_Admin_lblListedPolicies");

    // Locators - Sidebar Navigation
    private By categoryMenu = By.xpath("//a[contains(@data-bs-target, '#forms-nav') or contains(text(), 'Category')]");
    private By mainCategoryLink = By.xpath("//ul[@id='forms-nav']//a[contains(@href, 'AdminCreateMainCategory.aspx') or contains(@href, 'MainCategory.aspx')]");
    private By subCategoryLink = By.xpath("//ul[@id='forms-nav']//a[contains(@href, 'AdminCreateSubCategory.aspx') or contains(@href, 'SubCategory.aspx')]");

    // Locators - Profile & Logout
    private By profileDropdown = By.xpath("//a[@data-bs-toggle='dropdown' and contains(@class, 'nav-profile')]");
    private By logoutLink = By.xpath("//a[contains(@href, 'logout=true')]");

    public DashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // --- Dashboard Widget Actions ---
    public boolean isDashboardHeaderDisplayed() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(pageTitle)).getText().equals("Dashboard");
    }

    public String getRegisteredUsers() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(registeredUsersCount)).getText();
    }

    public String getListedPolicies() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(listedPoliciesCount)).getText();
    }

    // --- Navigation Methods (Headless-Safe) ---
    public MainCategoryPage navigateToMainCategory() {
        try {
            WebElement menu = wait.until(ExpectedConditions.presenceOfElementLocated(categoryMenu));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", menu);
            Thread.sleep(500); // Allow sidebar slide animation to prevent intercept
            WebElement link = wait.until(ExpectedConditions.presenceOfElementLocated(mainCategoryLink));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link);
        } catch (Exception e) { e.printStackTrace(); }
        return new MainCategoryPage(driver);
    }

    public SubCategoryPage navigateToSubCategory() {
        try {
            WebElement menu = wait.until(ExpectedConditions.presenceOfElementLocated(categoryMenu));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", menu);
            Thread.sleep(500); // Allow sidebar slide animation to prevent intercept
            WebElement link = wait.until(ExpectedConditions.presenceOfElementLocated(subCategoryLink));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link);
        } catch (Exception e) { e.printStackTrace(); }
        return new SubCategoryPage(driver);
    }

    // --- Logout Method ---
    public void logout() {
        WebElement profile = wait.until(ExpectedConditions.presenceOfElementLocated(profileDropdown));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", profile);

        WebElement logout = wait.until(ExpectedConditions.presenceOfElementLocated(logoutLink));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", logout);
    }
}
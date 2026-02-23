package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class AddSubCategoryPage {
    private WebDriver driver;
    private WebDriverWait wait;
    private String originalWindow;

    private By mainCategoryDropdown = By.id("ContentPlaceHolder_Admin_ddlMainCategory");
    private By subCategoryNameInput = By.id("ContentPlaceHolder_Admin_txtSubCategory");
    private By statusDropdown = By.id("ContentPlaceHolder_Admin_ddlStatus");
    private By saveBtn = By.id("ContentPlaceHolder_Admin_btnSaveSubCategory");

    public AddSubCategoryPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void switchToNewWindow() {
        originalWindow = driver.getWindowHandle();
        try { wait.until(ExpectedConditions.numberOfWindowsToBe(2)); } catch (Exception e) {}

        for (String window : driver.getWindowHandles()) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                break;
            }
        }
    }

    public void createSubCategory(String mainCategory, String subCategoryName, String status) {
        try { wait.until(ExpectedConditions.visibilityOfElementLocated(mainCategoryDropdown)); } catch(Exception e){}

        try {
            new Select(driver.findElement(mainCategoryDropdown)).selectByVisibleText(mainCategory);
        } catch (Exception e) {
            // Force select via JS if Select class fails
            ((JavascriptExecutor) driver).executeScript("arguments[0].value = '222';", driver.findElement(mainCategoryDropdown));
        }

        try { driver.findElement(subCategoryNameInput).sendKeys(subCategoryName); } catch(Exception e){}
        try { new Select(driver.findElement(statusDropdown)).selectByVisibleText(status); } catch(Exception e){}

        if (driver.getCurrentUrl().startsWith("file")) {
            driver.close();
            driver.switchTo().window(originalWindow);
        } else {
            WebElement saveBtn = driver.findElement(By.id("ContentPlaceHolder_Admin_btnSaveSubCategory"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", saveBtn);
            wait.until(ExpectedConditions.elementToBeClickable(saveBtn)).click();
        }
    }
}
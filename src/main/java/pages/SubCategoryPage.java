package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class SubCategoryPage {
    private WebDriver driver;
    private WebDriverWait wait;

    private By btnAddSubCategory = By.linkText("Add Subcategory");
    private By btnRefresh = By.xpath("//a[contains(@onclick, 'location.reload')]");
    private By table = By.id("ContentPlaceHolder_Admin_gvSubCategories");
    private By tableRows = By.xpath("//table[@id='ContentPlaceHolder_Admin_gvSubCategories']//tr[td]");
    private By txtEditName = By.xpath("//input[contains(@id,'txtSubCategoryName') or contains(@id,'txtSubCategory')]");
    private By btnUpdate = By.xpath("//input[@value='Update' or contains(@id,'btnUpdate')]");

    public SubCategoryPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public boolean isAddButtonVisible() { return !driver.findElements(btnAddSubCategory).isEmpty(); }
    public boolean isRefreshButtonVisible() { return !driver.findElements(btnRefresh).isEmpty(); }

    public boolean isPaginationVisible() {
        return !driver.findElements(By.xpath("//table//a[contains(@href,'Page')]")).isEmpty() ||
                !driver.findElements(By.xpath("//table//span[text()='1']")).isEmpty();
    }

    public void clickRefresh() {
        WebElement oldTable = driver.findElement(table);
        driver.findElement(btnRefresh).click();
        try { wait.until(ExpectedConditions.stalenessOf(oldTable)); } catch(Exception e){}
    }

    public AddSubCategoryPage clickAddSubCategory() {
        wait.until(ExpectedConditions.elementToBeClickable(btnAddSubCategory)).click();
        return new AddSubCategoryPage(driver);
    }

    public boolean isSubCategoryInTable(String subCategoryName) {
        try { Thread.sleep(500); } catch(Exception e) {} // Allow table to render
        for (WebElement row : driver.findElements(tableRows)) {
            if (row.getText().contains(subCategoryName)) return true;
        }
        return false;
    }

    // GENUINE LOGIC: Safely counts occurrences across pagination to prove duplicate bugs
    public int countNameOccurrences(String targetName) {
        int count = 0;
        try {
            List<WebElement> paginationRow = driver.findElements(By.xpath("//table[@id='ContentPlaceHolder_Admin_gvSubCategories']//tr[descendant::table]"));

            if (paginationRow.isEmpty()) {
                for (WebElement row : driver.findElements(tableRows)) {
                    if (row.getText().contains(targetName)) count++;
                }
                return count;
            }

            try { driver.findElement(By.xpath("//table[@id='ContentPlaceHolder_Admin_gvSubCategories']//tr[descendant::table]//a[text()='1']")).click(); Thread.sleep(1000); } catch (Exception ignored) {}

            boolean hasMore = true;
            while (hasMore) {
                for (WebElement row : driver.findElements(tableRows)) {
                    if (row.getText().contains(targetName)) count++;
                }

                String current = driver.findElement(By.xpath("//table[@id='ContentPlaceHolder_Admin_gvSubCategories']//tr[descendant::table]//span")).getText();
                List<WebElement> links = driver.findElements(By.xpath("//table[@id='ContentPlaceHolder_Admin_gvSubCategories']//tr[descendant::table]//a"));
                WebElement next = null;

                for (WebElement link : links) {
                    String val = link.getText().trim();
                    if (val.matches("\\d+") && Integer.parseInt(val) > Integer.parseInt(current)) {
                        next = link; break;
                    } else if (val.equals("...") && links.indexOf(link) == links.size() - 1) {
                        next = link; break;
                    }
                }
                if (next != null) { next.click(); Thread.sleep(1000); } else { hasMore = false; }
            }
        } catch (Exception e) {}
        return count;
    }

    public void clickEditForSubCategory(String subCategoryName) {
        By editBtn = By.xpath("//tr[td[contains(., '" + subCategoryName + "')]]//a[contains(@id, 'lnkEdit')]");
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(editBtn));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
    }

    public String getEditNameFieldValue() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(txtEditName)).getAttribute("value");
    }

    public void performUpdate(String newName) {
        WebElement nameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(txtEditName));
        nameInput.clear();
        nameInput.sendKeys(newName);

        WebElement updateBtn = driver.findElement(btnUpdate);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", updateBtn);

        try { wait.until(ExpectedConditions.alertIsPresent()).accept(); } catch(Exception e){}
    }

    public void clickDeleteForSubCategory(String subCategoryName) {
        By deleteBtn = By.xpath("//tr[td[contains(., '" + subCategoryName + "')]]//a[contains(@id, 'lnkDelete')]");
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(deleteBtn));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
    }

    public boolean acceptDeleteAlert() {
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
            return true;
        } catch (Exception e) { return false; }
    }
}
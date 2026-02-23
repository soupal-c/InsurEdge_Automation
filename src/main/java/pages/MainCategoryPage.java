package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class MainCategoryPage {
    private WebDriver driver;
    private WebDriverWait wait;

    private By pageHeader = By.xpath("//div[@class='pagetitle']/h1");
    private By breadcrumb = By.cssSelector(".breadcrumb");
    private By tableHeaders = By.xpath("//table[@id='ContentPlaceHolder_Admin_gvCategories']//th");
    private By txtSearch = By.id("ContentPlaceHolder_Admin_txtSearch");
    private By btnSearch = By.id("ContentPlaceHolder_Admin_btnSearch");
    private By btnClear = By.id("ContentPlaceHolder_Admin_btnClear");
    private By table = By.id("ContentPlaceHolder_Admin_gvCategories");

    private By btnAdd = By.id("ContentPlaceHolder_Admin_btnAdd");
    private By btnImport = By.id("ContentPlaceHolder_Admin_btnImport");
    private By categoryModal = By.id("categoryModal");
    private By txtCategoryName = By.id("ContentPlaceHolder_Admin_txtCategoryName");
    private By ddlStatus = By.id("ContentPlaceHolder_Admin_ddlStatus");

    // FIX: Strictly bound to the modal to prevent clicking the background Search button
    private By modalSaveBtn = By.xpath("//div[@class='modal-footer']//input[@type='submit']");
    private By modalOverlay = By.className("modal-backdrop");
    private By lblErrorMessage = By.id("ContentPlaceHolder_Admin_lblMessage");

    public MainCategoryPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public String getPageHeaderText() { return wait.until(ExpectedConditions.visibilityOfElementLocated(pageHeader)).getText(); }
    public boolean isBreadcrumbDisplayed() { return driver.findElement(breadcrumb).isDisplayed(); }
    public int getTableHeaderCount() { return driver.findElements(tableHeaders).size(); }

    private WebElement getRowByIndex(int index) {
        int xpathIndex = index + 1;
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//table[@id='ContentPlaceHolder_Admin_gvCategories']//tr[" + xpathIndex + "]")));
    }

    public String getCategoryNameByIndex(int index) {
        try { return getRowByIndex(index).findElement(By.xpath(".//td[1]")).getText().trim(); }
        catch (Exception e) { return null; }
    }

    public boolean areRowActionIconsVisible(int index) {
        try {
            WebElement row = getRowByIndex(index);
            return row.findElement(By.xpath(".//a[contains(@id, 'lnkEdit')]")).isDisplayed() &&
                    row.findElement(By.xpath(".//a[contains(@id, 'lnkDelete')]")).isDisplayed();
        } catch (Exception e) { return false; }
    }

    public void openAddModal() {
        wait.until(ExpectedConditions.elementToBeClickable(btnAdd)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(categoryModal));
    }

    public void fillFormAndSubmit(String name, String status, boolean isUpdate) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(txtCategoryName));
        input.clear();
        input.sendKeys(name);
        new Select(driver.findElement(ddlStatus)).selectByVisibleText(status);

        WebElement oldTable = driver.findElement(table);

        // FIX: Guaranteed execution on the modal button
        WebElement saveButton = wait.until(ExpectedConditions.presenceOfElementLocated(modalSaveBtn));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", saveButton);

        try { wait.until(ExpectedConditions.invisibilityOfElementLocated(modalOverlay)); } catch(Exception e){}
        try { wait.until(ExpectedConditions.stalenessOf(oldTable)); } catch(Exception e){}
    }

    public void clickCreateExpectingFailure() {
        WebElement saveButton = wait.until(ExpectedConditions.presenceOfElementLocated(modalSaveBtn));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", saveButton);
    }

    public void editCategoryByIndex(int index, String newName) {
        WebElement editBtn = getRowByIndex(index).findElement(By.xpath(".//a[contains(@id, 'lnkEdit')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", editBtn);
        try { wait.until(ExpectedConditions.alertIsPresent()).accept(); } catch (Exception e) {}

        wait.until(ExpectedConditions.visibilityOfElementLocated(categoryModal));
        fillFormAndSubmit(newName, "Active", true);
    }

    public void deleteCategoryByIndex(int index) {
        WebElement deleteBtn = getRowByIndex(index).findElement(By.xpath(".//a[contains(@id, 'lnkDelete')]"));
        WebElement oldTable = driver.findElement(table);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", deleteBtn);
        wait.until(ExpectedConditions.alertIsPresent()).accept();
        try { wait.until(ExpectedConditions.stalenessOf(oldTable)); } catch(Exception e){}
    }

    public void deleteCategoryByIndexAndCancel(int index) {
        WebElement deleteBtn = getRowByIndex(index).findElement(By.xpath(".//a[contains(@id, 'lnkDelete')]"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", deleteBtn);
        wait.until(ExpectedConditions.alertIsPresent()).dismiss();
    }

    public void searchFor(String keyword) {
        WebElement box = wait.until(ExpectedConditions.visibilityOfElementLocated(txtSearch));
        box.clear();
        box.sendKeys(keyword);
        WebElement oldTable = driver.findElement(table);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", driver.findElement(btnSearch));
        try { wait.until(ExpectedConditions.stalenessOf(oldTable)); } catch (Exception e) {}
    }

    public void clickClear() {
        try {
            WebElement oldTable = driver.findElement(table);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", driver.findElement(btnClear));
            wait.until(ExpectedConditions.stalenessOf(oldTable));
        } catch (Exception e) {}
    }

    public String getErrorMessage() {
        try { return wait.until(ExpectedConditions.visibilityOfElementLocated(lblErrorMessage)).getText(); }
        catch (Exception e) { return ""; }
    }

    public void clickImport() {
        WebElement importBtnElem = wait.until(ExpectedConditions.presenceOfElementLocated(btnImport));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", importBtnElem);
    }

    public boolean isImportModalVisible() {
        try { return new WebDriverWait(driver, Duration.ofSeconds(3)).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@type='file']"))).isDisplayed(); }
        catch (Exception e) { return false; }
    }
}
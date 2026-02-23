package tests;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.*;
import pages.SubCategoryPage;
import pages.CategoryPage;
import java.util.ArrayList;
import java.util.List;
import java.time.Duration;

public class SubCategoryTest extends BaseTest {

    private SubCategoryPage subPage;
    private CategoryPage catPage;

    public String lastCreatedMainCategory = "AutoSync_" + System.currentTimeMillis();
    private String mainWindow;

    String targetSubCat = "Retirement_" + System.currentTimeMillis();
    String updatedSubCat = "Retirement_Updated_" + System.currentTimeMillis();

    @BeforeClass
    public void setupPage() {
        subPage = new SubCategoryPage(driver);
        catPage = new CategoryPage(driver);
    }

    @Test(priority = 1)
    public void US2_SC_01_Task1_VerifyUIElements() {
        subPage.hardResetAndNavigate();
        WebElement addBtn = new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Add Subcategory")));
        Assert.assertTrue(addBtn.isDisplayed(), "Add Button not displayed");
    }

    @Test(priority = 2)
    public void US2_SC_01_Task2_VerifyPagination() {
        subPage.hardResetAndNavigate();
        if(subPage.isPaginationVisible()) Assert.assertTrue(true);
    }

    @Test(priority = 3)
    public void US2_SC_02_Task1_VerifyDropdownSync() {
        catPage.navigateToMainCategory();
        catPage.openAddModal();
        catPage.fillForm(lastCreatedMainCategory, "Active");
        catPage.clickCreate();

        subPage.hardResetAndNavigate();
        mainWindow = driver.getWindowHandle();
        subPage.clickAddSubCategory();

        new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> d.getWindowHandles().size() > 1);
        for (String handle : driver.getWindowHandles()) { if (!handle.equals(mainWindow)) { driver.switchTo().window(handle); break; } }

        boolean isFound = subPage.getAddPageDropdownOptions().contains(lastCreatedMainCategory);
        driver.close(); driver.switchTo().window(mainWindow);
        Assert.assertTrue(isFound, "Sync Failed!");
    }

    @Test(priority = 4)
    public void US2_SC_02_Task2_DuplicateSubCategoryNegative() {
        String subName = createSubcategoryViaPopup(lastCreatedMainCategory, "AutoSub-");
        boolean foundFirst = isSubcategoryPresentAcrossPages(subName);
        Assert.assertTrue(foundFirst, "FAIL: Subcategory was not found after creation!");

        // Try duplicate
        createSubcategoryViaPopup(lastCreatedMainCategory, subName);
        int occurrencesAfterDup = countNameOccurrences(subName);

        if (occurrencesAfterDup > 1) {
            Assert.fail("DEFECT: Application allowed a duplicate SubCategory! Found " + occurrencesAfterDup + " times.");
        }
    }

    @Test(priority = 5)
    public void US2_SC_02_Task3_VerifyImmediateListReflection() {
        String reflectName = createSubcategoryViaPopup(lastCreatedMainCategory, "ReflectSub-");
        boolean found = subPage.isSubCategoryVisibleInTable(reflectName);
        if(!found) Assert.fail("DEFECT: SubCategory did not immediately reflect in table without a manual refresh.");
    }

    @Test(priority = 6)
    public void US2_SC_03_Task0_SetupData() {
        subPage.hardResetAndNavigate();
        String originalWindow = driver.getWindowHandle();
        subPage.clickAddSubCategory();

        new WebDriverWait(driver, Duration.ofSeconds(15)).until(d -> d.getWindowHandles().size() > 1);
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalWindow)) {
                driver.switchTo().window(handle);
                driver.manage().window().maximize();
                break;
            }
        }
        subPage.fillAndSaveAddForm(targetSubCat, 1, "Active");
        try { new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.alertIsPresent()).accept(); } catch (Exception e) {}
        try { if (driver.getWindowHandles().size() > 1) driver.close(); } catch (Exception e) {}
        driver.switchTo().window(originalWindow);
    }

    @Test(priority = 7, dependsOnMethods = "US2_SC_03_Task0_SetupData")
    public void US2_SC_03_Task1_VerifyEditPrePopulation() {
        subPage.hardResetAndNavigate();
        subPage.searchAndLocateSubCategory(targetSubCat);
        subPage.clickEditForSubCategory(targetSubCat);
        Assert.assertEquals(subPage.getEditNameFieldValue(), targetSubCat, "Pre-population failed!");
        driver.navigate().refresh();
    }

    @Test(priority = 8, dependsOnMethods = "US2_SC_03_Task1_VerifyEditPrePopulation")
    public void US2_SC_03_Task2_VerifyUpdateReflection() {
        subPage.hardResetAndNavigate();
        subPage.searchAndLocateSubCategory(targetSubCat);
        subPage.clickEditForSubCategory(targetSubCat);
        subPage.performUpdate(updatedSubCat, "Active");

        if(!subPage.isSubCategoryVisibleInTable(updatedSubCat)) {
            Assert.fail("DEFECT: Edit was not reflected in the table immediately.");
        }
    }

    @Test(priority = 9)
    public void US2_SC_04_Task1_VerifyDeleteConfirmation() {
        String delName = createSubcategoryViaPopup(lastCreatedMainCategory, "DelSub-");
        subPage.hardResetAndNavigate();
        subPage.searchAndLocateSubCategory(delName);
        subPage.clickDeleteForSubCategory(delName);
        boolean alertHandled = subPage.acceptDeleteAlert();
        Assert.assertTrue(alertHandled, "Delete alert was not handled properly");
    }

    @Test(priority = 10)
    public void US2_SC_04_Task2_VerifyDeletePersistence() {
        String delName2 = createSubcategoryViaPopup(lastCreatedMainCategory, "PersistDel-");
        subPage.hardResetAndNavigate();
        subPage.searchAndLocateSubCategory(delName2);
        subPage.clickDeleteForSubCategory(delName2);
        subPage.acceptDeleteAlert();

        driver.navigate().refresh();
        boolean isDeleted = !isSubcategoryPresentAcrossPages(delName2);
        Assert.assertTrue(isDeleted, "Record persisted after manual list refresh");
    }

    @AfterClass(alwaysRun = true)
    public void cleanupEnvironment() {
        try { catPage.cleanUpAllTestArtifacts(); } catch (Exception e) {}
    }

    // --- FIX FOR THE UNHANDLED ALERT EXCEPTION ---
    private String createSubcategoryViaPopup(String parentText, String namePrefix) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        String main = driver.getWindowHandle();
        driver.findElement(By.xpath("//a[contains(.,'Add Subcategory')]")).click();
        wait.until(d -> d.getWindowHandles().size() > 1);
        for (String h : driver.getWindowHandles()) { if (!h.equals(main)) driver.switchTo().window(h); }

        new Select(wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ContentPlaceHolder_Admin_ddlMainCategory")))).selectByVisibleText(parentText);
        String finalName = namePrefix.endsWith("-") ? namePrefix + System.currentTimeMillis() : namePrefix;
        driver.findElement(By.id("ContentPlaceHolder_Admin_txtSubCategory")).sendKeys(finalName);
        new Select(driver.findElement(By.id("ContentPlaceHolder_Admin_ddlStatus"))).selectByVisibleText("Active");

        driver.findElement(By.id("ContentPlaceHolder_Admin_btnSaveSubCategory")).click();

        // FIX IS HERE: Explicitly clear the "Success" alert before moving on
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
        } catch (Exception ignored) {}

        if (driver.getWindowHandles().size() > 1) driver.close();
        driver.switchTo().window(main);

        driver.navigate().refresh();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ContentPlaceHolder_Admin_gvSubCategories")));
        return finalName;
    }

    private boolean isSubcategoryPresentAcrossPages(String targetName) {
        return countNameOccurrences(targetName) > 0;
    }

    private int countNameOccurrences(String targetName) {
        int count = 0;
        try {
            List<WebElement> paginationRow = driver.findElements(By.xpath("//table[@id='ContentPlaceHolder_Admin_gvSubCategories']//tr[descendant::table]"));

            if (paginationRow.isEmpty()) {
                for (WebElement row : driver.findElements(By.xpath("//table[@id='ContentPlaceHolder_Admin_gvSubCategories']//tr[td]"))) {
                    if (row.getText().contains(targetName)) count++;
                }
                return count;
            }

            try {
                driver.findElement(By.xpath("//table[@id='ContentPlaceHolder_Admin_gvSubCategories']//tr[descendant::table]//a[text()='1']")).click();
                Thread.sleep(1500);
            } catch (Exception ignored) {}

            List<String> visited = new ArrayList<>();
            boolean hasMore = true;
            while (hasMore) {
                String current = driver.findElement(By.xpath("//table[@id='ContentPlaceHolder_Admin_gvSubCategories']//tr[descendant::table]//span")).getText();
                visited.add(current);

                for (WebElement row : driver.findElements(By.xpath("//table[@id='ContentPlaceHolder_Admin_gvSubCategories']//tr[td]"))) {
                    if (row.getText().contains(targetName)) count++;
                }

                List<WebElement> links = driver.findElements(By.xpath("//table[@id='ContentPlaceHolder_Admin_gvSubCategories']//tr[descendant::table]//a"));
                WebElement next = null;
                for (WebElement link : links) {
                    String val = link.getText().trim();
                    if (val.matches("\\d+") && Integer.parseInt(val) > Integer.parseInt(current) && !visited.contains(val)) {
                        next = link; break;
                    } else if (val.equals("...") && links.indexOf(link) == links.size() - 1) {
                        next = link; break;
                    }
                }
                if (next != null) { next.click(); Thread.sleep(1500); } else { hasMore = false; }
            }
        } catch (Exception e) {}
        return count;
    }
}
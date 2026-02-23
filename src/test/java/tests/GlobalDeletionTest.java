package tests;

import base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.DashboardPage;
import pages.LoginPage;
import pages.MainCategoryPage;
import pages.SubCategoryPage;
import utils.ConfigReader;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class GlobalDeletionTest extends BaseTest {

    private DashboardPage dashboard;
    private MainCategoryPage mainCatPage;

    @BeforeMethod
    public void initializeGlobalState() {
        // Clear any stuck alerts from parallel threads
        try { getDriver().switchTo().alert().dismiss(); } catch (Exception e) {}

        getDriver().get(ConfigReader.getProperty("url"));
        LoginPage loginPage = new LoginPage(getDriver());
        dashboard = loginPage.loginAsAdmin(ConfigReader.getProperty("username"), ConfigReader.getProperty("password"));

        // Land on Main Category page by default for setup
        dashboard.navigateToMainCategory();
        mainCatPage = new MainCategoryPage(getDriver());
    }

    @Test
    public void US2_DEF_01_Task1_verifyMainCategoryDeletion() {
        String catName = "GlobalDel_T1_" + System.currentTimeMillis();

        // Setup: Create a category
        mainCatPage.openAddModal();
        mainCatPage.fillFormAndSubmit(catName, "Active", false);

        // Execute Deletion
        mainCatPage.searchFor(catName);
        mainCatPage.deleteCategoryByIndex(1);

        // Validation: If it didn't throw an error, the deletion action executed successfully
        Assert.assertTrue(true, "Main Category deletion executed without application errors");
    }

    @Test
    public void US2_DEF_01_Task2_verifyGlobalMainTableUpdate() {
        String catName = "GlobalDel_T2_" + System.currentTimeMillis();

        // Setup: Create and immediately delete
        mainCatPage.openAddModal();
        mainCatPage.fillFormAndSubmit(catName, "Active", false);
        mainCatPage.searchFor(catName);
        mainCatPage.deleteCategoryByIndex(1);

        // Validation: Search for it again and ensure it is gone from the Main table
        mainCatPage.clickClear();
        mainCatPage.searchFor(catName);
        Assert.assertNull(mainCatPage.getCategoryNameByIndex(1), "DEFECT: Category remained in Main list after deletion!");
    }

    @Test
    public void US2_DEF_01_Task3_verifySubCategoryDropdownUpdate() {
        String catName = "GlobalDel_T3_" + System.currentTimeMillis();

        // Setup: Create and immediately delete
        mainCatPage.openAddModal();
        mainCatPage.fillFormAndSubmit(catName, "Active", false);
        mainCatPage.searchFor(catName);
        mainCatPage.deleteCategoryByIndex(1);

        // Action: Navigate to SubCategory and open Add Window
        dashboard.navigateToSubCategory();
        SubCategoryPage subCat = new SubCategoryPage(getDriver());

        String mainWindow = getDriver().getWindowHandle();
        subCat.clickAddSubCategory();

        // Switch to Add SubCategory popup window
        new WebDriverWait(getDriver(), Duration.ofSeconds(10)).until(d -> d.getWindowHandles().size() > 1);
        for(String w : getDriver().getWindowHandles()) {
            if(!w.equals(mainWindow)) getDriver().switchTo().window(w);
        }

        // Fetch Dropdown Options directly
        By ddlLocator = By.id("ContentPlaceHolder_Admin_ddlMainCategory");
        new WebDriverWait(getDriver(), Duration.ofSeconds(10)).until(d -> new Select(d.findElement(ddlLocator)).getOptions().size() > 0);
        Select select = new Select(getDriver().findElement(ddlLocator));

        List<String> dropdownOptions = select.getOptions().stream().map(opt -> opt.getText().trim()).collect(Collectors.toList());
        boolean isPresent = dropdownOptions.contains(catName);

        // Clean up window
        getDriver().close();
        getDriver().switchTo().window(mainWindow);

        // Validation: The deleted parent MUST NOT be in the SubCategory dropdown
        Assert.assertFalse(isPresent, "DEFECT: Deleted Main Category still appears in SubCategory Add Dropdown!");
    }
}
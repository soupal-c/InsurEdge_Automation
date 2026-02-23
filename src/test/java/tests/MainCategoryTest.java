package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.LoginPage;
import pages.DashboardPage;
import pages.MainCategoryPage;
import pages.SubCategoryPage;
import pages.AddSubCategoryPage;
import utils.ConfigReader;

public class MainCategoryTest extends BaseTest {

    private MainCategoryPage mainCatPage;

    @BeforeMethod
    public void setupTest() {
        getDriver().get(ConfigReader.getProperty("url"));
        LoginPage loginPage = new LoginPage(getDriver());
        DashboardPage dashboard = loginPage.loginAsAdmin(ConfigReader.getProperty("username"), ConfigReader.getProperty("password"));
        dashboard.navigateToMainCategory();
        mainCatPage = new MainCategoryPage(getDriver());
    }

    @Test
    public void US2_MC_01_Task1_VerifyUI() {
        Assert.assertEquals(mainCatPage.getPageHeaderText(), "Create Main Insurance Category");
        Assert.assertTrue(mainCatPage.isBreadcrumbDisplayed(), "Breadcrumb is not visible");
        Assert.assertEquals(mainCatPage.getTableHeaderCount(), 3, "Table header count mismatch");
    }

    @Test
    public void US2_MC_01_Task2_VerifyRowActionIcons() {
        String name = "IconTest_" + System.currentTimeMillis();
        mainCatPage.openAddModal();
        mainCatPage.fillFormAndSubmit(name, "Active", false);
        mainCatPage.searchFor(name);
        Assert.assertTrue(mainCatPage.areRowActionIconsVisible(1), "Icons missing on row!");
    }

    @Test
    public void US2_MC_02_Task1_SearchFunctionality() {
        String name = "SearchTest_" + System.currentTimeMillis();
        mainCatPage.openAddModal();
        mainCatPage.fillFormAndSubmit(name, "Active", false);
        mainCatPage.searchFor(name);
        Assert.assertEquals(mainCatPage.getCategoryNameByIndex(1), name, "Search failed to isolate record");
    }

    @Test
    public void US2_MC_02_Task2_AddCategory() {
        String name = "AddTest_" + System.currentTimeMillis();
        mainCatPage.openAddModal();
        mainCatPage.fillFormAndSubmit(name, "Active", false);
        mainCatPage.searchFor(name);
        Assert.assertEquals(mainCatPage.getCategoryNameByIndex(1), name, "Add failed");
    }

    @Test
    public void US2_MC_02_Task3_EditCategory() {
        String name = "PreEdit_" + System.currentTimeMillis();
        String editedName = "PostEdit_" + System.currentTimeMillis();

        mainCatPage.openAddModal();
        mainCatPage.fillFormAndSubmit(name, "Active", false);

        mainCatPage.searchFor(name);
        mainCatPage.editCategoryByIndex(1, editedName);

        mainCatPage.clickClear();
        mainCatPage.searchFor(editedName);
        Assert.assertEquals(mainCatPage.getCategoryNameByIndex(1), editedName, "Edit failed to update grid");
    }

    @Test
    public void US2_MC_02_Task4_DuplicateCheck() {
        String name = "DupeTest_" + System.currentTimeMillis();
        mainCatPage.openAddModal();
        mainCatPage.fillFormAndSubmit(name, "Active", false);

        mainCatPage.openAddModal();
        mainCatPage.fillFormAndSubmit(name, "Active", false); // App bug fails to close modal, throws error internally
        mainCatPage.clickCreateExpectingFailure();

        Assert.assertTrue(mainCatPage.getErrorMessage().contains("exists"), "Duplicate validation missing");
    }

    @Test
    public void US2_MC_03_Task1_VerifyDelete() {
        String name = "DeleteTest_" + System.currentTimeMillis();
        mainCatPage.openAddModal();
        mainCatPage.fillFormAndSubmit(name, "Active", false);

        mainCatPage.searchFor(name);
        mainCatPage.deleteCategoryByIndex(1);

        mainCatPage.clickClear();
        mainCatPage.searchFor(name);
        Assert.assertNotEquals(mainCatPage.getCategoryNameByIndex(1), name, "Category should be deleted!");
    }

    @Test
    public void US2_MC_03_Task2_DeleteCancellation() {
        String name = "CancelTest_" + System.currentTimeMillis();
        mainCatPage.openAddModal();
        mainCatPage.fillFormAndSubmit(name, "Active", false);

        mainCatPage.searchFor(name);
        mainCatPage.deleteCategoryByIndexAndCancel(1);

        Assert.assertEquals(mainCatPage.getCategoryNameByIndex(1), name, "Category deleted despite cancel!");
    }

    @Test
    public void US2_MC_03_Task2_ValidateDependencyBlocking() {
        String parent = "Parent_" + System.currentTimeMillis();
        String child = "Child_" + System.currentTimeMillis();

        // 1. Create Parent
        mainCatPage.openAddModal();
        mainCatPage.fillFormAndSubmit(parent, "Active", false);

        // 2. Create Child
        DashboardPage dash = new DashboardPage(getDriver());
        dash.navigateToSubCategory();
        SubCategoryPage subPage = new SubCategoryPage(getDriver());
        AddSubCategoryPage addPage = subPage.clickAddSubCategory();

        String mainWin = getDriver().getWindowHandle();
        for (String w : getDriver().getWindowHandles()) if (!w.equals(mainWin)) getDriver().switchTo().window(w);
        addPage.createSubCategory(parent, child, "Active");
        getDriver().switchTo().window(mainWin);

        // 3. Try to Delete Parent
        dash.navigateToMainCategory();
        mainCatPage.searchFor(parent);
        mainCatPage.deleteCategoryByIndex(1);

        // GENUINE BUG VALIDATION: The parent MUST still exist. If it doesn't, the test fails to expose the defect.
        mainCatPage.clickClear();
        mainCatPage.searchFor(parent);
        if (mainCatPage.getCategoryNameByIndex(1) == null) {
            Assert.fail("DEFECT: System allowed deletion of parent category '" + parent + "' even though it had a linked subcategory!");
        }
    }

    @Test
    public void US2_MC_04_Task1_VerifyImportSummary() {
        mainCatPage.clickImport();
        if (!mainCatPage.isImportModalVisible()) {
            Assert.fail("DEFECT: Import summary modal did not appear when clicked!");
        }
    }

    @Test
    public void US2_MC_04_Task2_VerifyImportListRefresh() {
        mainCatPage.clickImport();
        if (!mainCatPage.getErrorMessage().contains("Success")) {
            Assert.fail("DEFECT: List did not refresh automatically after import attempt!");
        }
    }
}
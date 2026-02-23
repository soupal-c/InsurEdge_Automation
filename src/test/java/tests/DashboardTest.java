package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.LoginPage;
import pages.DashboardPage;
import utils.ConfigReader;

public class DashboardTest extends BaseTest {

    private DashboardPage dashboardPage;

    @BeforeMethod
    public void setupDashboardPreconditions() {
        getDriver().get(ConfigReader.getProperty("url")); // Reset to login page
        LoginPage loginPage = new LoginPage(getDriver());
        dashboardPage = loginPage.loginAsAdmin(ConfigReader.getProperty("username"), ConfigReader.getProperty("password"));
    }

    @Test
    public void US2_DASH_01_Task1_verifyDashboardWidgetsLoad() {
        // Task 1: Automate verification that key statistical widgets load properly upon login.
        Assert.assertTrue(dashboardPage.isDashboardHeaderDisplayed(), "Dashboard header did not load");
        Assert.assertNotNull(dashboardPage.getRegisteredUsers(), "Registered Users widget is empty");
        Assert.assertNotNull(dashboardPage.getListedPolicies(), "Listed Policies widget is empty");
    }

    @Test
    public void US2_DASH_01_Task2_verifySidebarNavigationToCategory() {
        // Task 2: Implement UI navigation flow to the Main Category management page.
        dashboardPage.navigateToMainCategory();
        // Standard check to ensure URL routed correctly
        Assert.assertTrue(getDriver().getCurrentUrl().contains("AdminCreateMainCategory"), "Failed to navigate to Main Category page");
    }

    @Test
    public void US2_DASH_01_Task3_verifyLogoutFunctionality() {
        // Task 3: Automate the logout flow and verify redirection to the login screen.
        dashboardPage.logout();
        Assert.assertTrue(getDriver().getCurrentUrl().contains("LoginPage"), "Logout failed to redirect to Login screen");
    }
}
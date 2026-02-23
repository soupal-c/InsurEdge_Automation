package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.LoginPage;
import pages.DashboardPage;
import utils.ConfigReader;

public class LoginTest extends BaseTest {

    private LoginPage loginPage;

    @BeforeMethod
    public void initPage() {
        // This line FORCES the browser back to the login screen before every single test
        getDriver().get(ConfigReader.getProperty("url"));
        loginPage = new LoginPage(getDriver());
    }

    @Test
    public void US2_LOG_01_Task1_verifyValidLogin() {
        // Task 1: Automate successful login flow and verify redirection to Dashboard.
        DashboardPage dashboard = loginPage.loginAsAdmin(ConfigReader.getProperty("username"), ConfigReader.getProperty("password"));
        Assert.assertTrue(dashboard.isDashboardHeaderDisplayed(), "Failed to route to Dashboard after valid login");
    }

    @Test
    public void US2_LOG_01_Task2_verifyInvalidCredentials() {
        // Task 2: Script validation for invalid credentials and ensure proper error message displays.
        loginPage.enterUsername("invalid_user");
        loginPage.enterPassword("WrongPassword!");
        loginPage.clickLogin();
        Assert.assertTrue(loginPage.getErrorMessage().contains("Invalid"), "Error message not displayed for invalid login");
    }

    @Test
    public void US2_LOG_01_Task3_verifyEmptyFieldsValidation() {
        // Task 3: Automate negative testing for empty username and password fields.
        loginPage.clickLogin();
        // The HTML uses HTML5 'needs-validation', so we check if the URL didn't change or if standard JS error pops up
        Assert.assertEquals(getDriver().getTitle(), "Pages / Login - InsurEdge", "System allowed login with empty fields");
    }
}
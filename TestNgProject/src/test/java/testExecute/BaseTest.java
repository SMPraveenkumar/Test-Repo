package testExecute;

import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.Markup;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.ChartLocation;
import com.aventstack.extentreports.reporter.configuration.Theme;

import io.github.bonigarcia.wdm.WebDriverManager;
import utils.MonitoringMail;
import utils.TestConfig;

public class BaseTest {

	public static WebDriver driver;
	public static WebElement element;
	public static JavascriptExecutor js;
	public static Select select;
	public static Robot robot;
	public static Actions actions;
	public static WebDriverWait wait;
	public static Row row;
	public static Cell cell;
	public static File file;
	public static Workbook workbook;
	public static Sheet sheet;
	public static FileOutputStream fo;
	public static TakesScreenshot ts;
	public static FileInputStream fi;
	public static Properties config;
	public static Properties or;
	public static String elementText;
	public static ExtentHtmlReporter htmlReporter;
	public static ExtentReports extent;
	public static ExtentTest test;
	public static String screenshotPath;
	public static Logger log;
	public static String dataFileType;
	public static String dataFileName;

	public BaseTest() {

		try {

			// Setting TimeZone
			TimeZone.setDefault(TimeZone.getTimeZone("IST"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@BeforeSuite
	public void set() {

		try {
			// Log4j file is loading...!!!!!
			log = Logger.getLogger(BaseTest.class.getName());
			PropertyConfigurator.configure(".\\src\\test\\resources\\properties\\log4j.properties");

			// OR.properties file is loading...!!!!!
			fi = new FileInputStream(".\\src\\test\\resources\\properties\\OR.properties");
			or = new Properties();
			or.load(fi);

			// config.properties file is loading...!!!!!
			fi = new FileInputStream(".\\src\\test\\resources\\properties\\config.properties");
			config = new Properties();
			config.load(fi);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@BeforeClass
	public void setup() {

		try {
			SimpleDateFormat sdf = new SimpleDateFormat("MMMM-dd-HH-mm");
			String timestamp = sdf.format(new Date());
			htmlReporter = new ExtentHtmlReporter(
					System.getProperty("user.dir") + "//reports//TestReport " + timestamp + ".html");
			htmlReporter.config().setDocumentTitle("Automation Test Report");
			htmlReporter.config().setReportName("Test Results");
			htmlReporter.config().setTheme(Theme.STANDARD);
			htmlReporter.config().setTestViewChartLocation(ChartLocation.TOP);
			htmlReporter.setAppendExisting(false);
			extent = new ExtentReports();
			extent.attachReporter(htmlReporter);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@BeforeMethod
	public void initializeWebDriver(ITestResult result) {

		try {
			startWebDriver();
			maximizeWindow();
			deleteAllCookies();
			implicitWait();
			pageLoadTimeout();
			test = extent.createTest(" @Test Case : " + result.getMethod().getMethodName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@AfterMethod
	public void addResultAndCloseDriver(ITestResult result) {

		try {
			if (result.getStatus() == ITestResult.SUCCESS) {
				String methodName = result.getMethod().getMethodName();
				String logText = "Test Case :  " + methodName + " is Passed..!!!";
				Markup m = MarkupHelper.createLabel(logText, ExtentColor.GREEN);
				test.log(Status.PASS, m);
				log.info(methodName + " method has been passed");
			} else if (result.getStatus() == ITestResult.FAILURE) {
				String methodName = result.getMethod().getMethodName();
				String logText = "Test Case :  " + methodName + " is Failed..!!!";
				Markup m = MarkupHelper.createLabel(logText, ExtentColor.RED);
				screenshotPath = captureScreenshot(result.getName());
				String baseImage = captureScreenshotAsBase64();
				test.log(Status.FAIL, m);
				log.info(methodName + " method has been failed");
				test.fail("Failed Step", MediaEntityBuilder.createScreenCaptureFromBase64String(baseImage).build());
			} else if (result.getStatus() == ITestResult.SKIP) {
				String methodName = result.getMethod().getMethodName();
				String logText = "Test Case :  " + methodName + " is Skipped..!!!";
				Markup m = MarkupHelper.createLabel(logText, ExtentColor.ORANGE);
				test.log(Status.SKIP, m);
				log.info(methodName + " method has been skipped");
			}
			driver.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@AfterClass
	public void tearDown() {

		try {
			extent.flush();
			driver.quit();
			log.info("Test Execution is completed !!!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public WebDriver startWebDriver() {

		try {
			switch (config.getProperty("browser")) {
			case "chrome":
//				ChromeOptions option = new ChromeOptions();
//				option.addArguments("incognito");
				WebDriverManager.chromedriver().setup();
				// driver = new ChromeDriver(option);
				driver = new ChromeDriver();
				log.info("Chrome Browser is Opened");
				break;

			case "firefox":
				FirefoxOptions firefox = new FirefoxOptions();
				firefox.addArguments("incognito");
				WebDriverManager.firefoxdriver().setup();
				driver = new FirefoxDriver(firefox);
				log.info("Firefox Browser is Opened");
				break;

			case "edge":
				EdgeOptions edge = new EdgeOptions();
				edge.addArguments("incognito");
				WebDriverManager.edgedriver().setup();
				driver = new EdgeDriver(edge);
				log.info("Edge Browser is Opened");
				break;

			default:
				throw new IllegalArgumentException("Browser mentioned is wrong");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return driver;
	}

	/**
	 * This will help to perform SendKeys operation
	 */
	public boolean sendKeys(String locator, String value) {

		boolean result = false;
		try {
			waitForVisibilityOf(locator);
			waitForClickabilityOf(locator);
			if (or.getProperty(locator).startsWith("//") || or.getProperty(locator).startsWith("(")) {
				driver.findElement(By.xpath(or.getProperty(locator))).sendKeys(value);
			} else {
				driver.findElement(By.id(or.getProperty(locator))).sendKeys(value);
			}
			log.info("'" + value + "' input is passed to the '" + locator + "'");
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Failed to pass the " + "'" + value + "'" + " to the '" + locator + "'");
			result = false;
		}
		return result;
	}

	/**
	 * This will help to perform the Click Operation
	 */
	public boolean click(String locator) {

		boolean result = false;
		try {
			waitForVisibilityOf(locator);
			waitForClickabilityOf(locator);

			if (or.getProperty(locator).startsWith("//") || or.getProperty(locator).startsWith("(")) {
				driver.findElement(By.xpath(or.getProperty(locator))).click();
			} else {
				driver.findElement(By.id(or.getProperty(locator))).click();
			}
			log.info("Clicked on the " + "'" + locator + "'");
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Failed to click on the " + "'" + locator + "'");
			result = false;
		}
		return result;
	}

	/**
	 * This will help to clear the inputs entered on the textBox
	 */
	public boolean clear(String locator) {

		boolean result = false;
		try {
			waitForVisibilityOf(locator);
			waitForClickabilityOf(locator);
			if (locator.endsWith("xpath")) {
				driver.findElement(By.xpath(or.getProperty(locator))).clear();
			} else if (locator.endsWith("id")) {
				driver.findElement(By.id(or.getProperty(locator))).clear();
			} else if (locator.endsWith("name")) {
				driver.findElement(By.name(or.getProperty(locator))).clear();
			}
			result = true;
		} catch (Exception e) {
			log.error("Failed to clear the " + locator);
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * This will perform click operation using JavaScript
	 */
	public boolean jsClick(String locator) {

		boolean result = false;
		try {
			waitForVisibilityOf(locator);
			waitForClickabilityOf(locator);
			if (or.getProperty(locator).startsWith("//") || or.getProperty(locator).startsWith("(")) {
				element = driver.findElement(By.xpath(or.getProperty(locator)));
			} else {
				element = driver.findElement(By.id(or.getProperty(locator)));
			}
			js = (JavascriptExecutor) driver;
			js.executeScript("arguments[0].click()", element);
			log.info("Clicked on the " + "'" + locator + "'");
			result = true;
		} catch (Exception e) {
			log.error("Failed to click on the " + "'" + locator + "'");
			e.printStackTrace();
			result = false;
		}
		return result;
	}

	/**
	 * This will help to perform SendKeys operation for values reading from excel
	 * using JavaScript
	 */
	public void jsSendKeys(Object obj, String userValue) {

		js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].setAttribute('value','" + userValue + "')", obj);
	}

	public void jsSendKeys(String id, String value) {

		js = ((JavascriptExecutor) driver);
		js.executeScript("document.getElementById('" + or.getProperty(id) + "').value='" + value + "';");
	}

	public void jsClickByXpath(String locator) {

		element = driver.findElement(By.xpath(locator));
		js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].click()", element);
	}

	/**
	 * This will help to select the DropDown value using VisibleText method
	 */
	public boolean dropDown(String locator, String value) {

		boolean result = false;
		try {
			waitForVisibilityOf(locator);
			waitForClickabilityOf(locator);
			if (or.getProperty(locator).startsWith("//") || or.getProperty(locator).startsWith("(")) {
				element = driver.findElement(By.xpath(or.getProperty(locator)));
			} else {
				element = driver.findElement(By.id(or.getProperty(locator)));
			}
			select = new Select(element);
			select.selectByVisibleText(value);
			log.info("Selected the " + value + " from " + locator + " dropDown");
			result = true;
		} catch (Exception e) {
			log.error("Failed to select the " + value + " from " + locator + " dropDown");
			e.printStackTrace();
			result = false;
		}
		return result;
	}

	/**
	 * This will help to select the DropDown value using Index method
	 */
	public boolean dropDown(String locator, int i) {

		boolean result = false;
		try {
			waitForVisibilityOf(locator);
			waitForClickabilityOf(locator);
			if (or.getProperty(locator).startsWith("//") || or.getProperty(locator).startsWith("(")) {
				element = driver.findElement(By.xpath(or.getProperty(locator)));
			} else {
				element = driver.findElement(By.id(or.getProperty(locator)));
			}
			select = new Select(element);
			select.selectByIndex(i);
			log.info("Selected the desired option from " + locator + " dropDown");
			result = true;
		} catch (Throwable t) {
			log.error("Failed to select the desired option from dropDown");
			t.getMessage();
			result = false;
		}
		return result;
	}

	// This will help to locate the element using ID
	public WebElement locatorById(String id) {

		return element = driver.findElement(By.id(id));
	}

	// This will help to locate the element using Xpath
	public WebElement locatorByXpath(String xpath) {

		return element = driver.findElement(By.xpath(xpath));
	}

	// This will help to locate the element using Name
	public WebElement locatorByName(String name) {

		return element = driver.findElement(By.name(name));
	}

	// This will help to locate the element using ClassName
	public WebElement locatorByClassName(String claseName) {

		return element = driver.findElement(By.className(claseName));
	}

	// This will help to locate the element using LinkText
	public WebElement locatorByLinkText(String linkText) {

		return element = driver.findElement(By.linkText(linkText));
	}

	// This will help to locate the element using PartiaLinkText
	public WebElement locatorByPartialLinkText(String partialLinkText) {

		return element = driver.findElement(By.partialLinkText(partialLinkText));
	}

	/**
	 * This will help to make WebDriver wait till the Element is Visible
	 */
	public void waitForVisibilityOf(String locator) {

		try {
			wait = new WebDriverWait(driver, Duration.ofSeconds(Integer.parseInt(config.getProperty("webDriverWait"))));
			if (or.getProperty(locator).startsWith("//") || or.getProperty(locator).startsWith("(")) {
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(or.getProperty(locator))));
			} else {
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(or.getProperty(locator))));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This will help to make WebDriver wait till the Element is Clickable
	 */
	public void waitForClickabilityOf(String locator) {

		try {
			wait = new WebDriverWait(driver, Duration.ofSeconds(Integer.parseInt(config.getProperty("webDriverWait"))));
			if (or.getProperty(locator).startsWith("//") || or.getProperty(locator).startsWith("(")) {
				wait.until(ExpectedConditions.elementToBeClickable(By.xpath(or.getProperty(locator))));
			} else {
				wait.until(ExpectedConditions.elementToBeClickable(By.id(or.getProperty(locator))));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This will help to make WebDriver wait till the Element is Invisible
	 */
	public void waitForInvisibilityOf(String locator) {

		try {
			wait = new WebDriverWait(driver, Duration.ofSeconds(Integer.parseInt(config.getProperty("webDriverWait"))));
			if (or.getProperty(locator).startsWith("//") || or.getProperty(locator).startsWith("(")) {
				wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(or.getProperty(locator))));
			} else {
				wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id(or.getProperty(locator))));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This will help to Click the WebElement using JavaScript
	 */
	public void jsClick(WebElement element) {

		js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].click()", element);
	}

	/**
	 * This will help to scroll to the desired WebElement
	 */
	public void scrollToElement(String locator) {

		try {
			if (or.getProperty(locator).startsWith("//") || or.getProperty(locator).startsWith("(")) {
				element = driver.findElement(By.xpath(or.getProperty(locator)));
			} else {
				element = driver.findElement(By.id(or.getProperty(locator)));
			}
			js = (JavascriptExecutor) driver;
			js.executeScript("arguments[0].scrollIntoView();", element);
			log.info("Scrolled to the " + locator);
		} catch (Exception e) {
			log.error("Not able to scroll to the " + " " + locator);
			e.printStackTrace();
		}
	}

	public void waitForVisibilityForLocator(String locator) {

		try {
			wait = new WebDriverWait(driver, Duration.ofSeconds(Integer.parseInt(config.getProperty("webDriverWait"))));
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void scrolldown(String locator) {

		element = driver.findElement(By.xpath(locator));
		js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].scrollIntoView();", element);
	}

	// This will help to implicitly wait for WebElement
	public void implicitWait() {

		driver.manage().timeouts()
				.implicitlyWait(Duration.ofSeconds(Integer.parseInt(config.getProperty("implicitWait"))));
	}

	// This will help to wait till the page is loaded
	public void pageLoadTimeout() {

		driver.manage().timeouts()
				.pageLoadTimeout(Duration.ofSeconds(Integer.parseInt(config.getProperty("pageLoadTimeOut"))));
	}

	public void fluentWait(String locator) {

		try {
			FluentWait<WebDriver> wait = new FluentWait<>(driver)
					.withTimeout(Duration.ofSeconds(Integer.parseInt(config.getProperty("webDriverWait"))))
					.pollingEvery(Duration.ofSeconds(2)).ignoring(NoSuchElementException.class);
			if (or.getProperty(locator).startsWith("//") || or.getProperty(locator).startsWith("(")) {
				element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(or.getProperty(locator))));
			} else {
				element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(or.getProperty(locator))));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void fluentWaitByXpath(String locator) {

		try {
			FluentWait<WebDriver> wait = new FluentWait<>(driver).withTimeout(Duration.ofSeconds(20))
					.pollingEvery(Duration.ofSeconds(2)).ignoring(NoSuchElementException.class);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void waitForClickabilityByXpath(String locator) {

		try {
			wait = new WebDriverWait(driver, Duration.ofSeconds(40));
			wait.until(ExpectedConditions.elementToBeClickable(By.xpath(locator)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This will help to maximize the window
	 */
	public void maximizeWindow() {
		driver.manage().window().maximize();
	}

	/**
	 * This will help to launch the URL
	 */
	public void url(String url) {
		driver.get(url);
	}

	/**
	 * This will help to delete all the cookies before WebDriver is launched
	 */
	public void deleteAllCookies() {
		driver.manage().deleteAllCookies();
	}

	/**
	 * This will make the WebDriver to wait for the given period of time in seconds
	 */
	public void waitForElement(int num) {

		try {
			Thread.sleep(1000 * num);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<WebElement> getElements(String locator) {

		List<WebElement> elementsList = driver.findElements(By.xpath(or.getProperty(locator)));
		return elementsList;
	}

	public void selectAttributeValue(WebElement element, String attributeValue) {
		select = new Select(element);
		select.selectByValue(attributeValue);
	}

	// Navigate
	public void navigateTo(String url) {
		driver.navigate().to(url);
	}

	public void refresh() {
		driver.navigate().refresh();
	}

	public void navigateForward() {
		driver.navigate().forward();
	}

	public void navigateBack() {
		driver.navigate().back();
	}

	public void fullscreen() {
		driver.manage().window().fullscreen();
	}

	/**
	 * This will help to perform MouseHover actions to the desired WebElement
	 */
	public boolean moveToElement(String locator) {

		boolean result = false;
		try {
			if (or.getProperty(locator).startsWith("//") || or.getProperty(locator).startsWith("(")) {
				element = driver.findElement(By.xpath(or.getProperty(locator)));
			} else {
				element = driver.findElement(By.id(or.getProperty(locator)));
			}
			actions = new Actions(driver);
			actions.moveToElement(element).build().perform();
			log.info("Mouse hover to the " + locator);
			result = true;
		} catch (Exception e) {
			log.error("Failed to move to the " + locator);
			result = false;
		}
		return result;
	}

	/**
	 * This will help to perform Drag and Drop actions
	 */
	public boolean dragAndDrop(String locator1, String locator2) {

		boolean result = false;
		WebElement element1 = null;
		WebElement element2 = null;
		try {
			if (or.getProperty(locator1).startsWith("//") || or.getProperty(locator1).startsWith("(")) {
				element1 = driver.findElement(By.xpath(or.getProperty(locator1)));
			} else {
				element1 = driver.findElement(By.id(or.getProperty(locator1)));
			}
			if (or.getProperty(locator2).startsWith("//") || or.getProperty(locator2).startsWith("(")) {
				element2 = driver.findElement(By.xpath(or.getProperty(locator2)));
			} else {
				element2 = driver.findElement(By.id(or.getProperty(locator2)));
			}
			actions = new Actions(driver);
			actions.dragAndDrop(element1, element2).build().perform();
			result = true;
		} catch (Exception e) {
			log.error("Failed to drag and drop the WebElements");
			result = false;
		}
		return result;
	}

	/**
	 * This will help to perform Double click actions on the desired WebElement
	 */
	public boolean doubleClick(String locator) {

		boolean result = false;
		try {
			if (or.getProperty(locator).startsWith("//") || or.getProperty(locator).startsWith("(")) {
				element = driver.findElement(By.xpath(or.getProperty(locator)));
			} else {
				element = driver.findElement(By.id(or.getProperty(locator)));
			}
			actions = new Actions(driver);
			actions.doubleClick(element).build().perform();
			log.error("Double clicked the " + locator);
			result = true;
		} catch (Exception e) {
			log.error("Failed to perform Double click operation");
			result = false;
		}
		return result;
	}

	/**
	 * This will help to perform Right or Context click on the desired WebElement
	 */
	public boolean contextClick(String locator) {

		boolean result = false;
		try {
			if (or.getProperty(locator).startsWith("//") || or.getProperty(locator).startsWith("(")) {
				element = driver.findElement(By.xpath(or.getProperty(locator)));
			} else {
				element = driver.findElement(By.id(or.getProperty(locator)));
			}
			actions = new Actions(driver);
			actions.contextClick(element).build().perform();
			log.info("Right/Context clicked on the " + locator);
			result = true;
		} catch (Exception e) {
			log.error("Failed to right/context click on the " + locator);
			result = false;
		}
		return result;
	}

	public void keyup(Keys key) {

		actions = new Actions(driver);
		actions.keyUp(key).perform();
	}

	public void keyDown(Keys key) {

		actions = new Actions(driver);
		actions.keyDown(key).perform();
	}

	// Alert
	public boolean acceptAlert() {

		boolean result = false;
		try {
			driver.switchTo().alert().accept();
			result = true;
		} catch (Exception e) {
			log.error("Failed to accept the alert");
			e.printStackTrace();
			result = false;
		}
		return result;
	}

	public boolean dismissAlert() {

		boolean result = false;
		try {
			driver.switchTo().alert().dismiss();
			result = true;
		} catch (Exception e) {
			log.error("Failed to dismiss the alert");
			e.printStackTrace();
			result = false;
		}
		return result;
	}

	public boolean promptAlert(String text) {

		boolean result = false;
		try {
			driver.switchTo().alert().sendKeys(text);
			driver.switchTo().alert().accept();
			result = true;
		} catch (Exception e) {
			log.error("Failed to accept the alert");
			result = false;
		}
		return result;
	}

	// Screenshot
	public void screenshot(String path) {

		try {
			ts = (TakesScreenshot) driver;
			File from = ts.getScreenshotAs(OutputType.FILE);
			File to = new File(path);
			FileUtils.copyFile(from, to);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// To capture Screenshot for the failed test cases
	public String captureScreenshot(String tc_name) {

		String destination = null;
		try {
			String dateName = new SimpleDateFormat("EEE_hhmm").format(new Date());
			TakesScreenshot ts = (TakesScreenshot) BaseTest.driver;
			File source = ts.getScreenshotAs(OutputType.FILE);
			destination = System.getProperty("user.dir") + "/screenshots/" + tc_name + "_" + dateName + ".png";
			File finalDestination = new File(destination);
			FileUtils.copyFile(source, finalDestination);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return destination;
	}

	// To capture Screenshot for the failed test cases
	public String captureScreenshotintoBytes(String tc_name) {

		String destination = null;
		try {
			String dateName = new SimpleDateFormat("EEE_hhmm").format(new Date());
			TakesScreenshot ts = (TakesScreenshot) BaseTest.driver;
			byte[] screenshotBytes = ts.getScreenshotAs(OutputType.BYTES);

			// Convert the screenshot bytes to Base64 string
			String base64Screenshot = Base64.getEncoder().encodeToString(screenshotBytes);

			destination = System.getProperty("user.dir") + "/screenshots/" + tc_name + ".png";
			File finalDestination = new File(destination);

			// Decode the Base64 string back to bytes
			byte[] decodedBytes = Base64.getDecoder().decode(base64Screenshot);

			// Write the decoded bytes to the file
			FileUtils.writeByteArrayToFile(finalDestination, decodedBytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return destination;
	}

	// Insert value
	public void jsInsertvalue(String locator, String text) {

		try {
			if (or.getProperty(locator).startsWith("//") || or.getProperty(locator).startsWith("(")) {
				element = driver.findElement(By.xpath(or.getProperty(locator)));
			} else {
				element = driver.findElement(By.id(or.getProperty(locator)));
			}
			js = (JavascriptExecutor) driver;
			js.executeScript("arguments[0].setAttribute('value','" + text + "')", element);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Object jsGetattribute(String locator) {

		if (or.getProperty(locator).startsWith("//") || or.getProperty(locator).startsWith("(")) {
			element = driver.findElement(By.xpath(or.getProperty(locator)));
		} else {
			element = driver.findElement(By.id(or.getProperty(locator)));
		}
		js = (JavascriptExecutor) driver;
		Object executeScript = js.executeScript("returnarguments[0].getattribute('value')", element);
		return executeScript;
	}

	public void jsScrollDown(String locator) {

		try {
			if (or.getProperty(locator).startsWith("//") || or.getProperty(locator).startsWith("(")) {
				element = driver.findElement(By.xpath(or.getProperty(locator)));
			} else {
				element = driver.findElement(By.id(or.getProperty(locator)));
			}
			js = (JavascriptExecutor) driver;
			js.executeScript("arguments[0].scrollIntoView(true)", element);
			log.info("Scrolled down to the Web Page");
		} catch (Exception e) {
			log.error("Failed to scroll down the webPage");
			e.printStackTrace();
		}
	}

	public void jsScrollUp(String locator) {

		try {
			if (or.getProperty(locator).startsWith("//") || or.getProperty(locator).startsWith("(")) {
				element = driver.findElement(By.xpath(or.getProperty(locator)));
			} else {
				element = driver.findElement(By.id(or.getProperty(locator)));
			}
			js = (JavascriptExecutor) driver;
			js.executeScript("arguments[0].scrollIntoView(false)", element);
			log.info("Scrolled up to the Web Page");
		} catch (Exception e) {
			log.error("Failed to scroll up the webPage");
			e.printStackTrace();
		}
	}

	public List<WebElement> getOptions(String locator) {

		if (or.getProperty(locator).startsWith("//") || or.getProperty(locator).startsWith("(")) {
			element = driver.findElement(By.xpath(or.getProperty(locator)));
		} else {
			element = driver.findElement(By.id(or.getProperty(locator)));
		}
		select = new Select(element);
		List<WebElement> options = select.getOptions();
		return options;
	}

	public List<WebElement> getAllSelectedOptions(String locator) {

		if (or.getProperty(locator).startsWith("//") || or.getProperty(locator).startsWith("(")) {
			element = driver.findElement(By.xpath(or.getProperty(locator)));
		} else {
			element = driver.findElement(By.id(or.getProperty(locator)));
		}
		select = new Select(element);
		List<WebElement> options = select.getAllSelectedOptions();
		return options;
	}

	public WebElement getFirstSelectedOption(String locator) {

		if (or.getProperty(locator).startsWith("//") || or.getProperty(locator).startsWith("(")) {
			element = driver.findElement(By.xpath(or.getProperty(locator)));
		} else {
			element = driver.findElement(By.id(or.getProperty(locator)));
		}
		select = new Select(element);
		WebElement options = select.getFirstSelectedOption();
		return options;
	}

	public Boolean isMultiple(String locator) {

		if (or.getProperty(locator).startsWith("//") || or.getProperty(locator).startsWith("(")) {
			element = driver.findElement(By.xpath(or.getProperty(locator)));
		} else {
			element = driver.findElement(By.id(or.getProperty(locator)));
		}
		select = new Select(element);
		boolean multiple = select.isMultiple();
		return multiple;
	}

	// DeSelect
	public void deselectByIndex(String locator, int i) {

		if (or.getProperty(locator).startsWith("//") || or.getProperty(locator).startsWith("(")) {
			element = driver.findElement(By.xpath(or.getProperty(locator)));
		} else {
			element = driver.findElement(By.id(or.getProperty(locator)));
		}
		select = new Select(element);
		select.deselectByIndex(i);
	}

	public void deselectByValue(String locator, String value) {

		if (or.getProperty(locator).startsWith("//") || or.getProperty(locator).startsWith("(")) {
			element = driver.findElement(By.xpath(or.getProperty(locator)));
		} else {
			element = driver.findElement(By.id(or.getProperty(locator)));
		}
		select = new Select(element);
		select.deselectByValue(value);
	}

	public void deselectByText(String locator, String text) {

		if (or.getProperty(locator).startsWith("//") || or.getProperty(locator).startsWith("(")) {
			element = driver.findElement(By.xpath(or.getProperty(locator)));
		} else {
			element = driver.findElement(By.id(or.getProperty(locator)));
		}
		select = new Select(element);
		select.deselectByVisibleText(text);
	}

	public void deselectAll(String locator) {

		if (or.getProperty(locator).startsWith("//") || or.getProperty(locator).startsWith("(")) {
			element = driver.findElement(By.xpath(or.getProperty(locator)));
		} else {
			element = driver.findElement(By.id(or.getProperty(locator)));
		}
		select = new Select(element);
		select.deselectAll();
	}

	// Frames
	public boolean switchToFrame(int i) {

		boolean result = false;
		try {
			driver.switchTo().frame(i);
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}

	public boolean switchToFrame(String id) {

		boolean result = false;
		try {
			driver.switchTo().frame(id);
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}

	public boolean switchToFrame(WebElement element) {

		boolean result = false;
		try {
			driver.switchTo().frame(element);
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}

	public boolean switchToParentFrame() {

		boolean result = false;
		try {
			driver.switchTo().parentFrame();
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}

	public boolean switchToDefaultFrame() {

		boolean result = false;
		try {
			driver.switchTo().defaultContent();
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}

	public String getParentWindowId() {

		String windowHandle = driver.getWindowHandle();
		return windowHandle;
	}

	public Set<String> allWindowsId() {

		Set<String> windowHandles = driver.getWindowHandles();
		return windowHandles;
	}

	/**
	 * This will help to fetch the data from the Excel using Index
	 */
	public String getData(String path, String sheetName, int i, int j) {

		String name = null;
		try {
			file = new File(path);
			fi = new FileInputStream(file);
			workbook = new XSSFWorkbook(fi);
			sheet = workbook.getSheet(sheetName);
			row = sheet.getRow(i);
			cell = row.getCell(j);
			int cellType = cell.getCellType();
			if (cellType == 1) {
				name = cell.getStringCellValue();
				System.out.println(name);
			} else if (DateUtil.isCellDateFormatted(cell)) {
				Date date = cell.getDateCellValue();
				SimpleDateFormat sid = new SimpleDateFormat("EEEE MMMM yyyy");
				name = sid.format(date);
				log.info(name);
			} else {
				double number = cell.getNumericCellValue();
				long num = (long) number;
				name = String.valueOf(num);
				log.info(name);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return name;
	}

	public void updateData(String path, String sheetName, int rowNum, int cellNum, String oldData, String newData) {

		try {
			file = new File(path);
			fi = new FileInputStream(file);
			workbook = new XSSFWorkbook(fi);
			sheet = workbook.getSheet(sheetName);
			row = sheet.getRow(rowNum);
			cell = row.getCell(cellNum);
			String value = cell.getStringCellValue();
			if (value.equalsIgnoreCase(oldData)) {
				cell.setCellValue(newData);
				// If not working , Initialize fo value with null
				fo = new FileOutputStream(file);
				workbook.write(fo);
				workbook.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void writeData(String path, String sheetname, int rownum, int cellnum, String data) {

		try {
			file = new File(path);
			fi = new FileInputStream(file);
			workbook = new XSSFWorkbook(fi);
			sheet = workbook.getSheet(sheetname);
			row = sheet.getRow(rownum);
			cell = row.createCell(cellnum);
			cell.setCellValue(data);
			fo = new FileOutputStream(file);
			workbook.write(fo);
			workbook.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<WebElement> getList(WebElement element, String Tagname) {

		List<WebElement> list = element.findElements(By.tagName(Tagname));
		return list;
	}

	// This will help to handle the auto suggestions
	public void getValueFromOptions(String xpath1, String value, String xpath2, String optionValue) {

		try {
			driver.findElement(By.xpath(xpath1)).sendKeys(value);
			waitForElement(3);
			List<WebElement> list = driver.findElements(By.xpath(xpath2));
			for (WebElement l : list) {
				if (l.getText().equalsIgnoreCase(optionValue)) {
					l.click();
					System.out.println(l.getText());
					break;
				}
			}
			log.info("Selected" + " " + optionValue + " " + "from the auto suggestions");
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Failed to select" + " " + optionValue + " " + "from the auto suggestions");
		}
	}

	// Verify
	public boolean verifyTitle(String expectedTitle) {

		boolean result = false;
		String actualTitle = driver.getTitle();
		try {
			if (actualTitle.equalsIgnoreCase(expectedTitle)) {
				result = true;
				log.info("The title " + actualTitle + " matches with the expected title");
			} else {
				log.error("The title " + actualTitle + " does not match the actual title " + expectedTitle);
				result = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	// IsDisplayed
	public boolean isDisplayed(WebElement element) {

		boolean result = false;
		try {
			if (element.isDisplayed()) {
				result = true;
			} else {
				result = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public boolean isDisplayed(String locator) {

		boolean result = false;
		if (or.getProperty(locator).startsWith("//") || or.getProperty(locator).startsWith("(")) {
			element = driver.findElement(By.xpath(or.getProperty(locator)));
		} else {
			element = driver.findElement(By.id(or.getProperty(locator)));
		}
		if (element.isDisplayed()) {
			result = true;
		} else {
			result = false;
			throw new RuntimeException();
		}
		return result;
	}

	public boolean isEnabled(String locator) {

		boolean result = false;
		if (or.getProperty(locator).startsWith("//") || or.getProperty(locator).startsWith("(")) {
			element = driver.findElement(By.xpath(or.getProperty(locator)));
		} else {
			element = driver.findElement(By.id(or.getProperty(locator)));
		}
		if (element.isEnabled()) {
			result = true;
		} else {
			result = false;
			throw new RuntimeException();
		}
		return result;
	}

	// IsEnabled
	public boolean isEnabled(WebElement element) {

		boolean result = false;
		try {
			if (element.isEnabled()) {
				result = true;
			} else {
				result = false;
				element.click();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	// SelectDropdown
	public boolean dropDownValue(String locator, String value) {

		boolean result = false;
		try {
			List<WebElement> list = driver.findElements(By.xpath(or.getProperty(locator)));
			for (WebElement l : list) {
				if (l.getText().equalsIgnoreCase(value)) {
					l.click();
					result = true;
					break;
				} else {
					result = false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Failed to select" + " " + value + " " + "from the dropDown");
		}
		return result;
	}

	// FileDownload
	public boolean isFileDownloaded(String path, String fileName) {

		boolean result = false;
		File dir = new File(path);
		File[] dirContents = dir.listFiles();
		if (dir.exists() && dir.isDirectory()) {
			for (int i = 0; i < dirContents.length; i++) {
				String name = dirContents[i].getName();

				if (name.contains(fileName)) {
					result = true;
					break;
				} else {

				}
			}
		} else {
			result = false;
		}
		return result;
	}

	// Date Picker
	public boolean datePicker(String fromDatePicker, String date) {

		boolean result = false;
		try {
			List<WebElement> t = driver.findElements(By.xpath(fromDatePicker));
			for (int k = 0; k < t.size(); k++) {
				// check date
				String dt = t.get(k).getText();
				if (dt.equals(date)) {
					t.get(k).click();
					result = true;
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * This will help to switch between the windows using its Index value
	 */
	public boolean switchToWindow(int num) {

		boolean result = false;
		try {
			Set<String> set = driver.getWindowHandles();
			List<String> list = new ArrayList<String>(set);
			driver.switchTo().window(list.get(num));
			log.info("WebDriver switched to the Window/Tab - " + num);
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
			log.error("Failed to switch to the desired Window/Tab");
		}
		return result;
	}

	/**
	 * This will help to switch between the windows by passing the Title of window
	 */
	public boolean switchToWindow(String title) {

		boolean result = false;
		try {
			Set<String> set = driver.getWindowHandles();
			for (String s : set) {
				if (driver.switchTo().window(s).getTitle().equalsIgnoreCase(title)) {
					result = true;
					break;
				} else
					result = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Failed to switch to the desired window");
		}
		return result;
	}

	/**
	 * This will help to copy a file
	 */
	public void copyfile(String path) {

		File source = new File(path);
		File dest = new File(".//Summaryreport/");

		try {
			FileUtils.copyFileToDirectory(source, dest);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void quit() {

		driver.quit();
	}

	public void close() {

		driver.close();
	}

	/**
	 * This will help to perform File upload operation if Input tag is available in
	 * DOM
	 */
	public void uploadFile(String locator, String filePath) {

		try {
			if (or.getProperty(locator).startsWith("//") || or.getProperty(locator).startsWith("(")) {
				element = driver.findElement(By.xpath(or.getProperty(locator)));
			} else {
				element = driver.findElement(By.id(or.getProperty(locator)));
			}
			element.sendKeys(filePath);
			log.info("'" + filePath + "' file uploaded Successfully");
		} catch (Exception e) {
			log.error("Failed to upload the '" + filePath + "'");
			e.printStackTrace();
		}
	}

	public void clickLocator(String locator) {

		driver.findElement(By.xpath(locator)).click();
	}

	/**
	 * This will help to perform File Upload operation using ToolKit
	 */
	public void uploadFile(String filePath) {

		try {
			StringSelection selection = new StringSelection(filePath);
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
			Robot robot = new Robot();
			robot.delay(500);
			robot.keyPress(KeyEvent.VK_ENTER);
			robot.keyRelease(KeyEvent.VK_ENTER);
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_V);
			robot.keyRelease(KeyEvent.VK_V);
			robot.keyRelease(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_ENTER);
			robot.delay(100);
			robot.keyRelease(KeyEvent.VK_ENTER);
			log.info("File uploaded Successfully");
		} catch (Exception e) {
			log.error("Failed to upload the file");
			e.printStackTrace();
		}
	}

	public void verifyText(String locator, String text) {

		try {
			if (or.getProperty(locator).startsWith("//") || or.getProperty(locator).startsWith("(")) {
				elementText = driver.findElement(By.xpath(or.getProperty(locator))).getText();
			} else {
				elementText = driver.findElement(By.id(or.getProperty(locator))).getText();
			}
			Assert.assertEquals(elementText, text);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * This will help to switch between the browsers using Keyboard Simulation
	 */
	public void switchToBrowser() {

		try {
			Robot robot = new Robot();
			robot.keyPress(KeyEvent.VK_ALT);
			robot.keyPress(KeyEvent.VK_TAB);
			robot.delay(100);
			robot.keyRelease(KeyEvent.VK_TAB);
			robot.keyRelease(KeyEvent.VK_ALT);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void fluentWaitForLocator(String locator, int waitTime) {

		FluentWait<WebDriver> wait = new FluentWait<>(driver).withTimeout(Duration.ofSeconds(waitTime))
				.pollingEvery(Duration.ofSeconds(2)).ignoring(NoSuchElementException.class);
		element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)));
	}

	public void waitForPageLoad() {
		wait = new WebDriverWait(driver, Duration.ofSeconds(Integer.parseInt(config.getProperty("webDriverWait"))));
		wait.until((ExpectedCondition<Boolean>) wd -> ((JavascriptExecutor) wd)
				.executeScript("return document.readyState").equals("complete"));
	}

	public String captureScreenshotAsBase64() {

		String base64Image = "";
		try {
			// Take screenshot
			TakesScreenshot ts = (TakesScreenshot) BaseTest.driver;
			File screenshotBytes = ts.getScreenshotAs(OutputType.FILE);

			// Convert screenshot to Base64
			byte[] fileContent = FileUtils.readFileToByteArray(screenshotBytes);
			base64Image = Base64.getEncoder().encodeToString(fileContent);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return base64Image;
	}

	public void mailSent() {

		try {
			MonitoringMail mail = new MonitoringMail();

			String messageBody = null;

			messageBody = "http://" + InetAddress.getLocalHost().getHostAddress() + ":8080/job/TestNGProject/reports/";
			mail.sendMail(TestConfig.server, TestConfig.from, TestConfig.to, TestConfig.subject, messageBody,
					TestConfig.attachmentPath, TestConfig.attachmentName);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
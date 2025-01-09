package testExecute;

import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Runner extends BaseTest {

	@Test
	public void testGoogle() throws Exception {

		url("https://www.google.com/");
		driver.get("https://www.google.com/");
		driver.findElement(By.name("q")).sendKeys("Selenium"+Keys.RETURN);
		Thread.sleep(3000);
	}

	@Test
	public void testGmail() throws Exception{

		url("https://www.gmail.com/");
		System.out.println(driver.getTitle());
		Thread.sleep(3000);
		
	}
	
	@Test(enabled = false)
	public void test() throws Exception {
		
		driver.get("https://www.google.com/");
		driver.findElement(By.name("q")).sendKeys("Selenium"+Keys.RETURN);
		WebElement link = driver.findElement(By.xpath("//a[@href='https://www.selenium.dev/']"));
		
		Thread.sleep(3000);
		Actions a = new Actions(driver);
		a.keyDown(Keys.CONTROL).click(link).keyUp(Keys.CONTROL).perform();
		Set<String> tabs = driver.getWindowHandles();
		System.out.println(tabs.size());
		Thread.sleep(3000);
		
	}
	
	

//	@Test
//	public void test3() throws Exception {
//		
//		driver.get("http://localhost:58843/");
//		Thread.sleep(10000);
//		
//		driver.findElement(By.id("flt-semantic-node-8")).click();
//		String string = driver.findElement(By.id("flt-semantic-node-5")).toString();
//		if (string.equals("1")) {
//			System.out.println("Pass");
//			
//		}
//		
		
//		driver.findElement(By.xpath("//*[@name='username']")).sendKeys("Admin");
//		driver.findElement(By.xpath("//*[@name='password']")).sendKeys("admin123");
//		driver.findElement(By.xpath("//*[text()='Leave']")).click();
//		
//		driver.findElement(By.xpath("(//*[@placeholder='yyyy-dd-mm'])[1]")).click();
//		
//		Thread.sleep(5000);
		
	}
	
	


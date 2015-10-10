package org.apache.nutch.protocol.interactiveselenium;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DonglinHandler implements InteractiveSeleniumHandler {

	@Override
	public void processDriver(WebDriver driver) {
		
		
		String url = truncateUrl(driver.getCurrentUrl());
		System.out.println("==> truncated url: " + url);
		driver.manage().timeouts().pageLoadTimeout(5000, TimeUnit.MILLISECONDS);

		
		switch (url) {
		case "arguntrader.com":
			fetchArguntrader(driver);
			break;
		case "cheaperthandirt.com":
			fetchCheaperthandirt(driver);
			break;
		case "hawaiiguntrader.com":
			fetchHawaiiguntrader(driver);
			break;
		case "kyclassifieds.com":
			fetchKyclassifieds(driver);
			break;
		case "ksl.com":
			fetchKslCom(driver);
			break;
		case "msguntrader.com":
			fetchMsguntrader(driver);
			break;
		case "donglinpu.me":
			fetchDonglinPuMe(driver);
			break;
		
		// Add case here.
		default:
			break;
		}
		
		driver.close();
	}

	/**
	 * @author Donglin Pu
	 * @param URL: input URL. We need to decide if we need to run interaction on this url. 
	 * 			If so, return true. Then processDriver function will run.
	 */
	@Override
	public boolean shouldProcessURL(String URL) {
		URL = truncateUrl(URL);
		switch (URL) {
		case "arguntrader.com":
		case "cheaperthandirt.com":
		case "hawaiiguntrader.com":
		case "kyclassifieds.com":
		case "ksl.com":
		case "msguntrader.com":
		
		case "freegunclassifieds.com":
		case "donglinpu.me":
		// Add case here.
			return true;
		default:
			return false;
		}
	}
	
	/**
	 * @author Donglin Pu
	 * @param url: the URL to be fetched.
	 * @return
	 */
	private String truncateUrl (String url) {
		if (url.startsWith("http://")) {
			url = url.substring(7);
			url = url.split("/")[0];
		} else if (url.startsWith("https://")) {
			url = url.substring(8);
			url = url.split("/")[0];
		}
		if (url.startsWith("www.")) {
			url = url.substring(4);
			url = url.split("/")[0];
		}
		return url;
	}
	
	private void fetchDonglinPuMe (WebDriver driver) {
		List<WebElement> elements = driver.findElements(By.id("clickThis"));
		if (elements.size() != 0) {
			elements.get(0).click();
		}
	}
	
	/**
	 * @author Donglin Pu
	 * Fetch http://www.kyclassifieds.com/
	 * Username: Oct042015
	 * Password: PalashGoyal
	 */
	private void fetchKyclassifieds (WebDriver driver) {
		List<WebElement> elements = driver.findElements(By.id("login-form"));
		if (elements.size() != 0) {
			WebElement loginForm = elements.get(0);
			if (loginForm != null) {
				WebElement loginUsername = driver.findElement(By.id("login_username"));
				WebElement loginPassword = driver.findElement(By.id("login_password"));
				WebElement loginButton = driver.findElement(By.id("login"));
				loginUsername.sendKeys("Oct042015");
				loginPassword.sendKeys("PalashGoyal");
				loginButton.click();
			}
		}
	}
	
	/**
	 * @author Donglin
	 * Fetch www.ksl.com
	 * Username: csci572.20151004@gmail.com
	 * Password: PalashGoyal
	 */
	private void fetchKslCom (WebDriver driver) {
		List<WebElement> elements = driver.findElements(By.xpath("//*[@action='/public/member/signin']"));
		if (elements.size() == 0) {
			elements = driver.findElements(By.xpath("//*[@action='/public/member/login']"));
		}
		if (elements.size() != 0) {
			WebElement loginForm = elements.get(0);
			if (loginForm != null) {
				WebElement loginUsername = driver.findElement(By.id("memberemail"));
				WebElement loginPassword = driver.findElement(By.id("memberpassword"));
				WebElement loginButton = driver.findElement(By.xpath("//*[@value='Log In']"));
				loginUsername.sendKeys("csci572.20151004@gmail.com");
				loginPassword.sendKeys("PalashGoyal");
				loginButton.click();
			}
		}
	}
	
	/**
	 * @author Donglin
	 * Fetch www.hawaiiguntrader.com
	 */
	private void fetchHawaiiguntrader (WebDriver driver) {
		List<WebElement> elements = driver.findElements(By.id("login-form"));
		if (elements.size() != 0) {
			WebElement loginForm = elements.get(0);
			if (loginForm != null) {
				driver.findElement(By.id("login_username")).sendKeys("csci572.20151004@gmail.com");
				driver.findElement(By.id("login_password")).sendKeys("PalashGoyal");
				driver.findElement(By.id("login")).click();
			}
		}
	}
	
	
	/**
	 * @author Donglin
	 * Fetch www.arguntrader.com
	 * Example http://www.arguntrader.com/viewforum.php?f=36&sid=fb1773c91934a3e1a9411e73d6276401
	 * Oct042015:PalashGoyal
	 * Find by <form action="./ucp.php?mode=login"...
	 */
	private void fetchArguntrader (WebDriver driver) {
		List<WebElement> elements = driver.findElements(By.xpath("//input[@type='submit' and @name='login']")); // find the <input> login button
		if (elements.size() != 0) {
			WebElement loginUsername = driver.findElement(By.id("username"));
			WebElement loginPassword = driver.findElement(By.id("password"));
			WebElement loginButton = driver.findElement(By.xpath("//*[@value='Login']"));
			loginUsername.sendKeys("Oct042015");
			loginPassword.sendKeys("PalashGoyal");
			loginButton.click();

//			WebDriverWait wdw = new WebDriverWait(driver, 10);
//			WebElement myDynamicElement = wdw.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), 'Return to the previous page')]")));
//			System.out.println("=== Waited === ");
		}
	}
	
	/**
	 * @author Donglin Pu
	 * Fetch www.cheaperthandirt.com
	 * Login: Oct042015 : KTrZ!MM^C#I1
	 * Wait for popup (sumome-popup-form): If see popup: click close popup: $(".sumome-popup-close").click()
	 */
	private void fetchCheaperthandirt (WebDriver driver){
		WebDriverWait wdw = new WebDriverWait(driver, 10);
		WebElement popup = wdw.until(ExpectedConditions.presenceOfElementLocated(By.className("sumome-popup-form")));
		if (popup != null) {
			System.out.println("== here is popup! ==");
			try {
				WebElement popupCloseDivByXPATH = driver.findElement(By.xpath("/html/body/div[3]/div[2]/div[3]"));
				popupCloseDivByXPATH.click();
			} catch (NoSuchElementException ex) {
				System.out.println("=== INFO: NoSuchElementException caught: popup element not found");
				driver.navigate().refresh();
			} catch (ElementNotVisibleException ex) { // this one worked!
				System.out.println("=== INFO: ElementNotVisibleException caught: popup element not found");
				driver.navigate().refresh();
			} catch (Exception e) {
				System.out.println("=== INFO: Exception caught: popup element not found");
			}
		}
	}
	
	
	
	/**
	 * @author Donglin Pu
	 * Fetch: http://msguntrader.com/
	 * Credential: WtfIsWangGuard : WtfIsWangGuard
	 */
	private void fetchMsguntrader (WebDriver driver) {
		List<WebElement> loginFormList = driver.findElements(By.id("login-form"));
		if (loginFormList.size() > 0) { // form exist
			try {
				
			WebElement loginInput = driver.findElement(By.id("login_username"));
			WebElement passwordInput = driver.findElement(By.id("login_password"));
			WebElement loginButton = driver.findElement(By.id("login"));

			loginInput.sendKeys("WtfIsWangGuard");
			passwordInput.sendKeys("WtfIsWangGuard");
			loginButton.click();
			
			} catch (Exception e) {
				// do nothing
			}
		}
	}
	
	
}

package org.apache.nutch.protocol.interactiveselenium;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class DonglinHandler implements InteractiveSeleniumHandler {

	@Override
	public void processDriver(WebDriver driver) {
		
		
		String url = truncateUrl(driver.getCurrentUrl());
		System.out.println("==> truncated url: " + url);
		driver.manage().timeouts().pageLoadTimeout(5000, TimeUnit.MILLISECONDS);

		
		switch (url) {
		case "www.donglinpu.me":
			fetchDonglinPuMe(driver);
			break;
		case "www.kyclassifieds.com":
			fetchKyclassifieds(driver);
			break;
		case "www.ksl.com":
			fetchKslCom(driver);
			break;
		case "www.hawaiiguntrader.com":
			fetchHawaiiguntrader(driver);
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
		case "www.donglinpu.me":
		case "www.kyclassifieds.com":
		case "www.ksl.com":
		case "www.hawaiiguntrader.com":
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
			return url;
		} else if (url.startsWith("https://")) {
			url = url.substring(8);
			url = url.split("/")[0];
			return url;
		} else {
			return url;
		}
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
}

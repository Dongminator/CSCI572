package org.apache.nutch.protocol.interactiveselenium;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class DonglinHandler implements InteractiveSeleniumHandler {

	@Override
	public void processDriver(WebDriver driver) {
		System.out.println();
		
		String url = driver.getCurrentUrl();
		switch (url) {
		case "http://www.donglinpu.me/csci572":
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
		System.out.println("==> Processing URL: " + URL);
		// Determine if we need to interact with the content
		
		// if so, return true
		
		// if not, return false
		
		return true;
	}
	
	private void fetchDonglinPuMe (WebDriver driver) {
		driver.findElement(By.id("clickThis")).click();
	}
}

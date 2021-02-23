package com.masuta.seleniumcrawler;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import com.google.gson.Gson;

public class CrawlerMain {
	
	static String targetURL = "http://www.kyobobook.co.kr/bestSellerNew/bestseller.laf";
	
	
	public static void main(String[] args) throws InterruptedException {
		  // 현재 package의 workspace 경로, Windows는 [ chromedriver.exe ]
        Path path = Paths.get("C:/Chromedriver/chromedriver.exe");  // 현재 package의
        
        // WebDriver 경로 설정
        System.setProperty("webdriver.chrome.driver", path.toString());
        
        // WebDriver 옵션 설정
        ChromeOptions options = new ChromeOptions();
        
        options.addArguments("--start-maximized");            			// 전체화면으로 실행
        options.addArguments("--disable-popup-blocking");    			// 팝업 무시
        options.addArguments("--disable-default-apps");     			// 기본앱 사용안함
        options.addArguments("--blink-settings=imagesEnabled=false");	// 이미지 무시 
        
        Long startTime =  System.currentTimeMillis();
        
        // WebDriver 객체 생성
        ChromeDriver driver = new ChromeDriver( options );                
        
        Map<String, String> searchOptions = new HashMap<String, String>();
                
        searchOptions.put("range", "1"); 
        searchOptions.put("kind", "0");
        
        
        driver.get(targetURL);
        
        JavascriptExecutor js = (JavascriptExecutor) driver;
        
        List<String> categoryLinkList = new ArrayList<String>();
        // 웹페이지에서 카테고리 영역 가져오기
        WebElement categoryList = driver.findElement(By.className("list_sub_category"));
        List<WebElement> categoryItems = categoryList.findElements(By.tagName("li"));        
                
        for (WebElement category : categoryItems) {
        	WebElement categoryLink = category.findElement(By.tagName("a"));
   	    	categoryLinkList.add(categoryLink.getAttribute("href").replace("javascript:", ""));        	
        	//categoryLinkList.add(getCategoryLinkSource(categoryLink.getAttribute("href")));        	
		}
        
//        for (int index=0; index < categoryLinkList.size(); index++) {
//        	if(index > 0)
//        		js.executeScript(categoryLinkList.get(index));
        	//js.executeScript(categoryLinkList.get(index));
        	
        	List<HashMap<String, Object>> itemList = new ArrayList<HashMap<String, Object>>();
        	getPageDataSource(driver, js, itemList);
        	//System.out.println(productTitle.getText());
        	//js.executeScript("_go_targetPage('10')");
        	
        	System.out.println(new Gson().toJson(itemList));
        	
//        }
        
        
       // 2. 카테고리별 페이지 리스트 가져오기
        
        // 탭 종료
        driver.close();
        Long endTime =  System.currentTimeMillis();
        
        System.out.println((endTime - startTime) / 1000 + "초 소요");                      
        
        
        // 5초 후에 WebDriver 종료
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // WebDriver 종료
            driver.quit();
        }
	}
	
	public List<HashMap<String, Object>> getPageInfo(String URL, ChromeOptions options, ChromeDriver driver){
		// WebDriver 옵션 설정
        
		driver.get("http://www.kyobobook.co.kr/bestSellerNew/bestseller.laf");
        
        // 웹페이지에서 글제목 가져오기
        WebElement categoryList = driver.findElement(By.className("list_sub_category"));
        List<WebElement> categoryItems = categoryList.findElements(By.tagName("li"));               
        
        for (WebElement category : categoryItems) {
        	WebElement categoryLink = category.findElement(By.tagName("a"));
			System.out.println(categoryLink.getAttribute("href"));
		}            
        
        // 탭 종료
        driver.close();
		return null;
	}
	private static void getPageDataSource(ChromeDriver driver, JavascriptExecutor js, List<HashMap<String, Object>> itemList) {
		
		WebElement productList = driver.findElement(By.className("list_type01"));
    	List<WebElement> productItems = productList.findElements(By.tagName("li"));
    	
		for(WebElement productItem : productItems) {
    		try {
    			HashMap<String, Object> item = new HashMap<String, Object>();
    			String authorString = "";
    			String additionalAuthorString = "";
    			WebElement productDetail = productItem.findElement(By.className("detail"));
        		WebElement productTitle = productDetail.findElement(By.className("title"));
        		WebElement productAuthor = productDetail.findElement(By.className("author"));
        		WebElement productPrice = productDetail.findElement(By.className("price")).findElement(By.className("book_price"));
        		
        		authorString = productAuthor.getText();
        		
        		if(authorString.contains("저자 더보기")) {
        			WebElement productAdditionalAuthor1 = productAuthor.findElement(By.className("uxOpenList"));
        			WebElement productAdditionalAuthor2 = productAdditionalAuthor1.findElement(By.className("list_author"));
        			List<WebElement> productAdditionalAuthor3 = productAdditionalAuthor2.findElements(By.tagName("li"));
        			for (WebElement additionalAuthor : productAdditionalAuthor3) {
        				if(additionalAuthorString.isEmpty()) {
        					additionalAuthorString += additionalAuthor.getAttribute("textContent");
        				} else {
        					additionalAuthorString += " " + additionalAuthor.getAttribute("textContent");
        				}
					}
        			authorString = authorString.replace("저자 더보기", additionalAuthorString).replace("\n", "");            			
        		}
        		
        		item.put("title", productTitle.getText());
        		item.put("author", productTitle.getText());
        		item.put("price", productPrice.getText());
        		
        		itemList.add(item);
        		
			} catch (Exception e) {
//				e.printStackTrace();
			}   		    		
    		   		
    	}
		

		WebElement pageList = driver.findElement(By.className("list_paging"));
		
		try {
			WebElement nextButton = pageList.findElement(By.className("btn_next"));
			//nextButton.getAttribute("href").replace("javascript:", "");
			js.executeScript(nextButton.getAttribute("href").replace("javascript:", ""));
			getPageDataSource(driver, js, itemList);
		} catch (Exception e) {
			
		}
	}
	
	// 카테고리 링크 추출
	private static String getCategoryLinkSource(String href) {	
		href = href.replace("javascript:goCateList(", "").replace(")", "").replace("'", "").replace(" ", "");		
				
		String[] linkSource = href.split(",");		
		
		String url = targetURL + "?range=1&kind=0" + "&mallGb=" + linkSource[0] + "&linkClass=" + linkSource[1] + "&orderClick=" + linkSource[2] ;		
				
		return url;
		
	}
	
}

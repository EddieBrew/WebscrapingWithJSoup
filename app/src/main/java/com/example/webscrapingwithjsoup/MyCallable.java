package com.example.webscrapingwithjsoup;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.concurrent.Callable;

public class MyCallable<String> implements Callable<String> {
	String website;
	public MyCallable(String website){
		this.website = website;

	}
	@Override
	public String call() throws Exception {
        StringBuilder sNodes = new StringBuilder();
		Document doc =  Jsoup.connect(java.lang.String.valueOf(website)).get();
		try {
			Elements table = doc.getElementsByTag("tbody"); //get the body of the table
			Elements rows = table.select("tr");//get the row tatag for the table

			//Log.i("OUT1", rows.text());
			for (Element row : rows) {
				Elements cells = row.children();//get all elements in the result cell
				//Log.i("NUMLIST", cells.text());
				for(Element cell: cells) {
					Elements numListings = cell.getElementsByAttributeValue("class", "draw-result list-unstyled list-inline" );
					//Log.i("XXXXX", numListings.text());
					for(Element numListing : numListings ){
						Elements numbers = numListing.getElementsByTag("li");
						for(Element number : numbers ){
							String lotteryNumber = (String) number.text();
							sNodes.append(lotteryNumber + " ");

						}
						sNodes.append("\n");

					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		Log.i("OUTPUT", sNodes.toString());
		String responseString = (String) sNodes.toString();

		return responseString;
	}
}

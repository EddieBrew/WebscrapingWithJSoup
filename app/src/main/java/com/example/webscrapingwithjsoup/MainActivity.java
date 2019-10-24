package com.example.webscrapingwithjsoup;



/*
Created by Robert Brewer
Date: 10/23/2019


WebsrapingwithJsoup illustrates abstracting data from websites without APIs. The apps
conssists of three methods:

1)webscrapingFromLotterSite- extracts daily lottery numbers from the website and display the
  the past 52 weeks drawings in the Logacat console
2)webscrapingFromLotterySiteUsingCallable() extracts daily lottery numbers from the website, using a
  custom Callable class. The return data from the Callable is then used to populate the UI scrollable
  textView
3)webscrapingFromLotterySiteUsingVolley() uses Volley to send a request and recive a string response
  from the lottery website. Since Volley introduces its own thread, the response can be sent to another
  method, that parses the data using Jsoup and populates the UI scrollable textView


 */
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.jsoup.Jsoup.parse;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = MainActivity.class.getName();
	private static final String REQUESTTAG = "string request first";
	private RequestQueue mRequestQueue; //requestqueue varaiable from Vollei library
	private StringRequest stringRequest;   //the variable type of the requestqueue
	Button button;
	TextView jsonTextView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final String website = "https://www.lotteryusa.com/california/super-lotto-plus/year";
		button = findViewById(R.id.button);
		jsonTextView = findViewById(R.id.jsonTextView);

		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				webscrapingFromLotterySite(website);
				//webscrapingFromLotterySiteUsingCallable(website);
				webscrapingFromLotterySiteUsingVolley(website);
			}
		});


	}



	/*********************************************************************************
	 *  webscrapingFromLotterSite() uses Jsoup to extract loeetry data from its website
	 *  The data is displayed in the Logcat window
	 *
	 *
	 * @pre none
	 * @parameter none
	 * @post Displays number in Logcat window
	 **********************************************************************************/
	private void webscrapingFromLotterySite(final String website){

		//For API 11 and above, Jsoup can not run on the apps main thread. Therefroe, a thread
		// to must be incorporated to execute Jsoup methods

		new Thread(new Runnable(){

			@Override
			public void run() {
				try {
					String sNodes = "";
					Document doc = Jsoup.connect(website).get();
					Elements table = doc.getElementsByTag("tbody"); //get the body of the table
					Elements rows = table.select("tr");//get the row tatag for the table

					//Log.i("OUT1", rows.text());
					for (org.jsoup.nodes.Element row : rows) {
						Elements cells = row.children();//get all elements in the result cell
						Log.i("NUMLIST", cells.text());
						for(org.jsoup.nodes.Element cell: cells) {
							Elements numListings = cell.getElementsByAttributeValue("class", "draw-result list-unstyled list-inline" );
							Log.i("XXXXX", numListings.text());
							for(org.jsoup.nodes.Element numListing : numListings ){
								Elements numbers = numListing.getElementsByTag("li");

								for(Element number : numbers ){
									String lotteryNumber = number.text();
									sNodes += lotteryNumber + " ";
								}
								sNodes += "\n";

							}
						}
					}
					Log.i("OUTPUT", sNodes);

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}




	/*********************************************************************************
	 *  webscrapingFromLotterySiteUsingCallable() uses a custom Callable class, that incorporates Jsoup for
	 *  webscraping lottery data . This method is used to be able to populate/update the UI
	 *  scrollable textView
	 *
	 *
	 * @pre none
	 * @parameter none
	 * @post Populates the UI lottery number displays
	 **********************************************************************************/
	private void webscrapingFromLotterySiteUsingCallable(String website){

		//Steps to implement a Callable,which allows data to be returned, via the future, from the thread

		//1) Declare an ExecutorService
		ExecutorService executor = Executors.newCachedThreadPool();

		//2)Declare a Future object, with its return type
		Future<String> future ;

		//3) Instantiate a Callable object
		Callable<String> callable = new MyCallable<>(website);

		//4)assign future object to the Callable return variable
		future = executor.submit(callable);

		try {

			jsonTextView.setText(future.get());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	/*********************************************************************************
	 *  webscrapingFromLotterySiteUsingVolley() uses the Volley library to do a network request and receive
	 *  the data from the lottery website.
	 *
	 *
	 * @pre none
	 * @parameter String website
	 * @post passes the network response to another method for data parsing
	 **********************************************************************************/
	private void webscrapingFromLotterySiteUsingVolley(String website){

		mRequestQueue = Volley.newRequestQueue(this);
		stringRequest = new StringRequest(Request.Method.GET, website, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				ParseHTMLPage(response);
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {

			}
		});

		stringRequest.setTag(REQUESTTAG);
		mRequestQueue.add(stringRequest);

	}

	/*********************************************************************************
	 *  ParseHTMLPage takes the string response from a Volley network call and parses the Table,
	 *  containing the past 52 weeks lottery data. The data is is then used to update and display
	 *  in the UI's scrollable textView
	 *
	 *
	 * @pre none
	 * @parameter String response
	 * @post passes the network response to another method for data parsing
	 **********************************************************************************/

	private void ParseHTMLPage(final String response){


		//final String website = "https://www.lotteryusa.com/california/super-lotto-plus/year";
		if( response == "")return;


		String sNodes = "";
		List<Integer> list = new ArrayList<>();
		//jsonTextView.setText(response);

		Document doc = null;

		doc = parse(response);
		//Log.i("list size", doc.text());

		try {
			Elements table = doc.getElementsByTag("tbody"); //get the body of the table
			Elements rows = table.select("tr");//get the row tatag for the table

			//Log.i("OUT1", rows.text());
			for (Element row : rows) {
				Elements cells = row.children();//get all elements in the result cell
				Log.i("NUMLIST", cells.text());
				for(Element cell: cells) {
					Elements numListings = cell.getElementsByAttributeValue("class", "draw-result list-unstyled list-inline" );
					Log.i("XXXXX", numListings.text());
					for(Element numListing : numListings ){
						Elements numbers = numListing.getElementsByTag("li");

						for(Element number : numbers ){
							String lotteryNumber = number.text();
							sNodes += lotteryNumber + " ";
						}
						sNodes += "\n";

					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}


		jsonTextView.setText(sNodes);
	}


}

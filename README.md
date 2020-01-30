# WebscrapingWithJSoup


WebsrapingwithJsoup illustrates abstracting data from websites without APIs. The apps
conssists of three methods:

1)webscrapingFromLotterySite- extracts daily lottery numbers from the website and display the
  the past 52 weeks drawings in the Logcat console
2)webscrapingFromLotterySiteUsingCallable() extracts daily lottery numbers from the website, using a
  custom Callable class. The return data from the Callable is then used to populate the UI scrollable
  textView
3)webscrapingFromLotterySiteUsingVolley() uses Volley to send a request and receive a string response
  from the lottery website. Since Volley introduces its own thread, the response can be sent to another
  method, that parses the data using Jsoup and populates the UI scrollable textView


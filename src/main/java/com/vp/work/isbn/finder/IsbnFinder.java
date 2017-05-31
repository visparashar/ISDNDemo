package com.vp.work.isbn.finder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.web.client.RestTemplate;

import com.vp.work.gutenberg.GutenbergProcessFlow;
import com.vp.work.isbn.config.Config;



public class IsbnFinder {
	
	private static final String STATUS_KEY="Status";
	private static final String DATA="Data";
	private static final String _OPENLIBRARY="openlibrary";
	private static final String _GOOGLEBOOKS="googlebooks";
	private static final String _GUTENBERG ="gutenberg";
	
	private static final Logger LOGGER = Logger.getLogger(IsbnFinder.class);
	
	public boolean isPresentOnPublicLink(String isbn){
		try{
			if(null==isbn){
				LOGGER.info("ISBN is null , hence returning false");
				return false;
			}
			isbn=isbn.replaceAll("-", "");
			LOGGER.info("removed all the - character in isbn number");
			String uri= IsbnFinder.getServiceUrl(isbn,_OPENLIBRARY);
			LOGGER.info("checking for the Open Library");
			String result =callRest(uri);
			if(result!=null && result.length()>0 && !result.equalsIgnoreCase("var _OLBookInfo = {};")){
				isPresentOnOpenLibrary(result ,isbn);
				if(!isPresentOnOpenLibrary(result ,isbn))
				{
					LOGGER.info("The response status of Open library is false , hence checking for google book");
					String googleBookResult=  callRest(IsbnFinder.getServiceUrl(isbn,_GOOGLEBOOKS));
					if(!isPresentOnGoogleBook(googleBookResult,isbn))
					{
						LOGGER.info("the response status of Google book is also false , hence calling guterberg flow");
						String isbnResponse = callRest(IsbnFinder.getServiceUrl(isbn,_GUTENBERG));
						String bookTitle = getBookTitle(isbnResponse);
						if(null!=bookTitle)
						{
							//					    			guterberg process start from here
							return GutenbergProcessFlow.startGutenbergFlow(bookTitle);
						}else{
							return false;
						}
					}
				}else{
					return false;
				}
			}else {
				LOGGER.info("The result of Open library is empty or null , hence checking for google book");
				String googleBookResult=  callRest(IsbnFinder.getServiceUrl(isbn,_GOOGLEBOOKS));
				if(!isPresentOnGoogleBook(googleBookResult,isbn)){
					LOGGER.info("the response status of Google book is also false , hence calling guterberg flow");
					String isbnResponse = callRest(IsbnFinder.getServiceUrl(isbn,_GUTENBERG));
					String bookTitle = getBookTitle(isbnResponse);
					if(null!=bookTitle)
					{
						//			    			guterberg process start from here
						return GutenbergProcessFlow.startGutenbergFlow(bookTitle);
					}else{
						return false;
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return false;
	}
	private static String callRest(String url){
		try{
			RestTemplate restTemplate = new RestTemplate();
			String result = restTemplate.getForObject(url, String.class);
			return result;
		}catch(Exception e)
		{
			return null;
		}
	}

	private static String getServiceUrl(String isbn,String diffCase)
	{
		
		switch(diffCase)
		{
		case _OPENLIBRARY:
			 return Config.OPENlIB_REST_URL+isbn;
		case _GOOGLEBOOKS:
			return Config._GOOGLEBOOK_REST_URL+isbn;
		case _GUTENBERG:
		{
			LOGGER.info(Config._ISBNDB_URL+"/"+Config.AUTHORIZATION_KEY+"/"+Config.SERVICE_TYPE_KEY+"/"+isbn);
			return Config._ISBNDB_URL+"/"+Config.AUTHORIZATION_KEY+"/"+Config.SERVICE_TYPE_KEY+"/"+isbn;
		}
			
		}
		return null;
	}
	private static String getBookTitle(String json) throws ParseException{
		JSONParser parser = new JSONParser();
		Map object  = (HashMap)parser.parse(json);
		if(object.containsKey("error"))
		{
			return null;
			
		}else{
			if(object.containsKey("data"))
			{
				if(object.get("index_searched").toString().equalsIgnoreCase("isbn")){
//					return true;
					List<Map> dataSet =(List<Map>)object.get("data");
						Map<String,String>eachData=dataSet.get(0);
						if(eachData.containsKey("title"))
							return eachData.get("title");					
				}
			}
		}
		return null;
	}

	
	private static boolean isPresentOnOpenLibrary(String json,String isbn) throws ParseException{
		
		JSONParser parser = new JSONParser();	
		String temp ="var _OLBookInfo =";
		json=json.replaceAll(temp, "");
		json=json.replaceAll(";","");
		
		Map object  = (HashMap)parser.parse(json);
		
		if(object.isEmpty()){
			return false;
		}else{
			HashMap dataSet =(HashMap)object.get("ISBN:"+isbn);
			if(dataSet.get("preview").toString().equalsIgnoreCase("borrow")){
				return true;
				
			}else{
				return false;
			}
		}
	}
	
	private static boolean isPresentOnGoogleBook(String json,String isbn) throws ParseException{
		JSONParser parser = new JSONParser();	
		Map response =(HashMap)parser.parse(json);
		if(response.get("totalItems").toString().equals("0")){
			return false;		
		}else{
			List items = (ArrayList)response.get("items");
			Map item =(HashMap)items.get(0);
			if(item!=null && item.containsKey("accessInfo")){
				Map accessMap = (HashMap) item.get("accessInfo");
				if(Boolean.getBoolean(accessMap.get("publicDomain").toString()))
				{
					Map pdf =(HashMap)accessMap.get("pdf");
					if(Boolean.getBoolean(pdf.get("isAvailable").toString())){
						return true;
					}
				}else{
					return false;
				}
			}
		}
		return false;
	}
}
		
		
		
		
		
	


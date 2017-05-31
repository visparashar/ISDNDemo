package com.vp.work.isbn.finder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.web.client.RestTemplate;

import com.vp.work.isbn.config.Config;



public class IsbnFinder {
	
	private static final String STATUS_KEY="Status";
	private static final String DATA="Data";
	private static final String _OPENLIBRARY="openlibrary";
	private static final String _GOOGLEBOOKS="googlebooks";
	
	public HashMap<String,String> isPresentOnPublicLink(String isbn){
		
		try{
			if(null==isbn){
				return null;
			}
			isbn=isbn.replaceAll("-", "");
			System.out.println(isbn);
				System.out.println("Calling isbn rest service");					
			     String uri= IsbnFinder.getServiceUrl(isbn,_OPENLIBRARY);
			     String result =callRest(uri);
			     if(result!=null && result.length()>0 && !result.equalsIgnoreCase("var _OLBookInfo = {};")){
			    	 HashMap response = getPublisherNameFor_(result ,isbn);
			    	 
			    	 if(response.get(STATUS_KEY).toString().equals("false"))
			    	 {
			    		 String googleBookResult=  callRest(IsbnFinder.getServiceUrl(isbn,_GOOGLEBOOKS));
					    	return getPublicherFor_GoogleBook(googleBookResult,isbn);
			    	 }else{
			    		 return response;
			    	 }
			    	 
			     }else{
			    	String googleBookResult=  callRest(IsbnFinder.getServiceUrl(isbn,_GOOGLEBOOKS));
			    	return getPublicherFor_GoogleBook(googleBookResult,isbn);
			     }
			    
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
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
			 return Config.ISBNDB_REST_URL+isbn;
		case _GOOGLEBOOKS:
			return Config._GOOGLEBOOK_REST_URL+isbn;
			
		}
		return null;
	
	}
	
	private static HashMap<String,String> getPublisherName(String json) throws ParseException{
		
		JSONParser parser = new JSONParser();
		Map object  = (HashMap)parser.parse(json);
		
		if(object.containsKey("error"))
		{
			Map map = new HashMap<String,String>();
			map.put(STATUS_KEY, false);
			map.put(DATA, (String)object.get("error"));
			return (HashMap<String, String>) map;
		}else{
			if(object.containsKey("data"))
			{
				if(object.get("index_searched").toString().equalsIgnoreCase("isbn")){
//					return true;
					List<Map> dataSet =(List<Map>)object.get("data");
					for(Map<String,String> eachData: dataSet){
						
						Map map = new HashMap<String,String>();
						map.put(STATUS_KEY, true);
						map.put(DATA, "PUBLISHER_NAME:"+eachData.get("publisher_name")+ " AND BOOK TITLE:"+eachData.get("title_long")+" hence is present on public link");
						return (HashMap<String, String>) map;
					}
				}else{
					Map map = new HashMap<String,String>();
					map.put(STATUS_KEY, false);
					map.put(DATA, "searched query have some result on "+object.get("index_searched")+ " condition , but we are searching for isbn , hence "
							+ "IGNORING ");
					return (HashMap<String, String>) map;
				}
			}
				
		}
		return null;
			
	}

	
	private static HashMap<String,String> getPublisherNameFor_(String json,String isbn) throws ParseException{
		
		JSONParser parser = new JSONParser();	
		String temp ="var _OLBookInfo =";
		json=json.replaceAll(temp, "");
		json=json.replaceAll(";","");
		
		Map object  = (HashMap)parser.parse(json);
		
		if(object.isEmpty()){
			Map map = new HashMap<String,String>();
			map.put(STATUS_KEY, false);
			map.put(DATA, "Book is not present on Public Domain");
			return (HashMap<String, String>) map;
			
		}else{
			HashMap dataSet =(HashMap)object.get("ISBN:"+isbn);
			if(dataSet.get("preview").toString().equalsIgnoreCase("borrow")){
				Map map = new HashMap<String,String>();
				map.put(STATUS_KEY, true);
				map.put(DATA, "Book is present on Public Domain https://archive.org" );
				return (HashMap<String, String>) map;
				
			}else{
				Map map = new HashMap<String,String>();
				map.put(STATUS_KEY, false);
				map.put(DATA, "Book is not present on Public Domain");
				return (HashMap<String, String>) map;
			
			}
		}
		
	}
	
	private static HashMap<String,String> getPublicherFor_GoogleBook(String json,String isbn) throws ParseException{
		JSONParser parser = new JSONParser();	
		Map response =(HashMap)parser.parse(json);
		if(response.get("totalItems").toString().equals("0")){
			Map map = new HashMap<String,String>();
			map.put(STATUS_KEY, false);
			map.put(DATA, "Book is not present on Public Domain");
			return (HashMap<String, String>) map;			
		}else{
			List items = (ArrayList)response.get("items");
			Map item =(HashMap)items.get(0);
			if(item!=null && item.containsKey("accessInfo")){
				Map accessMap = (HashMap) item.get("accessInfo");
						if(Boolean.getBoolean(accessMap.get("publicDomain").toString()))
						{
							Map pdf =(HashMap)accessMap.get("pdf");
							if(Boolean.getBoolean(pdf.get("isAvailable").toString())){
								Map map = new HashMap<String,String>();
								map.put(STATUS_KEY, true);
								map.put(DATA, "Book is  present on Public Domain-https://www.googleapis.com");
								return (HashMap<String, String>) map;
							}else{
								Map map = new HashMap<String,String>();
								map.put(STATUS_KEY, false);
								map.put(DATA, "Book is not present on Public Domain");
								return (HashMap<String, String>) map;	
							}
								
							
						}else{
							Map map = new HashMap<String,String>();
							map.put(STATUS_KEY, false);
							map.put(DATA, "Book is not present on Public Domain");
							return (HashMap<String, String>) map;	
						}
			}
				
			
		}
		return null;
		
	}
	
	
	
	
}
		
		
		
		
		
	


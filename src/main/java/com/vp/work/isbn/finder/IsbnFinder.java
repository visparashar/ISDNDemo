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
	
	
	
	
	
	public HashMap<String,String> isPresentOnPublicLink(String isbn){
		
		try{
			if(null==isbn){
				return null;
			}
//			if(isbn.length()==10 || isbn.length()==13){
			isbn=isbn.replaceAll("-", "");
			System.out.println(isbn);
				System.out.println("Calling isbn rest service");
				
				RestTemplate restTemplate = new RestTemplate();
			     String uri= IsbnFinder.getServiceUrl(isbn);
			     
//			    / RestTemplate restTemplate = new RestTemplate();
			     String result = restTemplate.getForObject(uri, String.class);
//			   
//			     HttpHeaders headers = new HttpHeaders();
//			    headers.setAccept(Arrays.asList(org.springframework.http.MediaType.APPLICATION_JSON));
//			    HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
////			     
//			    ResponseEntity<String> result = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
			   return getPublisherName(result);
//			}
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
		
		
		
	}
	
	private static String getServiceUrl(String isbn)
	{
//		System.out.println(Config.ISBNDB_REST_URL+Config.AUTHORIZATION_KEY+File.separator+Config.SERVICE_TYPE_KEY+File.separator+isbn);
		return Config.ISBNDB_REST_URL+Config.AUTHORIZATION_KEY+"/"+Config.SERVICE_TYPE_KEY+"/"+isbn;
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
}

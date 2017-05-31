package com.vp.work.isbn.endpoint;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.vp.work.isbn.finder.IsbnFinder;

@RestController(value="/isbn")
public class Controller {
	
	@RequestMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE,  method = RequestMethod.GET)
	
	public @ResponseBody HashMap getPublishedStatus(@PathVariable String id) 
	{
		
	   IsbnFinder find = new IsbnFinder();
	   boolean response =find.isPresentOnPublicLink(id);
	  if(response){
		  Map<String,String> map = new HashMap();
		  map.put("Data", "Book is present on Public Link");
		   map.put("Status", "true");
		   return (HashMap) map;
	  }else{
		  Map<String,String> map = new HashMap();
		   map.put("Data", "Book is not present on Public Link");
		   map.put("Status", "false");
		   return (HashMap) map;
		  
	  }
		  
	   
	
		
	}

}

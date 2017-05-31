package com.vp.work.gutenberg;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import com.vp.work.isbn.config.Config;



public class GutenbergProcessFlow {
	private static  String 	FILE_PATH="resources/indexed.txt";
	
	
	public static boolean startGutenbergFlow(String bookTitle)
	{
//		need to first check and download the file
		File f = new File(FILE_PATH);
		if(!(f.exists() && !f.isDirectory())) { 
			try {
				downloadIndexFile(new URL(Config._GUTERBER_MIRROR_URL),FILE_PATH);
				
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			return false;
		}
		return searchStringInFile(f,bookTitle);
	}
	private static String getFilePath(){
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		Date date = new Date();
		 return	FILE_PATH.concat(dateFormat.format(date).toString())+".txt";
//		 	return null;
	}
	
	private static void downloadIndexFile(URL url,String destination)
	{
		try{
			org.apache.commons.io.FileUtils.copyURLToFile(url, new File(destination));
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	private static boolean searchStringInFile(File file,String keyword){
		try{
			boolean flag=false;
			LineIterator it=FileUtils.lineIterator(file, "UTF-8");
			 while (it.hasNext()) {
				 
			        String line = it.nextLine();
			        if(line.contains(keyword))
			        {
			        	flag=true;
			        	break;
			        }
			    }
			 return flag;
			
		}catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	

}

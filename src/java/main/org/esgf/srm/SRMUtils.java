package org.esgf.srm;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class SRMUtils {

	public static String THREDDS_DATAROOT = "/thredds/fileServer/esg_srm_dataroot";
	
	public static String SRM_CACHE_REPLACE = "/SRMTemp/";

    public static String srmAPIURL = "http://localhost:8080/esgf-stager/service/srmrequest?";
	
    public static String RESPONSE_MESSAGE = "Request Completed";
    
    public static String [] gridftp2httpArr(String [] grid_ftp_arr) {
    	String [] http_arr = new String[grid_ftp_arr.length];

    	for(int i=0;i<grid_ftp_arr.length;i++) {
    		http_arr[i] = gridftp2http(grid_ftp_arr[i]);
    	}
    	
    	return http_arr;

    }
	
	
	public static String gridftp2http(String gsiftp) {
		String http = "";
		
		http = transformServerName(gsiftp);
		
		http = http.replace("gsiftp", "http");
		
		http = http.replace("//lustre/esgfs/SRMTemp", THREDDS_DATAROOT);
		
		
		return http;
	}
	
	public static String extractServerName(String url) {
		
		System.out.println("url: " + url);
		
		String serverName = null;
		
		serverName = url.substring(0, url.indexOf("?"));
		
		return serverName;
	}
	
	public static String transformServerName(String url) {
		return url.replace("esg2-sdnl1.ccs.ornl.gov", "esg.ccs.ornl.gov");
	}
	
	
	
	
	public static String [] replaceCacheNames(String [] url) {
		String [] replaced_urls = new String [url.length];
		
		for(int i=0;i<replaced_urls.length;i++){
			replaced_urls[i] = replaceCacheName(url[i]);
		}
		
		return replaced_urls;
	}
	
	public static String replaceCacheName(String url) {
		return url.replace("/SRM/", SRM_CACHE_REPLACE);
	}
	
	public static String stripIndex(String url) {
		
		int endIndex = url.indexOf("|");
		if(endIndex == -1) {
			return url;
		} 
		else {
			return url.substring(0,endIndex);
		}
	}
	
	public static String extractFile() {
		String extractedFile = "";
		
		return extractedFile;
	}
	
	//input
	//srm://esg2-sdnl1.ccs.ornl.gov:46790/srm/v2/server?SFN=mss://esg2-sdnl1.ccs.ornl.gov//proj/cli049/UHRGCS/ORNL/CESM1/t341f02.FAMIPr/atm/hist/t341f02.FAMIPr.cam2.h0.1978-09.nc
	//output
	//gsiftp://esg2-sdnl1.ccs.ornl.gov//lustre/esgfs/SRM/shared/V.0.0-505553807/t341f02.FAMIPr.cam2.h0.1978-09.nc
	public static SRMResp simulateSRM(String [] inputFiles) {
		SRMResp srm_response = new SRMResp();
		
		String [] outputFiles = new String [inputFiles.length];
		
		/*
		for(int i=0;i<inputFiles.length;i++) {
			System.out.println("input file: " + i + " " + inputFiles[i]);
		}
		*/
		
		for(int i=0;i<inputFiles.length;i++) {
			String tempFile = inputFiles[i].replace("srm://esg2-sdnl1.ccs.ornl.gov:46790/srm/v2/server?SFN=mss://", "file:///");
			//tempFile = transformServerName(tempFile);
			
			File f = new File(tempFile);
			String fileName = f.getName();
			
			//String outputFile = "gsiftp://esg.ccs.ornl.gov:2811//lustre/esgfs/SRM/" + fileName;
			String outputFile = "gsiftp://esg.ccs.ornl.gov:2811//lustre/esgfs/" + fileName;
			
			outputFiles[i] = outputFile;
		}
		
		srm_response.setResponse_urls(outputFiles);
		srm_response.setMessage("Doin fine");
		
		return srm_response;
	}
	
	
	public static String extractFileName(String fileName) {
		String newFileName = "";
		String tempStr = "";
		
		int counter = fileName.length()-1;
		char ch = fileName.charAt(counter);
		while(ch != '/') {
			tempStr += ch;
			ch = fileName.charAt(counter);
			counter--;
		}	
		
		for(int i=tempStr.length()-1;i>=0;i--) {
			newFileName += tempStr.charAt(i);
		}
		
		return newFileName;
	}
	
	public static String [] extractFileNames(String [] fileNames) {
		String [] newFileNames = new String[fileNames.length];
		
		for(int i=0;i<fileNames.length;i++) {
			newFileNames[i] = extractFileName(fileNames[i]);
		}
		
		return newFileNames;
	}
	
	public static void main(String [] args) {
		
		
		//String url = "srm://esg2-sdnl1.ccs.ornl.gov:46790/srm/v2/server?" +
        //        "SFN=mss://esg2-sdnl1.ccs.ornl.gov/proj/cli049/UHRGCS/ORNL/CESM1" +
        //        "/t341f02.FAMIPr/atm/hist/t341f02.FAMIPr.cam2.h0.1979-01.nc";
		
		
		String url = "srm://esg2-sdnl1.ccs.ornl.gov:46790/srm/v2/server?" +
					 "SFN=mss://esg2-sdnl1.ccs.ornl.gov//proj/cli049/UHRGCS/ORNL/CESM1/" +
				     "t341f02.FAMIPr/atm/hist/t341f02.FAMIPr.cam2.h0.1978-09.nc";
		
		String gridftp = "gsiftp://esg.ccs.ornl.gov//lustre/esgfs/SRMTemp/shared/V.0.0-730290723/t341f02.FAMIPr.cam2.h0.1978-10.nc";
		
		String ftp = gridftp2http(gridftp);
		System.out.println("ftp: " + ftp);
		/*
		String [] srm_urls = new String [1];
		
		srm_urls[0] = url;
		
		System.out.println(extractFileName(url));
		
		SRMResponse srm_response = new SRMResponse();
		
		srm_response = simulateSRM(srm_urls);
		
		String [] response_urls = replaceCacheNames(srm_response.getResponse_urls());
		
		for(int i=0;i<response_urls.length;i++) {
			System.out.println("resp: " + i + " " + response_urls[i]) ;
		}
		*/
		
	}
}

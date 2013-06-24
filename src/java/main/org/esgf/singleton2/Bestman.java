package org.esgf.singleton2;

import gov.lbl.srm.StorageResourceManager.TStatusCode;
import gov.lbl.srm.client.wsdl.FileStatus;
import gov.lbl.srm.client.wsdl.SRMRequest;
import gov.lbl.srm.client.wsdl.SRMRequestStatus;
import gov.lbl.srm.client.wsdl.SRMServer;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.esgf.stager.SRMReq;
import org.esgf.stager.SRMResp;
import org.esgf.stager.SRMUtils;

public class Bestman {

	private static Bestman firstInstance = null;
    
	private static final int MIN_SLEEP = 10;
	private static final int MAX_SLEEP = 600;
	
	//private SRMReq srm_request;
	//private SRMResp srm_response;
	
	private String word;
	
	
	public static String BESTMAN_SERVER_PROPERTIES_FILE = "/esg/config/stager_server.properties";
	public static String DEFAULT_MESSAGE = "/esg/config/stager_server.properties";
	
	
	public Bestman() {
		//this.srm_request = new SRMReq();
		//this.srm_response = new SRMResp();
	}
	
	private Bestman(String id) { 
    	this.setId(id);
    	
    }
     
    
	
	public Bestman(SRMReq srm_request) {
		//System.out.println("Second cons");
		//this.srm_request = srm_request;
		//this.srm_response = new SRMResp();
		
	}
	
	public Bestman(String [] file_urls,String id) {
		//System.out.println("Third cons");
		//this.srm_request = new SRMReq(file_urls);
		//this.srm_response = new SRMResp();
		this.setId(id);
	}
	
	//id of the thread accessing bestman
	private String id;
    
    // Used to slow down 1st thread
    static boolean firstThread = true;
     
    // Created to keep users from instantiation
    // Only Singleton will be able to instantiate this class
    
    
    
    
    public static Bestman getInstance(String id) {
    	
    	
    	if(firstInstance == null) {
            
           
            // Here we just use synchronized when the first object
            // is created
             
            synchronized(Bestman.class){
             
            	if(firstInstance == null) {
                    // If the instance isn't needed it isn't created
                    // This is known as lazy instantiation
             
                    firstInstance = new Bestman(id);
             
                     
                    
                } 
            }
             
        }
    	
    	return firstInstance;
    }
   
    
    public synchronized void get(String thread_id,String [] response_file_urls) {
		System.out.println("id: " + thread_id + " entering get");
		
		
    	BestmanServerProperties bestman_server_properties = 
    			new BestmanServerProperties(BESTMAN_SERVER_PROPERTIES_FILE);
    	
    	
    	
        if(firstThread){
         
            firstThread = false;
             
            try {
                Thread.currentThread();
                Thread.sleep(3000);
            } catch (InterruptedException e) {
             
                e.printStackTrace();
            }
        }
        
        
        String storageInfo = bestman_server_properties.getStorageInfo();
    	
        
        try{
        	
        	if(!storageInfo.equals("")) {
        		bestman_server_properties.setDelegationNeeded(true);
        	}
	    	System.out.println("CC Initialized");
		    
	    	String log4jlocation = bestman_server_properties.getLog4jlocation();
	    	String logPath = bestman_server_properties.getLogPath();
	    	boolean isDebug = bestman_server_properties.isDebug();
	    	boolean isDelegationNeeded = bestman_server_properties.isDelegationNeeded();
	    	String fileType = bestman_server_properties.getFileType();
	    	String retentionPolicy = bestman_server_properties.getRetentionPolicy();
	    	String accessLatency = bestman_server_properties.getAccessLatency();
		    
	    	SRMServer cc = new SRMServer(log4jlocation, 
	    			logPath, 
	    			isDebug,
	    			isDelegationNeeded);
	    	

		    System.out.println("credential: " + cc.getCredential().getName());
		    System.out.println("usage: " + cc.getCredential().getUsage());
	    	
	    	bestman_server_properties.setServer_url(response_file_urls);
	    	
	    	String server_url = bestman_server_properties.getServer_url();
	    	
	    	System.out.println("Server url: " + server_url);
	    	
	    	cc.connect(server_url);
	    	
		    System.out.println("Connection Established");
		    
		    SRMRequest request = new SRMRequest();
		    request.setSRMServer(cc);
		    

		    String uid = bestman_server_properties.getUid();
		    boolean debug = bestman_server_properties.isDebug();
		    boolean delegation = bestman_server_properties.isDelegationNeeded();
		    
		    for(int i=0;i<response_file_urls.length;i++) {
		    	System.out.println("url: " + i + " " + response_file_urls[i]);
			    
		    }
		    System.out.println("uid: " + uid);
		    System.out.println("storage info: " + storageInfo);
		    System.out.println("filetype: " + fileType);
		    System.out.println("retentionPolicy: " + retentionPolicy);
		    System.out.println("accessLatency: " + accessLatency);
		    System.out.println("debug: " + debug);
		    System.out.println("delegation: " + delegation);
		    
		    request.setAuthID(uid);
		    request.setRequestType("get");
		    request.addFiles(response_file_urls, null,null);
		    request.setStorageSystemInfo(storageInfo);
		    request.setFileStorageType(fileType);
		    request.setRetentionPolicy(retentionPolicy);
		    request.setAccessLatency(accessLatency);
		    System.out.println("Submitting...\n\n");
		    
		    
		    request.submit();
		    request.checkStatus();
		    
		    int sleepTime  = MIN_SLEEP;
		    SRMRequestStatus response = request.getStatus();
		    
		    
		    String message = DEFAULT_MESSAGE;
		    String retStr = "";
		    
		    
		    if(response != null) {
		    	
		    	
		    	while(response.getReturnStatus().getStatusCode() == TStatusCode.SRM_REQUEST_QUEUED ||
		                response.getReturnStatus().getStatusCode() == TStatusCode.SRM_REQUEST_INPROGRESS){
		    
		    		System.out.println("\nRequest.status="+response.getReturnStatus().getStatusCode());
			        System.out.println("request.explanation="+response.getReturnStatus().getExplanation());
//			        outLogFile.write("request.explanation="+response.getReturnStatus().getExplanation()+"\n");
			        System.out.println("Request urls (first 3)");
			        for(int i=0;i<response_file_urls.length;i++) {
			        	if(i < 3) {
			        		System.out.println("url: " + response_file_urls[i]);
			        	}
			        }
			    	System.out.println("SRM-CLIENT: Next status call in "+ sleepTime + " secs");
		        	Thread.currentThread().sleep(sleepTime * 1000);
		        	sleepTime*=2;
		    	
		        	if(sleepTime>=MAX_SLEEP){
		        		sleepTime=MAX_SLEEP;
		        	}
		        	
		        	//CHECK STATUS AGAIN
		        	request.checkStatus();
				    response = request.getStatus();
		        	
				  //If failed to extract then exit
		        	if(!(response.getReturnStatus().getStatusCode() == TStatusCode.SRM_SUCCESS || 
		        			response.getReturnStatus().getStatusCode() == TStatusCode.SRM_FILE_PINNED ||
		        			response.getReturnStatus().getStatusCode() == TStatusCode.SRM_REQUEST_QUEUED ||
		        			response.getReturnStatus().getStatusCode() == TStatusCode.SRM_REQUEST_INPROGRESS)){
		        		System.out.println("SRM failed to extract file. Exiting.");
		        		message = "<srm_error>" + response.getReturnStatus().getStatusCode().toString() + "</srm_error>";
		        		System.out.println("status code: " + response.getReturnStatus().getStatusCode().toString());
		        		System.out.println("explanation: " + response.getReturnStatus().getExplanation());
		        		//sendRequestFailed(response.getReturnStatus().getStatusCode().toString());
		        		cc.disconnect();
		        		
		        	}
		    	}
		    	
		    	
		    	
		    	System.out.println("\nStatus.code="+response.getReturnStatus().getStatusCode());
		        //System.out.println("\nStatus.exp="+response.getReturnStatus().getExplanation());
		        
		    	if(response.getReturnStatus().getStatusCode() == TStatusCode.SRM_SUCCESS ||
	    			response.getReturnStatus().getStatusCode() == TStatusCode.SRM_FILE_PINNED) {
		    		retStr="";
		    		HashMap map = response.getFileStatuses();
		    		Set set = map.entrySet();
		    		Iterator i = set.iterator();
		    		int j=0;
		    		while(i.hasNext()) {
		    			Map.Entry me = (Map.Entry) i.next();
		    			String key =  (String) me.getKey();
		    			Object value = me.getValue();
		    			if(value != null) {
		    				FileStatus fileStatus = (FileStatus) value;
		    				org.apache.axis.types.URI uri = fileStatus.getTransferSURL();
		    				
		    				System.out.println("\nTransferSURL="+uri);
		    				
		    				retStr += (uri.toString()+";");	//Return value
		    			}
		    		}
		          cc.disconnect();
		    		
		    	}//end if success
		    	
		    	
		    	
		    	
		    } //end if response != null
		    
		    
		    
        }catch(Exception e) {
        	e.printStackTrace();
        }
        
	    
	    
	    //System.out.println("MESSAGE: " + message);
		//this.srm_response.setMessage(message);
		
		
        System.out.println("id: " + thread_id + " exiting get");
        
        
        
         
    }



	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}




	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the word
	 */
	public String getWord() {
		return word;
	}

	/**
	 * @param word the word to set
	 */
	public void setWord(String word) {
		this.word = word;
	}
    
    
    
}




/*
String message = "";

String retStr = "";

String [] response_file_urls = new String[this.srm_request.getFile_urls().length];



try{
	if(!this.srm_request.getStorageInfo().equals("")) {
       this.srm_request.setDelegationNeeded(true);
    }
	System.out.println("CC Initialized");
    
    
    SRMServer cc = new SRMServer(this.srm_request.getLog4jlocation(), 
    							 this.srm_request.getLogPath(), 
    							 this.srm_request.isDebug(),
    							 this.srm_request.isDelegationNeeded());
    
    
    cc.connect(this.srm_request.getServer_url());
    System.out.println("Connection Established");
    
    
    
    SRMRequest req = new SRMRequest();
    req.setSRMServer(cc);
    req.setAuthID(this.srm_request.getUid());
    req.setRequestType("get");
    
    
    req.addFiles(this.srm_request.getFile_urls(), null,null);
    req.setStorageSystemInfo(this.srm_request.getStorageInfo());
    req.setFileStorageType(this.srm_request.getFileType());
    req.setRetentionPolicy(this.srm_request.getRetentionPolicy());
    req.setAccessLatency(this.srm_request.getAccessLatency());
    System.out.println("Submitting...\n\n");
    

    req.submit();
    
    req.checkStatus();
    
    int sleepTime  = MIN_SLEEP;
    
    SRMRequestStatus response = req.getStatus();
    
    
    if(response != null) {
    	while(response.getReturnStatus().getStatusCode() == TStatusCode.SRM_REQUEST_QUEUED ||
                response.getReturnStatus().getStatusCode() == TStatusCode.SRM_REQUEST_INPROGRESS){
    
    		System.out.println("\nRequest.status="+response.getReturnStatus().getStatusCode());
	        System.out.println("request.explanation="+response.getReturnStatus().getExplanation());
//	        outLogFile.write("request.explanation="+response.getReturnStatus().getExplanation()+"\n");
	        System.out.println("Request urls (first 3)");
	        for(int i=0;i<this.srm_request.getFile_urls().length;i++) {
	        	if(i < 3) {
	        		System.out.println("url: " + this.srm_request.getFile_urls()[i]);
	        	}
	        }
	    	System.out.println("SRM-CLIENT: Next status call in "+ sleepTime + " secs");
        	Thread.currentThread().sleep(sleepTime * 1000);
        	sleepTime*=2;
    	
        	if(sleepTime>=MAX_SLEEP){
        		sleepTime=MAX_SLEEP;
        	}
        	
        	//CHECK STATUS AGAIN
        	req.checkStatus();
		    response = req.getStatus();
        	
		  //If failed to extract then exit
        	if(!(response.getReturnStatus().getStatusCode() == TStatusCode.SRM_SUCCESS || 
        			response.getReturnStatus().getStatusCode() == TStatusCode.SRM_FILE_PINNED ||
        			response.getReturnStatus().getStatusCode() == TStatusCode.SRM_REQUEST_QUEUED ||
        			response.getReturnStatus().getStatusCode() == TStatusCode.SRM_REQUEST_INPROGRESS)){
        		System.out.println("SRM failed to extract file. Exiting.");
        		message = "<srm_error>" + response.getReturnStatus().getStatusCode().toString() + "</srm_error>";
        		System.out.println("status code: " + response.getReturnStatus().getStatusCode().toString());
        		System.out.println("explanation: " + response.getReturnStatus().getExplanation());
        		//sendRequestFailed(response.getReturnStatus().getStatusCode().toString());
        		cc.disconnect();
        		
        	}
    	}
    	
    	
    	
    	System.out.println("\nStatus.code="+response.getReturnStatus().getStatusCode());
        //System.out.println("\nStatus.exp="+response.getReturnStatus().getExplanation());
        
    	if(response.getReturnStatus().getStatusCode() == TStatusCode.SRM_SUCCESS ||
          response.getReturnStatus().getStatusCode() == TStatusCode.SRM_FILE_PINNED) {
    		retStr="";
    		HashMap map = response.getFileStatuses();
    		Set set = map.entrySet();
    		Iterator i = set.iterator();
    		int j=0;
    		while(i.hasNext()) {
    			Map.Entry me = (Map.Entry) i.next();
    			String key =  (String) me.getKey();
    			Object value = me.getValue();
    			if(value != null) {
    				FileStatus fileStatus = (FileStatus) value;
    				org.apache.axis.types.URI uri = fileStatus.getTransferSURL();
    				
    				System.out.println("\nTransferSURL="+uri);
    				
    				retStr += (uri.toString()+";");	//Return value
    			}
    		}//end while
          cc.disconnect();
    		
       }//end if
    	
    	
    }
    
    //System.out.println("ReturnStr: " + retStr);
	
	if(retStr != null) {
		String [] response_urls = retStr.split(";");
		
		this.srm_response.setResponse_urls(response_urls);
		message = SRMUtils.RESPONSE_MESSAGE;
		
	}
	
    
    
} catch(Exception e) {
	e.printStackTrace();
}
*/

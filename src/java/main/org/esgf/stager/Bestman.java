package org.esgf.stager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.esgf.stager.BestmanServerProperties;
import org.esgf.stager.utils.XmlFormatter;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import gov.lbl.srm.StorageResourceManager.TStatusCode;
import gov.lbl.srm.client.wsdl.FileStatus;
import gov.lbl.srm.client.wsdl.SRMRequest;
import gov.lbl.srm.client.wsdl.SRMRequestStatus;
import gov.lbl.srm.client.wsdl.SRMServer;

public class Bestman {

	private static final int MIN_SLEEP = 10;
	private static final int MAX_SLEEP = 180;

	private static Bestman firstInstance = null;

	
	private String thread_id;

	public static String BESTMAN_SERVER_PROPERTIES_FILE = "/esg/config/stager_server.properties";

	public static boolean firstThread = true;
	

	public Bestman(String thread_id) {
		this.thread_id = thread_id;
	}
	
	
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
	
	public synchronized String get(String [] file_urls) {
		return get(file_urls,"-1");
	}
	
	
	public synchronized String get(String [] file_urls,String thread_id) {

		System.out.println("Thread id: " + thread_id + " entering get");
	    
	    String message = "";

	    String response_message = "";
	    		
    	String retStr = "";
    	
    	String [] response_file_urls = new String[file_urls.length];
	    
    	BestmanServerProperties bestman_server_properties = 
    			new BestmanServerProperties(BESTMAN_SERVER_PROPERTIES_FILE);
    	
    	if(firstThread){
            
            firstThread = false;
             
            try {
                Thread.currentThread();
                Thread.sleep(5000);
            } catch (InterruptedException e) {
             
                e.printStackTrace();
            }
        }
    	
    	String log4jlocation = bestman_server_properties.getLog4jlocation();
    	String logPath = bestman_server_properties.getLogPath();
    	boolean isDebug = bestman_server_properties.isDebug();
    	boolean isDelegationNeeded = bestman_server_properties.isDelegationNeeded();
    	String fileType = bestman_server_properties.getFileType();
    	String retentionPolicy = bestman_server_properties.getRetentionPolicy();
    	String accessLatency = bestman_server_properties.getAccessLatency();
	    
    	String storageInfo = bestman_server_properties.getStorageInfo();
    	
	    try{
	    	
	    	for(int i=0;i<file_urls.length;i++) {
	    		//System.out.println("i: " + i);
	    		//System.out.println("\t" + file_urls[i]);
	    		response_file_urls[i] = SRMUtils.transformServerName(file_urls[i]);
	    	}
	    	
	    	
	    	if(storageInfo.equals("")) {
		       bestman_server_properties.setDelegationNeeded(true);
		    }
	    	//System.out.println("\n\n\ttrying...");
		    System.out.println("CC Initialized");

		    bestman_server_properties.setServer_url(response_file_urls);
	    	
		    
		    String server_url = bestman_server_properties.getServer_url();
	    	
	    	//System.out.println("Server url: " + server_url);
	    	
		    
		    
		    SRMServer cc = new SRMServer(log4jlocation, 
		    							 logPath, 
		    							 isDebug,
		    							 isDelegationNeeded);

		    
		    
		    cc.connect(server_url);
		    
		    
		    
		    SRMRequest req = new SRMRequest();
		    req.setSRMServer(cc);
		    
		    String uid = bestman_server_properties.getUid();
		    boolean debug = bestman_server_properties.isDebug();
		    boolean delegation = bestman_server_properties.isDelegationNeeded();
		    
		    req.setAuthID(uid);
		    req.setRequestType("get");
		    
		    
		    req.addFiles(response_file_urls, null,null);
		    req.setStorageSystemInfo(storageInfo);
		    req.setFileStorageType(fileType);
		    req.setRetentionPolicy(retentionPolicy);
		    req.setAccessLatency(accessLatency);
		    System.out.println("Submitting...\n\n");
		    
		    //System.out.println(this.toString());
		    
		    
		    
		    req.submit();
		    
		    req.checkStatus();
		    
		    int sleepTime  = MIN_SLEEP;
		    
		    
		    SRMRequestStatus response = req.getStatus();
		    
		    
		    
	    	
	    	
		    if(response != null) {
		    	while(response.getReturnStatus().getStatusCode() == TStatusCode.SRM_REQUEST_QUEUED ||
		                response.getReturnStatus().getStatusCode() == TStatusCode.SRM_REQUEST_INPROGRESS){
		    
		    		System.out.println("\nRequest.status="+response.getReturnStatus().getStatusCode());
			        //System.out.println("request.explanation="+response.getReturnStatus().getExplanation());
//			        outLogFile.write("request.explanation="+response.getReturnStatus().getExplanation()+"\n");
			        
			        System.out.println("Request urls (first " + SRMUtils.MAX_URL_DISPLAY + ")");
			        for(int i=0;i<response_file_urls.length;i++) {
			        	if(i < SRMUtils.MAX_URL_DISPLAY) {
			        		System.out.println("url: " + response_file_urls[i] + " thread_id: " + thread_id);
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
		    				
		    				//System.out.println("\nTransferSURL="+uri);
		    				
		    				//System.out.println("\tTURL: " + fileStatus.getTURL());
		    				//org.apache.axis.types.URI uri2 = fileStatus.getTURL();
		    				
		    				//System.out.println("\nTURL="+uri2);
		    				//System.out.println("Pin Time:"+fileStatus.getPinLifeTime());	
		    				retStr += (uri.toString()+";");	//Return value
    	                //response_file_urls[j] = uri.toString();
		    			}
		    		}//end while
    	          cc.disconnect();
    	          //Notify by e-mail that request has been completed successfully.//
	              //sendRequestCompletion(retStr);
    	          
    	          //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
	        		
    	       }//end if
		    	
		    	
		    }
		    
		    //System.out.println("ReturnStr: " + retStr);
			
			if(retStr != null) {
				String [] response_urls = retStr.split(";");
				
				//this.srm_response.setResponse_urls(response_urls);
				String error = "0";
				
				message = SRMUtils.RESPONSE_MESSAGE_SUCCESS;
				//message += retStr;
				//construct full message here
				response_message = this.toResponseXML(message, error, response_urls);
				
				//System.out.println("Response Message:\n" + new XmlFormatter().format(response_message));
				
			}
			
		    
		    
	    } catch(Exception e) {
	    	System.out.println("\n\tcommunication with BESTMAN failed...\n\n");
	    	e.printStackTrace();
	    	message = SRMUtils.RESPONSE_MESSAGE_FAILURE;
	    	String error = "-1";
	    	String [] response_urls = null;
	    	
	    	response_message = this.toResponseXML(response_message, error, response_urls);
	    }

	    //System.out.println("Thread_id: " + thread_id + " producing MESSAGE: \n" + message);
		//this.srm_response.setMessage(message);
		
	    return response_message;
	    
	}
	
	
	/** Description of toXML()
     * 
     * @return
     */
    private String toResponseXML(String message,String error,String [] response_urls) {
        String xml = "";
        
        Element srm_responseEl = new Element("srm_response");

        if(message != null) {
            Element messageEl = new Element("message");
            messageEl.addContent(message);
            srm_responseEl.addContent(messageEl);
        }
        
        if(error != null) {
            Element errorEl = new Element("error");
            errorEl.addContent(error);
            srm_responseEl.addContent(errorEl);
        }
        
        if(response_urls != null) {
        	Element response_urlsEl = new Element("response_urls");
        	
        	for(int i=0;i<response_urls.length;i++) {
        		Element response_urlEl = new Element("response_url");
        		String response_url = response_urls[i];
        		response_urlEl.addContent(response_url);
        		response_urlsEl.addContent(response_urlEl);
        	}
        	
        	srm_responseEl.addContent(response_urlsEl);
        }
        
        

        XMLOutputter outputter = new XMLOutputter();
        xml = outputter.outputString(srm_responseEl);
        
        return xml;
    }
	
	
}

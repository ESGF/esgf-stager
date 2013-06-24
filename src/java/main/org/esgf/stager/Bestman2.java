package org.esgf.stager;

import gov.lbl.srm.client.wsdl.SRMRequest;
import gov.lbl.srm.client.wsdl.SRMRequestStatus;
import gov.lbl.srm.client.wsdl.SRMServer;

import org.esgf.singleton2.Bestman;
import org.esgf.singleton2.BestmanServerProperties;

public class Bestman2 {

	private String thread_id;
	private static Bestman2 firstInstance = null;

    // Used to slow down 1st thread
    static boolean firstThread = true;

	private static final int MIN_SLEEP = 10;
	private static final int MAX_SLEEP = 600;
	

	public static String BESTMAN_SERVER_PROPERTIES_FILE = "/esg/config/stager_server.properties";
    
	public Bestman2(String thread_id) {
		this.thread_id = thread_id;
	}
	
	
	public static Bestman2 getInstance(String id) {
    	
    	if(firstInstance == null) {
            
            // Here we just use synchronized when the first object
            // is created
            synchronized(Bestman2.class){
             
            	if(firstInstance == null) {
                    // If the instance isn't needed it isn't created
                    // This is known as lazy instantiation
             
                    firstInstance = new Bestman2(id);
             
                     
                    
                } 
            }
             
        }
    	
    	return firstInstance;
    }
	
	public synchronized void get(String thread_id,String [] file_urls) {
		
		String message = "";

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
	    	if(!storageInfo.equals("")) {
		       bestman_server_properties.setDelegationNeeded(true);
		    }
	    
	    	SRMServer cc = new SRMServer(log4jlocation, 
					 					 logPath, 
					 					 isDebug,
					 					 isDelegationNeeded);

	    	
	    	
	    	bestman_server_properties.setServer_url(file_urls);
	    	String server_url = bestman_server_properties.getServer_url();
	    	
	    	System.out.println("Server url: " + server_url);
	    	
	    	cc.connect(server_url);
		    

		    SRMRequest request = new SRMRequest();
		    request.setSRMServer(cc);
		    

		    
		    
		    String uid = bestman_server_properties.getUid();
		    boolean debug = bestman_server_properties.isDebug();
		    boolean delegation = bestman_server_properties.isDelegationNeeded();
		    
		    for(int i=0;i<response_file_urls.length;i++) {
		    	System.out.println("url: " + i + " " + file_urls[i]);
			    
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
		    request.addFiles(file_urls, null,null);
		    request.setStorageSystemInfo(storageInfo);
		    request.setFileStorageType(fileType);
		    request.setRetentionPolicy(retentionPolicy);
		    request.setAccessLatency(accessLatency);
		    System.out.println("Submitting...\n\n");
		    
		    
		    request.submit();
		    request.checkStatus();
		    
		    int sleepTime  = MIN_SLEEP;
		    SRMRequestStatus response = request.getStatus();
		    
		    
		    //String message = DEFAULT_MESSAGE;
		    //String retStr = "";
		    
	    	
	    	
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    	
    	
		System.out.println("\n\n\n\nThead id: " + thread_id + " finished\n\n\n\n");
		
	}
	
	
	
	
	
}

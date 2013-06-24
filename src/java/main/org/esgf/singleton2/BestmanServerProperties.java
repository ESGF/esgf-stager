package org.esgf.singleton2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.esgf.stager.SRMUtils;

public class BestmanServerProperties {

	public static String BESTMAN_SERVER_PROPERTIES_FILE = "/esg/config/stager_server.properties";
	
	
	//defaults
	private String uid="";
	private String logPath="/tmp/esg-srm.log";
    private String log4jlocation="";
    private String storageInfo="";
    private String fileType="volatile";
    private String retentionPolicy="replica";
    private String accessLatency="online";
    private boolean debug = false;
    private boolean delegationNeeded=false;
    private String server_url;
    
    
    public static void main(String [] args) {
    	BestmanServerProperties properties = new BestmanServerProperties(BESTMAN_SERVER_PROPERTIES_FILE);
    	
    	System.out.println(properties.getAccessLatency());
    	
    }
    
    public BestmanServerProperties(String fileName) {
    	
    	Properties prop = new Properties();
    	
    	try {
            
    		File file = new File(BESTMAN_SERVER_PROPERTIES_FILE);
    		FileInputStream fis = new FileInputStream(file);
    		
    		//load a properties file
    		prop.load(fis);

    		this.accessLatency = prop.getProperty("accessLatency");
    		this.uid = prop.getProperty("uid");
    		this.logPath = prop.getProperty("logPath");
    	    this.log4jlocation = prop.getProperty("log4jlocation");
    	    this.storageInfo = prop.getProperty("storageInfo");
    	    this.fileType = prop.getProperty("fileType");
    	    this.retentionPolicy = prop.getProperty("retentionPolicy");
    	    this.accessLatency = prop.getProperty("accessLatency");
    	    this.debug = Boolean.parseBoolean(prop.getProperty("debug"));
    	    this.delegationNeeded = Boolean.parseBoolean(prop.getProperty("delegationNeeded"));
    		
    	    fis.close();

    	} catch (IOException ex) {

    		prop.setProperty("uid",this.uid);
    		prop.setProperty("logPath", this.logPath);
    		prop.setProperty("log4jlocation", this.log4jlocation);
    		prop.setProperty("storageInfo", this.storageInfo);
    		prop.setProperty("fileType", this.fileType);
    		prop.setProperty("retentionPolicy", this.retentionPolicy);
    		prop.setProperty("accessLatency", this.accessLatency);
    		prop.setProperty("debug", Boolean.toString(this.debug));
    		prop.setProperty("delegationNeeded", Boolean.toString(this.delegationNeeded));
    		
    		try {
    			
    			File file = new File(BESTMAN_SERVER_PROPERTIES_FILE);
    			FileOutputStream fos = new FileOutputStream(file);
				
    			prop.store(fos, null);
				
				fos.close();
				
				FileInputStream fis = new FileInputStream(file);
	    		
	    		//load a properties file
	    		prop.load(fis);

	    		this.accessLatency = prop.getProperty("accessLatency");
	    		this.uid = prop.getProperty("uid");
	    		this.logPath = prop.getProperty("logPath");
	    	    this.log4jlocation = prop.getProperty("log4jlocation");
	    	    this.storageInfo = prop.getProperty("storageInfo");
	    	    this.fileType = prop.getProperty("fileType");
	    	    this.retentionPolicy = prop.getProperty("retentionPolicy");
	    	    this.accessLatency = prop.getProperty("accessLatency");
	    	    this.debug = Boolean.parseBoolean(prop.getProperty("debug"));
	    	    this.delegationNeeded = Boolean.parseBoolean(prop.getProperty("delegationNeeded"));
	    		
	    	    fis.close();
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    		
    		
    		
    	}
    	
    	
    }

    public BestmanServerProperties(String [] file_urls) {
    	this.setServer_url(SRMUtils.extractServerName(file_urls[0]));
    }
    
    public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getLogPath() {
		return logPath;
	}
	public void setLogPath(String logPath) {
		this.logPath = logPath;
	}
	public String getLog4jlocation() {
		return log4jlocation;
	}
	public void setLog4jlocation(String log4jlocation) {
		this.log4jlocation = log4jlocation;
	}
	public String getStorageInfo() {
		return storageInfo;
	}
	public void setStorageInfo(String storageInfo) {
		this.storageInfo = storageInfo;
	}
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	public String getRetentionPolicy() {
		return retentionPolicy;
	}
	public void setRetentionPolicy(String retentionPolicy) {
		this.retentionPolicy = retentionPolicy;
	}
	public String getAccessLatency() {
		return accessLatency;
	}
	public void setAccessLatency(String accessLatency) {
		this.accessLatency = accessLatency;
	}
	public boolean isDebug() {
		return debug;
	}
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	public boolean isDelegationNeeded() {
		return delegationNeeded;
	}
	public void setDelegationNeeded(boolean delegationNeeded) {
		this.delegationNeeded = delegationNeeded;
	}

	/**
	 * @return the server_url
	 */
	public String getServer_url() {
		return server_url;
	}
	
	public void setServer_url(String [] file_urls) {
		this.setServer_url(SRMUtils.extractServerName(file_urls[0]));
	}

	/**
	 * @param server_url the server_url to set
	 */
	public void setServer_url(String server_url) {
		this.server_url = server_url;
	}
}

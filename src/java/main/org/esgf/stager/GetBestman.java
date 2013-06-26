package org.esgf.stager;

import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

	 
public class GetBestman implements Runnable {
	    
	private HttpServletRequest request;
	private HttpServletResponse response;
	
	public GetBestman(HttpServletRequest request,final HttpServletResponse response) {
		this.request = request;
		this.response = response;
	}
	
	
    public void run(){
 
    		Random random = new Random();
    		double d_id = random.nextDouble();
    		String thread_id = Double.toString(d_id);
    		
    		SynchronizedSRMRequestController controller  = new SynchronizedSRMRequestController();
    		
    		controller.addSynchronizedSRMRequest(this.request,this.response,thread_id);
    		
         
         
    }
     
}

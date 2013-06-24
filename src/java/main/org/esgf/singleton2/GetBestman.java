package org.esgf.singleton2;

import java.util.LinkedList;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import org.esgf.stager.SRMRequestController;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
	 
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
    	
    		
    		/*
    		final MockHttpServletRequest mockRequest = new MockHttpServletRequest();
    		final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

    		String [] url = {
    				"srm://esg2-sdnl1.ccs.ornl.gov:46790/srm/v2/server?SFN=mss://esg2-sdnl1.ccs.ornl.gov//proj/cli049/UHRGCS/ORNL/CESM1/t341f02.FAMIPr/atm/hist/t341f02.FAMIPr.cam2.h0.1978-10.nc",
    				"srm://esg2-sdnl1.ccs.ornl.gov:46790/srm/v2/server?SFN=mss://esg2-sdnl1.ccs.ornl.gov//proj/cli049/UHRGCS/ORNL/CESM1/t341f02.FAMIPr/atm/hist/t341f02.FAMIPr.cam2.h0.1979-06.nc"
    			};

    		String length = "2";
			


    		mockRequest.addParameter("url", url);
    		mockRequest.addParameter("length", length);
    		*/
    		
    		//MockController controller = new MockController();
    		
    		SynchronizedSRMRequestController controller  = new SynchronizedSRMRequestController();
    		
    		//controller.addSRMRequest(mockRequest,mockResponse,thread_id);
    	
    		controller.addSRMRequest3(this.request,this.response,thread_id);
    		
            // How you create a new instance of Singleton
            //Bestman newInstance = Bestman.getInstance(thread_id);
             
            
            // Get unique id for instance object
            //System.out.println("Thread ID: " + thread_id + " " + "Bestman Instance ID: " + System.identityHashCode(newInstance));
             
            // Get all of the letters stored in the List
             
            
            //newInstance.get(thread_id);
            
            //System.out.println(newInstance.getWord());
             
            //System.out.println("Player 1: ");
         
    }
     
}

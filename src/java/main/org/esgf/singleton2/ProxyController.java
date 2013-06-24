package org.esgf.singleton2;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

public class ProxyController {

	@RequestMapping(method=RequestMethod.POST, value="/multithreadtest2")
	//public ModelAndView addEmployee(@RequestBody String body) {
	public @ResponseBody String doMultithreadTest(HttpServletRequest request,final HttpServletResponse response) {
	
		System.out.println("doing multithreadtest");
		
		//thread 1
        Runnable getBestman = new GetBestman(request,response);
        
        
        //thread 2
        Runnable getBestmanAgain = new GetBestman(request,response);
        
        // Call for the code in the method run to execute
                 
        new Thread(getBestman).start();
        new Thread(getBestmanAgain).start();
         
	
		return "done";
	}
	
}

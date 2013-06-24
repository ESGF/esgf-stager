package org.esgf.singleton2;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.ResponseBody;

public class MockController {

	private Bestman bestman;
	
	public static void main(String [] args) {
		
	}
	
	
	
	public MockController() {
		
	}
	
	
	public String addSRMRequest(HttpServletRequest request,final HttpServletResponse response,String id) {
	
		String length = request.getParameter("length");
		if(length == null) {
			length = "1";
		}

		System.out.println("Length: " + length);
		
		String [] file_urls = null; 
		
		if(length.equals("1")) {
			file_urls = new String[1];
			String file_url = request.getParameter("url");
			//System.out.println("file_url: " + file_url);
			file_urls[0] = file_url;
		} 
		else {
			file_urls = new String[Integer.parseInt(length)];
			String [] urls = request.getParameterValues("url");
			for(int i=0;i<urls.length;i++) {
				file_urls[i] = urls[i];
			}
		}

		
		
		this.bestman = Bestman.getInstance(id);
		
		System.out.println("Setting word to " + "id" + id);
		
		this.bestman.setWord("id" + id);
		
		
		
		this.bestman.get(id,file_urls);
		
		return null;
		
	}
	
}

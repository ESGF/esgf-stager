package org.esgf.stager.example;

public class SRMRequestModel {

	
	
}





/*

		//grab params
		System.out.println("In HTTP POST: addSRMRequest");
		
		String openid = request.getParameter("openid");
		if(openid == null) {
			this.openid = "jfharney";
		} else {
			this.openid = openid;
		}

		String length = request.getParameter("length");
		if(length == null) {
			length = "1";
		}
		
		String file_request_type = request.getParameter("file_request_type");
		if(file_request_type == null) {
			this.file_request_type = "http";
			//file_request_type = "http";
		} else if(file_request_type.equals("http")){
			this.file_request_type = "http";
		} else if(file_request_type.equals("gridftp")) {
			this.file_request_type = "gridftp";
		} else if(file_request_type.equals("globusonline")) {
			this.file_request_type = "globusonline";
		} else {
			this.file_request_type = "http";
		}
		
		
		
		String [] file_urls = null; 
		
		System.out.println("Length: " + length);
		
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



*/
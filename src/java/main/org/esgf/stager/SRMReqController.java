package org.esgf.stager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;


import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class SRMReqController {

	@RequestMapping(method=RequestMethod.POST, value="/srmreq")
	public @ResponseBody String postSRMRequest(HttpServletRequest request,final HttpServletResponse response) {
	
		System.out.println("In post");
		
		return "POST";
	}
	
	@RequestMapping(method=RequestMethod.GET, value="/srmreq")
	public @ResponseBody String getSRMRequest(HttpServletRequest request,final HttpServletResponse response) {
	
		System.out.println("In get");
		
		return "GET";
	}
}

package org.esgf.singleton2;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.esgf.json.JSONArray;
import org.esgf.json.JSONException;
import org.esgf.json.JSONObject;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

//import dw.spring3.rest.bean.Employee;
//import dw.spring3.rest.bean.EmployeeList;
//import dw.spring3.rest.ds.EmployeeDS;

@Controller
public class EmployeeController {

	private static final String XML_VIEW_NAME = "employees";
	/*
	private EmployeeDS employeeDS;
	
	public void setEmployeeDS(EmployeeDS ds) {
		this.employeeDS = ds;
	}
	
	private Jaxb2Marshaller jaxb2Mashaller;
	
	public void setJaxb2Mashaller(Jaxb2Marshaller jaxb2Mashaller) {
		this.jaxb2Mashaller = jaxb2Mashaller;
	}

	private static final String XML_VIEW_NAME = "employees";
	
	@RequestMapping(method=RequestMethod.GET, value="/employee/{id}")
	public ModelAndView getEmployee(@PathVariable String id) {
		Employee e = employeeDS.get(Long.parseLong(id));
		return new ModelAndView(XML_VIEW_NAME, "object", e);
	}
	
	@RequestMapping(method=RequestMethod.PUT, value="/employee/{id}")
	public ModelAndView updateEmployee(@RequestBody String body) {
		Source source = new StreamSource(new StringReader(body));
		Employee e = (Employee) jaxb2Mashaller.unmarshal(source);
		employeeDS.update(e);
		return new ModelAndView(XML_VIEW_NAME, "object", e);
	}
	
	@RequestMapping(method=RequestMethod.POST, value="/employee")
	public ModelAndView addEmployee(@RequestBody String body) {
		Source source = new StreamSource(new StringReader(body));
		Employee e = (Employee) jaxb2Mashaller.unmarshal(source);
		employeeDS.add(e);
		return new ModelAndView(XML_VIEW_NAME, "object", e);
	}
	
	@RequestMapping(method=RequestMethod.DELETE, value="/employee/{id}")
	public ModelAndView removeEmployee(@PathVariable String id) {
		employeeDS.remove(Long.parseLong(id));
		List<Employee> employees = employeeDS.getAll();
		EmployeeList list = new EmployeeList(employees);
		return new ModelAndView(XML_VIEW_NAME, "employees", list);
	}
	*/
	
	@RequestMapping(method=RequestMethod.POST, value="/employee1")
	public @ResponseBody String addEmployee(@RequestBody String body) {
		/*
		Source source = new StreamSource(new StringReader(body));
		Employee e = (Employee) jaxb2Mashaller.unmarshal(source);
		employeeDS.add(e);
		*/
		System.out.println("Add employee");
		Object e = new Object();
		
		System.out.println(body);
		
		
		try {
			JSONObject jObj = new JSONObject(body);
			Iterator<String> keysIter = jObj.keys();
		    while ( keysIter.hasNext() ){
		    	String key = keysIter.next();
		    	;
		      System.out.println( "key: " + key + " value: " + (jObj.get(key)).toString() );
		      
		      if(key.equals("file_urls")) {
			      JSONArray j = new JSONArray((jObj.get(key)).toString());
			      System.out.println(j.get(0));
			      processPrivateURLs(j);
		    	  //System.out.println(new JSONArray( jObj.get(key) ));
		    	  //
		      }
		    }
			
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		//return new ModelAndView(XML_VIEW_NAME, "object", e);
		return "done";
	}
	
	private void processPrivateURLs(JSONArray jsonArray) {
		
		
		
		//values are a string array
		/*
		String [] file_urls = (String []) values;
		for(int i=0;i<file_urls.length;i++) {
			System.out.println("file_url: " + i + " " + file_urls[i]);
		}
		*/
		System.out.println("process array length " + jsonArray.length());
		for(int i=0;i<jsonArray.length();i++) {
			try {
				System.out.println("json: " + i + " " + (String)jsonArray.get(i));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	@RequestMapping(method=RequestMethod.GET, value="/employees")
	public ModelAndView getEmployees() {
		
		System.out.println("\n\n\n\nIn controller\n\n\n");
		List<String> list = new ArrayList<String>();
		return new ModelAndView(XML_VIEW_NAME, "employees", list);
		/*
		List<Employee> employees = employeeDS.getAll();
		EmployeeList list = new EmployeeList(employees);
		return new ModelAndView(XML_VIEW_NAME, "employees", list);
		*/
	}
	
}

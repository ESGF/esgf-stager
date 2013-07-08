<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

    

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Employees</title>
	<script type="text/javascript" src="../scripts/jquery-1.7.2/jquery.min.js"></script>
	<script type="text/javascript">
	$(function(){
		
		var data = {};
		data['b'] = 'bb';
		
		var arr = new Array();
		arr.push('url1');
		//arr.push('url2');
		
		data['file_urls'] = arr;
		
		
		var jdata = JSON.stringify(data);
		
		var url = 'http://localhost:8080/esgf-stager/service//synchronizedsrmrequestBody'
		$.ajax({
			url: url,
			global: false,
			type: 'POST',
			data: jdata,
			async: false,
			success: function(data) {
				alert('success');
			},
			error: function (xhr, ajaxOptions, thrownError) {
		        alert(xhr.status);
		        //alert(thrownError);
		      }
			
			/*
			async: false,
			dataType: 'json',
			data: queryString,
			success: function(data) {
				//alert('data: ' + data);
				//if(datacart_entry != "failure") {
					datacart_entry = data['datacartdataset'];
				//} else {
					//alert('failure');
				//}
			},
			
			*/
		});
		
		
		
	});
	
	</script>
	

</head>
<body>
<%-- 
<table border=1>
	<thead><tr>
		<th>ID</th>
		<th>Name</th>
		<th>Email</th>
	</tr></thead>
	<c:forEach var="employee" items="${employees.employees}">
	<tr>
		<td>${employee.id}</td>
		<td>${employee.name}</td>
		<td>${employee.email}</td>
	</tr>
	</c:forEach>
</table>
--%>
<div>dfasdfgas</div>
</body>



</html>
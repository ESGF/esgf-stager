<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

    

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Employees</title>
	<script type="text/javascript" src="../scripts/jquery-1.7.2/jquery.min.js"></script>
	<script type="text/javascript">
	$(function(){
		
		var url = 'http://localhost:8080/esgf-stager/service/function'
		$.ajax({
			url: url,
			global: false,
			type: 'POST',
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
<div>employee</div>
</body>



</html>
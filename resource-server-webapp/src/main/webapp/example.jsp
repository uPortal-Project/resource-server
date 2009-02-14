<%@ page contentType="text/html" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="rs" uri="http://www.jasig.org/resource-server" %>

<rs:resourceURL var="jQueryPath" value="/jquery/1.3.1/jquery-1.3.1.js"/>
<script type="text/javascript" language="javascript" src="${jQueryPath}"></script>

<script type="text/javascript">
	$(document).ready(function(){
		$("#test").html("Tag library is working!");
	});
</script>

<div id="test">Tag library is not working</div>
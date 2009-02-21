<%@ page contentType="text/html" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="rs" uri="http://www.jasig.org/resource-server" %>

<html>
    <head>
        <title>Available Resources</title>
        
        <rs:resourceURL var="jQueryPath" value="/jquery/1.3.1/jquery-1.3.1.js"/>
        <script type="text/javascript" language="javascript" src="${jQueryPath}"></script>
        
        <script type="text/javascript">
            $(document).ready(function(){
                $("#test").html("Tag library is working!");
            });
        </script>
        
        <%--
         | TODO add Hide/Show toggle to the jsExample and cssExample pre tags
         +--%>
    </head>
    <body>
        <div id="test">Tag library is not working</div>
        <div>
            <h2>JavaScript Resources</h2>
            <ul>
                <c:forEach var="jsResource" items="${jsResources}" varStatus="forStatus">
                    <li>
                        <div>${jsResource}</div>
<pre id="jsEx_${forStatus.index}" class="jsExample">&lt;rs:resourceURL var="scriptPath" value="${jsResource}"/>
&lt;script type="text/javascript" language="javascript" src="${'${'}scriptPath}">&lt;/script></pre>
                    </li>
                </c:forEach>
            </ul>
        </div>
        
        <div>
            <h2>CSS Resources</h2>
            <ul>
                <c:forEach var="cssResource" items="${cssResources}">
                    <li>
                        <div>${cssResource}</div>
<pre id="jsEx_${forStatus.index}" class="cssExample">&lt;rs:resourceURL var="cssPath" value="${cssResource}"/>
&lt;link rel="stylesheet" type="text/css" href="${'${'}cssPath}" /></pre>
                    </li>
                </c:forEach>
            </ul>
        </div>
    </body>
</html>
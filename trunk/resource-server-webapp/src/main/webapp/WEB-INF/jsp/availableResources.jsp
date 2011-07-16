<%--

    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.

--%>
<%@ page contentType="text/html" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="rs" uri="http://www.jasig.org/resource-server" %>

<html>
    <head>
        <title>Available Resources</title>
        
        <rs:resourceURL var="jQueryPath" value="/rs/jquery/1.3.2/jquery-1.3.2.min.js"/>
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
        
        <h2>Available Resources</h2>
        <ul>
            <c:forEach var="library" items="${libraries}">
                <li>${library.key}
                    <ul>
                        <c:forEach var="version" items="${library.value}">
                            <li>${version.key}
                                <ul>
                                    <c:forEach var="jsResource" items="${version.value['js']}">
                                        <li>
                                            <div>${jsResource.pathWithinContext}</div>
<pre id="jsEx_${forStatus.index}" class="jsExample">&lt;rs:resourceURL var="scriptPath" value="${jsResource.pathWithinContext}"/>
&lt;script type="text/javascript" language="javascript" src="${'${'}scriptPath}">&lt;/script></pre>
                                        </li>
                                    </c:forEach>
                                    
                                    <c:forEach var="cssResource" items="${version.value['css']}">
                                        <li>
                                            <div>${cssResource.pathWithinContext}</div>
<pre class="cssExample">&lt;rs:resourceURL var="cssPath" value="${cssResource.pathWithinContext}"/>
&lt;link rel="stylesheet" type="text/css" href="${'${'}cssPath}" /></pre>
                                        </li>
                                    </c:forEach>
                                </ul>
                            </li>
                        </c:forEach>
                    </ul>
                </li>
            </c:forEach>
        </ul>
    </body>
</html>
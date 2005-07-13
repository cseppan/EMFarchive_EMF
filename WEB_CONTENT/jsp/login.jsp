<html>
<head>
<title>Test Login Token</title>
</head>
<body>

<%
    	String tokenName = (String)session.getAttribute("logintoken");
%>

The value of logintoken is <%=tokenName%>
</body>
</html>
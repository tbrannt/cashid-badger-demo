<html>
	<head>
		<#include "general_head.ftl" />
	</head>
	<body>
		<h1>Welcome ${username?html}, you're now logged in</h1>
		
		<form action="logout" method="post" id="logoutform">
			<input type="submit" value="Logout" />
		</form>
	</body>
<html>
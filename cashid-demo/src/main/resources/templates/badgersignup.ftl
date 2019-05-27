<html>
	<head>
		<#include "general_head.ftl" />
	</head>
	<body>	
		<#if error??><span class="error" style="font-weight: 500; color: red;">${error}</span></#if>
		<form action="badgersignupSubmit" method="post">
			<br>
			<span class="signuphint"><b>Please choose a username to Sign Up</b></span>
			<input type="hidden" name="sc" value="${signupSecret?html}">
		    <input type="text" name="username" placeholder="Username" <#if username??>value="${username?html}"</#if> autofocus>
		   	
		   	<div><input type="checkbox" name="agreeTerms" value="true"> I have read and agree to the <a href="http://www.someterms.com" target="_blank">Terms of Service</a></div>
		    
		   	<br>
		    
			<input type="submit" value="Sign Up" />
		</form>
	</body>
</html>
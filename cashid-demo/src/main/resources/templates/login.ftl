<html>
	<head>
		<#include "general_head.ftl" />
		
		<script>
			cashidDemo.badgerNonce = '${badgerNonce}';
			cashidDemo.host = '${host}';
		</script>
	</head>
	<body>	
		<h1>Please log in</h1>
		
		<button href="#" onclick="cashidDemo.badgerLoginSignup(this);return false;">
			Login via Badger
		</button>
		
		<form action="loginSubmit" method="post" id="loginForm">
			<input type="hidden" name="signedNonce" value="${badgerNonce}" />
			<input style="display:none;" type="submit" value="Login" />
		</form>
	</body>
</html>
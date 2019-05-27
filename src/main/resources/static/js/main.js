(function(cashidDemo, $, undefined) {
	
	$(document).ready(function() {
		
	});
	
	cashidDemo.badgerLoginSignup = function(elem) {
		if(typeof web4bch === 'undefined' || typeof web4bch.bch === 'undefined') {
			window.open('https://badger.bitcoin.com/','_blank');
		} else {
			if(typeof web4bch.bch.defaultAccount === 'undefined') {
				alert('Please unlock your Badger Wallet');
			} else {
				web4bch.bch.sign(web4bch.bch.defaultAccount, 'cashid:' + cashidDemo.host + '/badgerSignatureSubmit?a=login&d=cashidDemo-loginsignup&x=' + cashidDemo.badgerNonce, function(err, res) {
					if(res) {
						var $loginFrm = $('#loginForm');
						
						$loginFrm.attr('action', 'badgerLoginSignupSubmit');
						
						$loginFrm.submit();
					} else {
						$('.error').text('Error. Maybe you\'re trying that too often');
						$('.error').show();
					}
				});
			}
		}
	};
	
}(window.cashidDemo = window.cashidDemo || {}, jQuery));
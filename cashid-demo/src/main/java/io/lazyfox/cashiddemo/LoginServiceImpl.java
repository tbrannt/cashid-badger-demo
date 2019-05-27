package io.lazyfox.cashiddemo;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.SignatureException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.MainNetParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.lazyfox.cashiddemo.bchaddressformatter.BitcoinCashAddressFormatter;
import io.lazyfox.cashiddemo.bchaddressformatter.BitcoinCashAddressType;
import io.lazyfox.cashiddemo.bchaddressformatter.MoneyNetwork;

/**
 * Copyright (c) 2019 FoxTalk Ltd
 * 
 * Distributed under the MIT software license, see the accompanying file LICENSE
 * or http://www.opensource.org/licenses/mit-license.php.
 */
@Service
public class LoginServiceImpl implements LoginService {

	private static final Logger logger = LoggerFactory.getLogger(LoginServiceImpl.class);

	public static final String SIGNATURE_LOGIN_TEXT_TO_SIGN_CORE = "cashidDemo-loginsignup";

	public static final String SIGNATURE_SIGNUP_TEXT_TO_SIGN_CORE = "cashidDemo-signup";

	public static final int SIGNATURE_MAX_LENGTH = 200;

	private static final int BADGER_SIGNATURE_REQUEST_MAX_LENGTH = 3000;

	private static final int MAX_BADGER_RESPONSE_ADDRESS_LENGTH = 100;

	private static final int MAX_BADGER_RESPONSE_REQUEST_LENGTH = 200;

	private static final String BADGER_NONCE_VAL = "badgernonce";

	private static final int BADGER_NONCE_BIT_LENGTH = 160;

	private static final int BADGER_SIGNUP_SECRET_BIT_LENGTH = 160;

	private static final String BADGER_LOGIN_SIGN_RESPONSE_REQUIRED_STRING = "d=" + SIGNATURE_LOGIN_TEXT_TO_SIGN_CORE
			+ "&x=";

	private static final String BADGER_RESPONSE = "BK";

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ExpiringBadgerSignupSecretMap expiringBadgerSignupSecretMap;

	@Autowired
	private ExpiringBadgerNonceMap expiringBadgerNonceMap;

	@Autowired
	private ExpiringSignedNonceToAddressMap expiringSignedNonceToAddressMap;

	private final SecureRandom secureRandom = new SecureRandom();

	private final static ObjectMapper jsonMapper = new ObjectMapper();

	@Override
	public String goToLoginPage(Model model) {
		model.addAttribute("badgerNonce", getBadgerNonce());
		model.addAttribute("host", "localhost:8080");

		return "login";
	}

	@Override
	public String badgerSignatureSubmit(String jsonString, HttpServletRequest request) {
		if (!StringUtils.isEmpty(jsonString) && jsonString.length() <= BADGER_SIGNATURE_REQUEST_MAX_LENGTH) {
			BadgerSignResponse signResponse;
			try {
				signResponse = jsonMapper.readValue(jsonString, BadgerSignResponse.class);

				if (isBadgerWalletLoginSignupSignatureCorrect(signResponse, request)) {
					String signedNonce = getNonceFromBadgerSignResponse(signResponse);

					if (signedNonce != null) {
						expiringSignedNonceToAddressMap.put(signedNonce, signResponse.getAddress());
					}
				}
			} catch (IOException e) {
				logger.error("exception thrown, invalid badger response");
			}
		}

		return BADGER_RESPONSE;
	}

	@Override
	public String badgerLoginSignupSubmit(String signedNonce, HttpServletRequest request, Model model,
			RedirectAttributes redir) {
		String addressSigning = expiringSignedNonceToAddressMap.get(signedNonce);

		if (StringUtils.isEmpty(addressSigning)) {
			logger.info("Failed login attempt no addressSigning found");
			model.addAttribute("error", "");
			return goToLoginPage(model);
		}

		User user = userRepository.findFirstByBchAddress(addressSigning);

		if (user != null) {
			logger.info("doing badgerwallet login for user {}", user);

			doSuccessfulAuthenticatedActions(request, user);

			return "welcome";
		} else {
			redir.addAttribute("sc", getBadgerSignupSecret(addressSigning));

			return "redirect:/badgersignupShow";
		}
	}

	@Override
	public String badgerSignupSubmit(String username, String signupSecret, boolean agreeTerms,
			HttpServletRequest request, RedirectAttributes redir, Model model) {
		String usernameError = getErrorReasonForUsername(username);
		if (usernameError != null) {
			model.addAttribute("error", usernameError);
			model.addAttribute("username", username);
			return badgerSignupShowForm(signupSecret, model);
		}
		User existingUser = userRepository.findFirstByName(username);
		if (existingUser != null) {
			model.addAttribute("error", "username already exists");
			model.addAttribute("username", username);
			return badgerSignupShowForm(signupSecret, model);
		}

		String bchAddressSignedWith = expiringBadgerSignupSecretMap.get(signupSecret);
		if (bchAddressSignedWith == null) {
			redir.addAttribute("error", "signup session expired");

			return "redirect:/signupShowForm";
		}

		if (!agreeTerms) {
			model.addAttribute("error", "you need to agree to the terms");
			model.addAttribute("username", username);
			return badgerSignupShowForm(signupSecret, model);
		}

		User user = new User();
		user.setBchAddress(bchAddressSignedWith);
		user.setName(username);

		expiringBadgerSignupSecretMap.remove(signupSecret);

		userRepository.save(user);

		doSuccessfulAuthenticatedActions(request, user);

		return "redirect:/";
	}

	private String getErrorReasonForUsername(String username) {
		if (StringUtils.length(username) < 3) {
			return "username needs at least 3 chars";
		}
		if (username.length() > 15) {
			return "username can have at most 15 chars";
		}
		return null;
	}

	@Override
	public String badgerSignupShowForm(String signupSecret, Model model) {
		model.addAttribute("signupSecret", StringEscapeUtils.escapeHtml4(signupSecret));

		return "badgersignup";
	}

	@Override
	public String logout(HttpServletRequest request) {
		HttpSession session = request.getSession(false);

		if (session != null) {
			logger.info("User will logout his session");
			session.invalidate();
		}

		return "redirect:/";
	}

	private void doSuccessfulAuthenticatedActions(HttpServletRequest request, User user) {
		HttpSession session = request.getSession(true);

		session.setAttribute("username", user.getName());

		logger.info("User {} just logged in", user);
	}

	private String getBadgerSignupSecret(String signingUpAddress) {
		String signupSecret = new BigInteger(BADGER_SIGNUP_SECRET_BIT_LENGTH, secureRandom).toString(32);
		expiringBadgerSignupSecretMap.put(signupSecret, signingUpAddress);

		return signupSecret;
	}

	private String getBadgerNonce() {
		String badgerNonce = generateBadgerNonce();
		expiringBadgerNonceMap.put(badgerNonce, BADGER_NONCE_VAL);

		return badgerNonce;
	}

	private String generateBadgerNonce() {
		return new BigInteger(BADGER_NONCE_BIT_LENGTH, secureRandom).toString(32);
	}

	private boolean isBadgerWalletLoginSignupSignatureCorrect(BadgerSignResponse signResponse,
			HttpServletRequest request) {
		int indexOf = signResponse.getRequest().indexOf(BADGER_LOGIN_SIGN_RESPONSE_REQUIRED_STRING);
		if (indexOf == -1) {
			return false;
		}

		String receivedNonce = getNonceFromBadgerSignResponse(signResponse);
		String storedNonceVal = receivedNonce == null ? null : expiringBadgerNonceMap.get(receivedNonce);
		if (!BADGER_NONCE_VAL.equals(storedNonceVal)) {
			return false;
		} else {
			expiringBadgerNonceMap.remove(receivedNonce);
		}

		try {
			if (signResponse.getSignature() == null || signResponse.getSignature().length() > SIGNATURE_MAX_LENGTH) {
				return false;
			}

			ECKey pubKey = ECKey.signedMessageToKey(signResponse.getRequest(), signResponse.getSignature());
			Address address = pubKey.toAddress(MainNetParams.get());

			String signingCashAddress = BitcoinCashAddressFormatter.toCashAddress(BitcoinCashAddressType.P2PKH,
					address.getHash160(), MoneyNetwork.MAIN);

			if (!BitcoinCashAddressFormatter.isValidCashAddress(signingCashAddress, MoneyNetwork.MAIN)) {
				return false;
			}
			if (!signingCashAddress.equals(signResponse.getAddress())) {
				return false;
			}

			return true;
		} catch (SignatureException e) {
			return false;
		}
	}

	private String getNonceFromBadgerSignResponse(BadgerSignResponse badgerSignResponse) {
		if (badgerSignResponse.getRequest() == null
				|| badgerSignResponse.getRequest().length() > MAX_BADGER_RESPONSE_REQUEST_LENGTH
				|| badgerSignResponse.getAddress() == null
				|| badgerSignResponse.getAddress().length() > MAX_BADGER_RESPONSE_ADDRESS_LENGTH) {
			return null;
		}

		int indexOf = badgerSignResponse.getRequest().indexOf(BADGER_LOGIN_SIGN_RESPONSE_REQUIRED_STRING);
		if (indexOf == -1) {
			return null;
		} else {
			return badgerSignResponse.getRequest()
					.substring(indexOf + BADGER_LOGIN_SIGN_RESPONSE_REQUIRED_STRING.length());
		}
	}

}

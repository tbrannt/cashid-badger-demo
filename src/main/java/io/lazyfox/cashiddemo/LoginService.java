package io.lazyfox.cashiddemo;

import javax.servlet.http.HttpServletRequest;

import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Copyright (c) 2019 FoxTalk Ltd
 * 
 * Distributed under the MIT software license, see the accompanying file LICENSE
 * or http://www.opensource.org/licenses/mit-license.php.
 */
public interface LoginService {

	public String goToLoginPage(Model model);

	public String badgerSignatureSubmit(String jsonString, HttpServletRequest request);

	public String badgerLoginSignupSubmit(String signedNonce, HttpServletRequest request, Model model,
			RedirectAttributes redir);

	String badgerSignupShowForm(String signupSecret, Model model);

	public String badgerSignupSubmit(String username, String signupSecret, boolean agreeTerms,
			HttpServletRequest request, RedirectAttributes redir, Model model);

	public String logout(HttpServletRequest request);

}

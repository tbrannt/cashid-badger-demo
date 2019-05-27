package io.lazyfox.cashiddemo;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Copyright (c) 2019 FoxTalk Ltd
 * 
 * Distributed under the MIT software license, see the accompanying file LICENSE
 * or http://www.opensource.org/licenses/mit-license.php.
 */
@Controller
public class CashIdDemoController {

	@Autowired
	private LoginService loginService;

	@RequestMapping("/")
	public String getWelcomePage(HttpServletRequest request, Model model) {
		model.addAttribute("username", request.getSession(false).getAttribute("username"));
		return "welcome";
	}

	@RequestMapping("/login")
	public String getLoginPage(Model model) {
		return loginService.goToLoginPage(model);
	}

	@CrossOrigin
	@RequestMapping(value = "/badgerSignatureSubmit", method = RequestMethod.POST)
	@ResponseBody
	public String badgerSignatureSubmit(@RequestBody String jsonString, HttpServletRequest request) {
		return loginService.badgerSignatureSubmit(jsonString, request);
	}

	@RequestMapping(value = "/badgerLoginSignupSubmit", method = RequestMethod.POST)
	public String badgerLoginOrSignupSubmit(@RequestParam(value = "signedNonce", required = true) String signedNonce,
			HttpServletRequest request, Model model, RedirectAttributes redir) {
		return loginService.badgerLoginSignupSubmit(signedNonce, request, model, redir);
	}

	@RequestMapping(value = "/badgersignupShow", method = RequestMethod.GET)
	public String badgerSignupShowForm(@RequestParam(value = "sc", required = true) String signupSecret, Model model) {
		return loginService.badgerSignupShowForm(signupSecret, model);
	}

	@RequestMapping(value = "/badgersignupSubmit", method = RequestMethod.POST)
	public String badgerSignupSubmit(@RequestParam(value = "username", required = true) String username,
			@RequestParam(value = "sc", required = true) String signupSecret,
			@RequestParam(value = "agreeTerms", required = false) boolean agreeTerms, HttpServletRequest request,
			RedirectAttributes redir, Model model) {
		return loginService.badgerSignupSubmit(username, signupSecret, agreeTerms, request, redir, model);
	}

	@RequestMapping(value = "/logout", method = { RequestMethod.POST })
	public String logout(HttpServletRequest request) {
		return loginService.logout(request);
	}

}

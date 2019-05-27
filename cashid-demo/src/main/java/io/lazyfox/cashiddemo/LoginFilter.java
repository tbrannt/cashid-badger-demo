package io.lazyfox.cashiddemo;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Component;

/**
 * Copyright (c) 2019 FoxTalk Ltd
 * 
 * Distributed under the MIT software license, see the accompanying file LICENSE
 * or http://www.opensource.org/licenses/mit-license.php.
 */
@Component
public class LoginFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;

		String requestedPath = httpServletRequest.getRequestURI();

		// static resources allowed for everyone
		if (requestedPath.startsWith("/css/") || requestedPath.startsWith("/img/") || requestedPath.startsWith("/js/")
				|| requestedPath.startsWith("/favicon.ico")) {
			chain.doFilter(httpServletRequest, response);
			return;
		}

		// resources allowed for anonymous users
		if (requestedPath.equals("/login") || requestedPath.equals("/loginSubmit")
				|| requestedPath.equals("/badgerSignatureSubmit") || requestedPath.equals("/badgerLoginSignupSubmit")
				|| requestedPath.equals("/badgersignupShow") || requestedPath.equals("/badgersignupSubmit")) {
			chain.doFilter(httpServletRequest, response);
			return;
		} else {
			HttpSession session = httpServletRequest.getSession(false);

			if (session == null) {
				request.getRequestDispatcher("/login").forward(request, response);
				return;
			} else {
				chain.doFilter(httpServletRequest, response);
				return;
			}
		}
	}

	@Override
	public void destroy() {
	}

}

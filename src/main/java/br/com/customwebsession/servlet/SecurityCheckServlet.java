package br.com.customwebsession.servlet;

import java.io.IOException;
import java.security.MessageDigest;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import br.com.customwebsession.common.Usuario;
import br.com.customwebsession.controller.AuthServletRequest;

public class SecurityCheckServlet extends HttpServlet {

	private static final long serialVersionUID = 1025155574281569651L;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, this.getServletContext());
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.getRequestDispatcher("/login.jsp").forward(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String username = req.getParameter("j_username");
		String password = req.getParameter("j_password");
		String backurl = req.getParameter("backurl");

		Usuario usuario = findByLogin(username);

		if (usuario == null
				|| (!username.equalsIgnoreCase(usuario.getLogin()) || !MD5(password).equals(usuario.getSenha()))) {
			req.setAttribute("error", true);
			req.setAttribute("backurl", backurl);
			req.getRequestDispatcher("/template/jsp/login.jsp").forward(req, resp);
			return;
		}

		req.getSession().setMaxInactiveInterval(1800);
		req.getSession().setAttribute(AuthServletRequest.USER_PRINCIPAL_KEY, username);

		resp.sendRedirect(backurl);
	}

	private Usuario findByLogin(String username) {
		return null;
	}

	public String MD5(String md5) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] array = md.digest(md5.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
			}
			return sb.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}

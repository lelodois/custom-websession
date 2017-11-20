package br.com.customwebsession.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import br.com.customwebsession.common.MyUtils;
import br.com.customwebsession.common.Usuario;

public class AutenticationServlet extends HttpServlet {

	private static final long serialVersionUID = -2950120222918713636L;

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

		try {
			String passMd5 = MyUtils.toMd5(password);
			if (usuario == null
					|| (!username.equalsIgnoreCase(usuario.getLogin()) || !passMd5.equals(usuario.getSenha()))) {
				req.setAttribute("error", true);
				req.setAttribute("backurl", backurl);
				req.getRequestDispatcher("/template/jsp/login.jsp").forward(req, resp);
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException(e);
		}

		req.getSession().setMaxInactiveInterval(1800);
		req.getSession().setAttribute(CustomHttpServletRequest.USER_PRINCIPAL_KEY, username);

		resp.sendRedirect(backurl);
	}

	private Usuario findByLogin(String username) {
		return null;
	}
}

package de.hska.iwii.picturecommunity.backend.listener;

import de.hska.iwii.picturecommunity.backend.entities.User;
import de.hska.iwii.picturecommunity.controller.LoginController;
import org.primefaces.push.PushContext;
import org.primefaces.push.PushContextFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles Logouts.
 */
@Component
public class LogoutHandler extends SimpleUrlLogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {

        Logger log = Logger.getLogger(this.getClass().getName());
        User user = (User)authentication.getPrincipal();
        log.log(Level.INFO, "logout event catched: " + user.getName());

        this.setDefaultTargetUrl("/faces/pages/public/login.xhtml");
		this.setTargetUrlParameter("/faces/pages/public/login.xhtml");

		LoginController.loggedOut(user);

        super.onLogoutSuccess(request, response, authentication);
    }
}

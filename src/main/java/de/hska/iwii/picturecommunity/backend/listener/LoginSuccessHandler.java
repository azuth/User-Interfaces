package de.hska.iwii.picturecommunity.backend.listener;

import de.hska.iwii.picturecommunity.backend.entities.User;
import de.hska.iwii.picturecommunity.controller.LoginController;
import org.primefaces.push.PushContext;
import org.primefaces.push.PushContextFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles UserLogins.
 */
@Component
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        handle(request, response, authentication);

        Logger log = Logger.getLogger(this.getClass().getName());
        User user = (User)authentication.getPrincipal();
        log.log(Level.INFO, "Login event catched: " + user.getName());

        PushContext pushContext = PushContextFactory.getDefault().getPushContext();
        pushContext.push("/login_channel", user.getName());

        this.setDefaultTargetUrl("/faces/pages/public/profile.xhtml");
		this.setTargetUrlParameter("/faces/pages/public/profile.xhtml");
		
        LoginController.loggedIn(user);

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
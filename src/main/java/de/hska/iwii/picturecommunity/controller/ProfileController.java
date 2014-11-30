package de.hska.iwii.picturecommunity.controller;

import de.hska.iwii.picturecommunity.backend.dao.UserDAO;
import de.hska.iwii.picturecommunity.backend.entities.User;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

@Component
@Scope("request")
public class ProfileController {

    @Autowired
    private UserDAO userDAO;

    private User user;

    public User getUser() {
        LoggerFactory.getLogger(this.getClass()).info("getUser: " + (user != null ? user.getName() : "Invalid"));
        return user;
    }

    @PostConstruct
    public String loadUser() {
        String username = ((HttpServletRequest) FacesContext.getCurrentInstance()
                                                            .getExternalContext()
                                                            .getRequest()).getParameterMap().get("user")[0];

        user = userDAO.findUserByName(username);
        LoggerFactory.getLogger(this.getClass()).info("loadUser: " + user);
        return (user != null ? "error" : "");
    }
}

package de.hska.iwii.picturecommunity.controller;

import de.hska.iwii.picturecommunity.backend.dao.UserDAO;
import de.hska.iwii.picturecommunity.backend.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles UserSpecific Tasks.
 */
@Component
@Scope("session")
public class UserController {
    private User user = new User();

    @Autowired
    private LoginController loginController;

    private String friendName;

    public String getFriendName() {
        return friendName;
    }
    public void  setFriendName(String friendName) {
        this.friendName = friendName;
    }

    /**
     * Completion method of the add friend autocompletion
     * @param input user input
     * @return a list with possible users to add.
     */
    public List<String> completionMethod(String input) {
        List<String> retlist = new ArrayList();
        List<User> users = userDAO.findUsersByName(input, null);
        List<User> friends = userDAO.findFriendsOfUser(loginController.getInstance());
        for(User u: users) {
            if (!friends.contains(u) && u != loginController.getInstance()) retlist.add(u.getName());
        }
        return retlist;
    }

    /**
     * Deletes a friend of actual logged in user.
     * @param event
     */
    public void delFriend(ActionEvent event) {
        String userId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("userId");
        User friend = userDAO.findUserByID(Integer.valueOf(userId));
        User user = loginController.getInstance();
        userDAO.updateFriends(user);
        user.delFriend(friend);
        userDAO.updateUser(user);
    }

    /**
     * Add a friend to actual logged in user.
     * @return
     */
    public void addFriend() {
        User user = loginController.getInstance();
        if (user != null) {
            user.setFriendsOf(new HashSet<User>(userDAO.findFriendsOfUser(user)));
            user.addFriend(userDAO.findUserByName(friendName));
            userDAO.updateUser(user);
        }
        friendName = "";
    }


    @Autowired
    private UserDAO userDAO;

    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * getter for actual user data
     * @return user data
     */
    public User getUser() {
        return user;
    }

    /**
     * Action: register
     *
     * registers a new user
     * @return outcome
     */
    public String register() {
        String passwd = user.getPassword();
        user.setRole("User");
        if(!userDAO.createUser(user)) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, this.getClass().getName() + " registration failed.");
            return "/faces/pages/public/register.xhtml";
        }

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user, passwd);
        Authentication authUser = authenticationManager.authenticate(token);
        if (authUser.isAuthenticated()) {
            SecurityContext sc = SecurityContextHolder.getContext();
            sc.setAuthentication(authUser);

            FacesContext fc = FacesContext.getCurrentInstance();
            ExternalContext ec = fc.getExternalContext();
            ((HttpSession) ec.getSession(true)).setAttribute(
                    HttpSessionSecurityContextRepository.
                            SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext());
        }
        return "/faces/pages/private/home.xhtml";
    }

    public Object registerVisitor() {
        return null;
    }

    /**
     * Validate the register form.
     * @param context
     * @param component
     * @param value
     * @throws ValidatorException
     */
    public void validate(FacesContext context, UIComponent component, Object value)
            throws ValidatorException {

        if (component.getId().equals("email")) {
            if (userDAO.findUserByMailaddress((String) value) != null) {
                FacesMessage message = new FacesMessage();
                message.setSeverity(FacesMessage.SEVERITY_ERROR);
                message.setSummary("Email is already taken.");
                message.setDetail("Email is already taken. Please change it and try again.");
                context.addMessage("userForm:email", message);
                throw new ValidatorException(message);
            }
        }
        if (component.getId().equals("name")) {
            if (userDAO.findUserByName((String) value) != null) {
                FacesMessage message = new FacesMessage();
                message.setSeverity(FacesMessage.SEVERITY_ERROR);
                message.setSummary("Username is already taken.");
                message.setDetail("Username is already taken. Please change it and try again.");
                context.addMessage("userForm:name", message);
                throw new ValidatorException(message);
            }
        }
    }

    /**
     * Returns a list of all users.
     * @return all users
     */
    public List<User> getAllUsers() {
        return userDAO.findUsersByName("*", null);
    }
}

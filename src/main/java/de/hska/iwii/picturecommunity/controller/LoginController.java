package de.hska.iwii.picturecommunity.controller;

import de.hska.iwii.picturecommunity.backend.dao.UserDAO;
import de.hska.iwii.picturecommunity.backend.entities.OnlineState;
import de.hska.iwii.picturecommunity.backend.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.security.Timestamp;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Information of actual logged in user.
 */
@Component
@Scope("session")
public class LoginController {

    @Autowired
    private UserDAO userDAO;


    private static Set<User> onlineTracker = new HashSet<>();

    /**
     * Add a user to loggedIn list
     * @param user
     */
    public static void loggedIn(User user) {
        onlineTracker.add(user);
    }

    /**
     * Remove a user from loggedIn list
     * @param user
     */
    public static void loggedOut(User user) {
        onlineTracker.remove(user);
    }


    /**
     * Returns actual login status.
     *
     * @return True, when user is logged in.
     */
    public boolean getLoggedIn() {
        SecurityContext sc = SecurityContextHolder.getContext();
        Authentication auth = sc.getAuthentication();
        if (auth instanceof AnonymousAuthenticationToken) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Returns admin state
     * @return <code>true</code> when logged in user is an admin user.
     *         <code>false</code> when not logged in or just a normal user.
     */
    public boolean getAdminUser() {
        if (!getLoggedIn()) return false;
        return getInstance().getRole().equals(User.ROLE_ADMIN);
    }

    /**
     * Username of actual user.
     *
     * @return the username
     */
    public String getUsername() {
        SecurityContext sc = SecurityContextHolder.getContext();
        Authentication auth = sc.getAuthentication();
        if (auth instanceof AnonymousAuthenticationToken) {
            return "";
        }
        User user = (User) sc.getAuthentication().getPrincipal();
        return user.getUsername();
    }

    /**
     * Returns the role of the current user.
     *
     * @return role
     */
    public String getRole() {
        SecurityContext sc = SecurityContextHolder.getContext();
        Authentication auth = sc.getAuthentication();
        if (auth instanceof AnonymousAuthenticationToken) {
            return "Anonymous";
        }
        User user = (User) sc.getAuthentication().getPrincipal();
        return user.getRole();
    }

    /**
     * Returns the email of the current user.
     *
     * @return email
     */
    public String getEmail() {
        SecurityContext sc = SecurityContextHolder.getContext();
        Authentication auth = sc.getAuthentication();
        if (auth instanceof AnonymousAuthenticationToken) {
            return "";
        }
        User user = (User) sc.getAuthentication().getPrincipal();
        return user.getEmail();
    }

    /**
     * Returns the unique id of the current user.
     *
     * @return id
     */
    public Integer getId() {
        SecurityContext sc = SecurityContextHolder.getContext();
        Authentication auth = sc.getAuthentication();
        if (auth instanceof AnonymousAuthenticationToken) {
            return -1;
        }
        User user = (User) sc.getAuthentication().getPrincipal();
        return user.getId();
    }

    /**
     * Returns the user object of the current user.
     *
     * @return user object
     */
    public User getInstance() {
        SecurityContext sc = SecurityContextHolder.getContext();
        if (sc.getAuthentication() instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return (User) sc.getAuthentication().getPrincipal();
    }

    public List<OnlineState> getFriends() {
        User user = getInstance();
        ArrayList<User> users = new ArrayList<User>(userDAO.findFriendsOfUser(user));
        ArrayList<OnlineState> ret = new ArrayList<>();

        Logger log = Logger.getLogger(this.getClass().getName());
        for (User u: onlineTracker) {
            log.log(Level.INFO, "onlineTracker contains: " + u);
        }



        for (User u: users) {
            log.log(Level.INFO, "friend list add: " + u + " -- " + onlineTracker.contains(u));
            ret.add(new OnlineState(u, onlineTracker.contains(u)));
        }
        return ret;
    }


}

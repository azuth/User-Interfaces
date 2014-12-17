package de.hska.iwii.picturecommunity.controller;

import de.hska.iwii.picturecommunity.backend.dao.UserDAO;
import de.hska.iwii.picturecommunity.backend.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Handles Admin requests.
 */
@Component
@Scope("session")
public class AdminController {

    @Autowired
    private UserDAO userDAO;

    public List<Map.Entry<User, Long>> getTopUploader() {
        return userDAO.getMostActiveUsers(0);
    }

    public void delUser(int uid) {
        userDAO.deleteUser(userDAO.findUserByID(uid));
    }

}

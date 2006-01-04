package gov.epa.emissions.framework.dao;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.InfrastructureException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UserManagerDAO {
    private static Log log = LogFactory.getLog(UserManagerDAO.class);

    DataSource datasource = null;

    private Connection conn;

    private static final String GET_USER_QUERY = "select * from emf.users where user_name=?";

    private static final String GET_USERS_QUERY = "select * from emf.users order by user_name";

    private static final String INSERT_USER_QUERY = "INSERT INTO emf.users (user_name,user_pass,fullname,affiliation,workphone,emailaddr,inadmingrp,acctdisabled) VALUES (?,?,?,?,?,?,?,?)";

    private static final String UPDATE_USER_QUERY = "UPDATE emf.users SET user_name=?,user_pass=?,fullname=?,affiliation=?,workphone=?,emailaddr=?,inadmingrp=?,acctdisabled=? WHERE user_name=?";

    private static final String DELETE_USER_QUERY = "DELETE FROM emf.users where user_name=?";

    public UserManagerDAO(DataSource datasource) throws SQLException {
        this.datasource = datasource;
        conn = datasource.getConnection();
    }

    public boolean contains(String userName) throws InfrastructureException {
        boolean newuser = true;
        try {
            PreparedStatement selectStmt = conn.prepareStatement(GET_USER_QUERY);
            selectStmt.setString(1, userName);

            ResultSet rst = selectStmt.executeQuery();
            if (rst.next())
                newuser = false;

            // Close the result set, statement and the connection
            rst.close();
            selectStmt.close();
        } catch (SQLException ex) {
            log.error(ex);
            throw new InfrastructureException("Database error");
        }

        return newuser;
    }

    public User get(String userName) throws EmfException {
        try {
            PreparedStatement selectStmt = conn.prepareStatement(GET_USER_QUERY);
            selectStmt.setString(1, userName);

            ResultSet rst = selectStmt.executeQuery();

            try {
                if (rst.next())
                    return extractUser(rst);
            } finally {
                rst.close();
                selectStmt.close();
            }
        } catch (SQLException ex) {
            log.error(ex);
            throw new InfrastructureException("Database error");
        }

        log.error("User Data error: No record found for username= " + userName);
        return null;
    }

    private User extractUser(ResultSet rst) throws EmfException {
        User user = new User();
        try {
            user.setAcctDisabled(rst.getBoolean("acctdisabled"));
            user.setAffiliation(rst.getString("affiliation"));
            user.setEmail(rst.getString("emailaddr"));
            user.setFullName(rst.getString("fullname"));
            user.setInAdminGroup(rst.getBoolean("inadmingrp"));
            user.setEncryptedPassword(rst.getString("user_pass"));
            user.setUsername(rst.getString("user_name"));
            user.setPhone(rst.getString("workphone"));
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }

        return user;
    }

    public List all() throws EmfException {
        log.debug("In getUsers");

        ArrayList users = new ArrayList();
        User emfUser = null;

        try {
            PreparedStatement selectStmt = conn.prepareStatement(GET_USERS_QUERY);
            log.debug("Is statement null? " + (selectStmt == null));
            log.debug("The query string " + GET_USERS_QUERY);

            ResultSet rst = selectStmt.executeQuery();
            log.debug("Is result set null? " + (rst == null));

            while (rst.next()) {
                log.debug("An rst record found ");
                emfUser = extractUser(rst);

                users.add(emfUser);
                log.debug(emfUser.getUsername());
            }// while

            // Close the result set, statement and the connection
            rst.close();
            selectStmt.close();
        } catch (SQLException ex) {
            log.error(ex);
            throw new InfrastructureException("Database error");
        }

        log.debug("End of getUsers size is " + users.size());
        return users;
    }

    public void remove(String userName) throws InfrastructureException {
        log.debug("Begin delete user");
        try {
            PreparedStatement deleteStmt = conn.prepareStatement(DELETE_USER_QUERY);
            log.debug("Is statement null? " + (deleteStmt == null));
            log.debug("The query string " + DELETE_USER_QUERY);

            deleteStmt.setString(1, userName);
            deleteStmt.executeUpdate();

            // Close the result set, statement and the connection
            deleteStmt.close();
        } catch (SQLException ex) {
            log.error(ex);
            throw new InfrastructureException("Database error");
        }

        log.debug("End delete user");
    }

    public void update(User user) throws InfrastructureException {
        log.debug("Begin update user");
        try {
            PreparedStatement updateStmt = conn.prepareStatement(UPDATE_USER_QUERY);
            log.debug("Is statement null? " + (updateStmt == null));
            log.debug("The query string " + UPDATE_USER_QUERY);

            updateStmt.setString(1, user.getUsername());
            updateStmt.setString(2, user.getEncryptedPassword());
            updateStmt.setString(3, user.getFullName());
            updateStmt.setString(4, user.getAffiliation());
            updateStmt.setString(5, user.getPhone());
            updateStmt.setString(6, user.getEmail());
            updateStmt.setBoolean(7, user.isInAdminGroup());
            updateStmt.setBoolean(8, user.isAcctDisabled());
            updateStmt.setString(9, user.getUsername());
            updateStmt.executeUpdate();

            // Close the result set, statement and the connection
            updateStmt.close();
        } catch (SQLException ex) {
            log.error(ex);
            throw new InfrastructureException("Database error");
        }

        log.debug("End update user");
    }

    public void add(User user) throws InfrastructureException {
        log.debug("Begin insert user");
        try {
            PreparedStatement insertStmt = conn.prepareStatement(INSERT_USER_QUERY);
            log.debug("Is statement null? " + (insertStmt == null));
            log.debug("The query string " + INSERT_USER_QUERY);

            insertStmt.setString(1, user.getUsername());
            insertStmt.setString(2, user.getEncryptedPassword());
            insertStmt.setString(3, user.getFullName());
            insertStmt.setString(4, user.getAffiliation());
            insertStmt.setString(5, user.getPhone());
            insertStmt.setString(6, user.getEmail());
            insertStmt.setBoolean(7, user.isInAdminGroup());
            insertStmt.setBoolean(8, user.isAcctDisabled());

            insertStmt.executeUpdate();

            // Close the result set, statement and the connection
            insertStmt.close();
        } catch (SQLException ex) {
            log.error(ex);
            throw new InfrastructureException("Database error");
        }

        log.debug("End insert user");
    }
}

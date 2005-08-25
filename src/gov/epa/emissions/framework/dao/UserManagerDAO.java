/*
 * Created on Jul 11, 2005
 *
 * Eclipse Project Name: EMFServer
 * Package: package gov.epa.emissions.framework.dao;
 * File Name: UserManagerDAO.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.dao;

import gov.epa.emissions.framework.InfrastructureException;
import gov.epa.emissions.framework.UserException;
import gov.epa.emissions.framework.services.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Conrad F. D'Cruz
 * 
 */
public class UserManagerDAO {
    private static Log log = LogFactory.getLog(UserManagerDAO.class);

    DataSource ds = null;

    private static final String GET_USER_QUERY = "select * from emf.users where user_name=?";

    private static final String GET_USERS_QUERY = "select * from emf.users order by user_name";

    private static final String INSERT_USER_QUERY = "INSERT INTO emf.users (user_name,user_pass,fullname,affiliation,workphone,emailaddr,inadmingrp,acctdisabled) VALUES (?,?,?,?,?,?,?,?)";

    private static final String UPDATE_USER_QUERY = "UPDATE emf.users SET user_name=?,user_pass=?,fullname=?,affiliation=?,workphone=?,emailaddr=?,inadmingrp=?,acctdisabled=? WHERE user_name=?";

    private static final String DELETE_USER_QUERY = "DELETE FROM emf.users where user_name=?";

    public UserManagerDAO() throws InfrastructureException {
        try {
            Context ctx = new InitialContext();
            log.debug("BEFORE: Is datasource null? " + (ds == null));
            ds = (DataSource) ctx.lookup("java:/comp/env/jdbc/EMFDB");
            log.debug("AFTER: Is datasource null? " + (ds == null));
        } catch (Exception ex) {
            log.error("could not initialize EMF datasource", ex);
            throw new InfrastructureException("Server configuration error");
        }
    }

    public boolean isNewUser(String userName) throws InfrastructureException {
        log.debug("Verify if this is a new user: " + userName);
        boolean newuser = true;
        try {
            if (ds != null) {
                Connection conn = ds.getConnection();
                log.debug("Is connection null? " + (conn == null));

                if (conn != null) {
                    PreparedStatement selectStmt = conn.prepareStatement(GET_USER_QUERY);
                    log.debug("Is statement null? " + (selectStmt == null));
                    log.debug("The query string " + GET_USER_QUERY);

                    selectStmt.setString(1, userName);

                    ResultSet rst = selectStmt.executeQuery();
                    log.debug("Is result set null? " + (rst == null));

                    if (rst.next())
                        newuser = false;

                    // Close the result set, statement and the connection
                    rst.close();
                    selectStmt.close();
                    conn.close();
                }// conn not null
            }// ds not null
        } catch (SQLException ex) {
            log.error(ex);
            throw new InfrastructureException("Database error");
        }
        log.debug("Verify if this is a new user: " + userName);
        return newuser;
    }

    public User getUser(String userName) throws InfrastructureException, UserException {
        log.debug("in getUser for username= " + userName);

        User emfUser = null;

        try {
            if (ds != null) {
                Connection conn = ds.getConnection();
                log.debug("Is connection null? " + (conn == null));

                if (conn != null) {
                    PreparedStatement selectStmt = conn.prepareStatement(GET_USER_QUERY);
                    log.debug("Is statement null? " + (selectStmt == null));
                    log.debug("The query string " + GET_USER_QUERY);

                    selectStmt.setString(1, userName);

                    ResultSet rst = selectStmt.executeQuery();
                    log.debug("Is result set null? " + (rst == null));

                    while (rst.next()) {
                        log.debug("An rst record found ");
                        emfUser = new User();
                        emfUser.setAcctDisabled(rst.getBoolean("acctdisabled"));
                        emfUser.setAffiliation(rst.getString("affiliation"));
                        emfUser.setEmailAddr(rst.getString("emailaddr"));
                        emfUser.setFullName(rst.getString("fullname"));
                        emfUser.setInAdminGroup(rst.getBoolean("inadmingrp"));
                        emfUser.setPassword(rst.getString("user_pass"));
                        emfUser.setUserName(rst.getString("user_name"));
                        emfUser.setWorkPhone(rst.getString("workphone"));
                        emfUser.setDirty(false);
                    }// while

                    // Close the result set, statement and the connection
                    rst.close();
                    selectStmt.close();
                    conn.close();
                }// conn not null
            }// ds not null
        } catch (SQLException ex) {
            log.error(ex);
            throw new InfrastructureException("Database error");
        }

        if (emfUser != null) {
            log.debug("End of getUser with fullname= " + emfUser.getFullName());
        } else {
            log.error("User Data error: No record found for username= " + userName);
        }

        log.debug("in getUser for username= " + userName);
        return emfUser;
    }// getUser

    public void updateUsers(User[] users) throws InfrastructureException {
        log.debug("Start DAO:updateUsers");

        int count = users.length;
        for (int i = 0; i < count; i++) {
            this.updateUser(users[i]);
        }
        log.debug("End transport:updateUsers");

    }// setUsers

    public List getUsers() throws InfrastructureException, UserException {
        log.debug("In getUsers");

        ArrayList users = new ArrayList();
        User emfUser = null;

        try {
            if (ds != null) {
                Connection conn = ds.getConnection();
                log.debug("Is connection null? " + (conn == null));

                if (conn != null) {
                    PreparedStatement selectStmt = conn.prepareStatement(GET_USERS_QUERY);
                    log.debug("Is statement null? " + (selectStmt == null));
                    log.debug("The query string " + GET_USERS_QUERY);

                    ResultSet rst = selectStmt.executeQuery();
                    log.debug("Is result set null? " + (rst == null));

                    while (rst.next()) {
                        log.debug("An rst record found ");
                        emfUser = new User();
                        emfUser.setAcctDisabled(rst.getBoolean("acctdisabled"));
                        emfUser.setAffiliation(rst.getString("affiliation"));
                        emfUser.setEmailAddr(rst.getString("emailaddr"));
                        emfUser.setFullName(rst.getString("fullname"));
                        emfUser.setInAdminGroup(rst.getBoolean("inadmingrp"));
                        emfUser.setPassword(rst.getString("user_pass"));
                        emfUser.setUserName(rst.getString("user_name"));
                        emfUser.setWorkPhone(rst.getString("workphone"));
                        emfUser.setDirty(false);

                        users.add(emfUser);
                        log.debug(emfUser.getUserName());
                    }// while

                    // Close the result set, statement and the connection
                    rst.close();
                    selectStmt.close();
                    conn.close();
                }// conn not null
            }// ds not null
        } catch (SQLException ex) {
            log.error(ex);
            throw new InfrastructureException("Database error");
        }

        log.debug("End of getUsers size is " + users.size());
        return users;
    }

    public void deleteUser(String userName) throws InfrastructureException {
        log.debug("Begin delete user");
        try {
            if (ds != null) {
                Connection conn = ds.getConnection();
                log.debug("Is connection null? " + (conn == null));

                if (conn != null) {
                    PreparedStatement deleteStmt = conn.prepareStatement(DELETE_USER_QUERY);
                    log.debug("Is statement null? " + (deleteStmt == null));
                    log.debug("The query string " + DELETE_USER_QUERY);

                    deleteStmt.setString(1, userName);
                    deleteStmt.executeUpdate();

                    // Close the result set, statement and the connection
                    deleteStmt.close();
                    conn.close();
                }// conn not null
            }// ds not null
        } catch (SQLException ex) {
            log.error(ex);
            throw new InfrastructureException("Database error");
        }

        log.debug("End delete user");
    }

    public void updateUser(User user) throws InfrastructureException {
        log.debug("Begin update user");
        try {
            if (ds != null) {
                Connection conn = ds.getConnection();
                log.debug("Is connection null? " + (conn == null));

                if (conn != null) {
                    PreparedStatement updateStmt = conn.prepareStatement(UPDATE_USER_QUERY);
                    log.debug("Is statement null? " + (updateStmt == null));
                    log.debug("The query string " + UPDATE_USER_QUERY);

                    updateStmt.setString(1, user.getUserName());
                    updateStmt.setString(2, user.getPassword());
                    updateStmt.setString(3, user.getFullName());
                    updateStmt.setString(4, user.getAffiliation());
                    updateStmt.setString(5, user.getWorkPhone());
                    updateStmt.setString(6, user.getEmailAddr());
                    updateStmt.setBoolean(7, user.isInAdminGroup());
                    updateStmt.setBoolean(8, user.isAcctDisabled());
                    updateStmt.setString(9, user.getUserName());
                    updateStmt.executeUpdate();

                    // Close the result set, statement and the connection
                    updateStmt.close();
                    conn.close();
                }// conn not null
            }// ds not null
        } catch (SQLException ex) {
            log.error(ex);
            throw new InfrastructureException("Database error");
        }

        log.debug("End update user");
    }

    public void insertUser(User user) throws InfrastructureException {
        log.debug("Begin insert user");
        try {
            if (ds != null) {
                Connection conn = ds.getConnection();
                log.debug("Is connection null? " + (conn == null));

                if (conn != null) {
                    PreparedStatement insertStmt = conn.prepareStatement(INSERT_USER_QUERY);
                    log.debug("Is statement null? " + (insertStmt == null));
                    log.debug("The query string " + INSERT_USER_QUERY);

                    insertStmt.setString(1, user.getUserName());
                    insertStmt.setString(2, user.getPassword());
                    insertStmt.setString(3, user.getFullName());
                    insertStmt.setString(4, user.getAffiliation());
                    insertStmt.setString(5, user.getWorkPhone());
                    insertStmt.setString(6, user.getEmailAddr());
                    insertStmt.setBoolean(7, user.isInAdminGroup());
                    insertStmt.setBoolean(8, user.isAcctDisabled());

                    insertStmt.executeUpdate();

                    // Close the result set, statement and the connection
                    insertStmt.close();
                    conn.close();
                }// conn not null
            }// ds not null
        } catch (SQLException ex) {
            log.error(ex);
            throw new InfrastructureException("Database error");
        }

        log.debug("End insert user");
    }
}

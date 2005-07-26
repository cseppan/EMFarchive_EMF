/*
 * Created on Jul 11, 2005
 *
 * Eclipse Project Name: EMFServer
 * Package: package gov.epa.emissions.framework.dao;
 * File Name: UserManagerDAO.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import gov.epa.emissions.framework.commons.User;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

/**
 * @author Conrad F. D'Cruz
 *
 */
public class UserManagerDAO {

    DataSource ds = null;
    private static final String GET_USER_QUERY="select * from users where user_name=? order by user_name";
    private static final String GET_USERS_QUERY="select * from users order by user_name";
    private static final String INSERT_USER_QUERY="INSERT INTO users (user_name,user_pass,fullname,affiliation,workphone,emailaddr,inadmingrp,acctdisabled) VALUES (?,?,?,?,?,?,?,?)";
    private static final String UPDATE_USER_QUERY="UPDATE users SET user_name=?,user_pass=?,fullname=?,affiliation=?,workphone=?,emailaddr=?,inadmingrp=?,acctdisabled=? WHERE user_name=?";
    private static final String DELETE_USER_QUERY="DELETE FROM users where user_name=?";
    
    /**
     * @throws Exception
     * 
     */
    public UserManagerDAO() throws Exception {
        super();
        try{
            Context ctx = new InitialContext();
            if(ctx == null ) 
                throw new Exception("No Context");
            System.out.println("BEFORE: Is datasource null? " + (ds ==null));
            ds = (DataSource)ctx.lookup("java:/comp/env/jdbc/EMFDB");
            System.out.println("AFTER: Is datasource null? " + (ds ==null));
        }catch(Exception ex) {
            ex.printStackTrace();
            throw new Exception(ex);
        }   
    }//constructor
    
    
    public User getUser(String userName) throws Exception{
      System.out.println("in getUser for username= " + userName);
  
      User emfUser = new User();

      try{
      if (ds != null) {
          Connection conn = ds.getConnection();
          System.out.println("Is connection null? " + (conn ==null));

          if(conn != null)  {
              PreparedStatement selectStmt = conn.prepareStatement(GET_USER_QUERY);
              System.out.println("Is statement null? " + (selectStmt ==null));
              System.out.println("The query string " + GET_USER_QUERY);

              selectStmt.setString(1,userName);                  
              
              ResultSet rst = selectStmt.executeQuery();
              System.out.println("Is result set null? " + (rst ==null));
              
                  while( rst.next() ){
                      System.out.println("An rst record found ");
                      emfUser.setAcctDisabled(rst.getBoolean("acctdisabled"));
                      emfUser.setAffiliation(rst.getString("affiliation"));
                      emfUser.setEmailAddr(rst.getString("emailaddr"));
                      emfUser.setFullName(rst.getString("fullname"));
                      emfUser.setInAdminGroup(rst.getBoolean("inadmingrp"));
                      emfUser.setPassword(rst.getString("user_pass"));
                      emfUser.setUserName(rst.getString("user_name"));
                      emfUser.setWorkPhone(rst.getString("workphone"));                      
                      emfUser.setDirty(false);
                    }//while                  
              
              // Close the result set, statement and the connection
              rst.close() ;
              selectStmt.close() ;
              conn.close() ;
          }//conn not null
      }//ds not null
      }catch(SQLException ex){
          ex.printStackTrace();
          throw new Exception(ex);

      }

      System.out.println("End of getUser with fullname= " + emfUser.getFullName());

      return emfUser;
    }//getUser

    public void updateUsers(User[] users) throws Exception{
        System.out.println("Start DAO:updateUsers");

        int count = users.length;
        for (int i=0; i<count; i++){
            this.updateUser(users[i]);
        }        
        System.out.println("End transport:updateUsers");

    }//setUsers
    
    public List getUsers() throws Exception{
      System.out.println("In getUsers");
      
      ArrayList users = new ArrayList();
      User emfUser = null;
      
      try{
          if (ds != null) {
              Connection conn = ds.getConnection();
              System.out.println("Is connection null? " + (conn ==null));

              if(conn != null)  {
                  PreparedStatement selectStmt = conn.prepareStatement(GET_USERS_QUERY);
                  System.out.println("Is statement null? " + (selectStmt ==null));
                  System.out.println("The query string " + GET_USERS_QUERY);
                  
                  ResultSet rst = selectStmt.executeQuery();
                  System.out.println("Is result set null? " + (rst ==null));
                  
                      while( rst.next() ){
                          System.out.println("An rst record found ");
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
                          System.out.println(emfUser.getUserName());
                        }//while                  
                  
                  // Close the result set, statement and the connection
                  rst.close() ;
                  selectStmt.close() ;
                  conn.close() ;
              }//conn not null
          }//ds not null
          }catch(SQLException ex){
              ex.printStackTrace();
              throw new Exception(ex);
          }
      
          System.out.println("End of getUsers size is " + users.size());
      return users;
    }
    
    public void deleteUser(String userName) throws Exception{
      System.out.println("Begin delete user");    
      try{
          if (ds != null) {
              Connection conn = ds.getConnection();
              System.out.println("Is connection null? " + (conn ==null));

              if(conn != null)  {
                  PreparedStatement deleteStmt = conn.prepareStatement(DELETE_USER_QUERY);
                  System.out.println("Is statement null? " + (deleteStmt ==null));
                  System.out.println("The query string " + DELETE_USER_QUERY);

                  deleteStmt.setString(1,userName);                  
                  deleteStmt.executeUpdate();
                                   
                  // Close the result set, statement and the connection
                  deleteStmt.close() ;
                  conn.close() ;
              }//conn not null
          }//ds not null
          }catch(SQLException ex){
              ex.printStackTrace();
              throw new Exception(ex);
          }
        
      System.out.println("End delete user");    
    }

    public void updateUser (User user) throws Exception{
        System.out.println("Begin update user");
        try{
            if (ds != null) {
                Connection conn = ds.getConnection();
                System.out.println("Is connection null? " + (conn ==null));

                if(conn != null)  {
                    PreparedStatement updateStmt = conn.prepareStatement(UPDATE_USER_QUERY);
                    System.out.println("Is statement null? " + (updateStmt ==null));
                    System.out.println("The query string " + UPDATE_USER_QUERY);

                    updateStmt.setString(1,user.getUserName());
                    updateStmt.setString(2,user.getPassword());
                    updateStmt.setString(3,user.getFullName());
                    updateStmt.setString(4,user.getAffiliation());
                    updateStmt.setString(5,user.getWorkPhone());
                    updateStmt.setString(6,user.getEmailAddr());
                    updateStmt.setBoolean(7,user.isInAdminGroup());
                    updateStmt.setBoolean(8,user.isAcctDisabled());
                    updateStmt.setString(9,user.getUserName());
                    updateStmt.executeUpdate();
                    
                    
                    // Close the result set, statement and the connection
                    updateStmt.close() ;
                    conn.close() ;
                }//conn not null
            }//ds not null
            }catch(SQLException ex){
                ex.printStackTrace();
                throw new Exception(ex);
            }

    System.out.println("End update user");
}
    
    public void insertUser(User user) throws Exception{
        System.out.println("Begin insert user");
            try{
                if (ds != null) {
                    Connection conn = ds.getConnection();
                    System.out.println("Is connection null? " + (conn ==null));

                    if(conn != null)  {
                        PreparedStatement insertStmt = conn.prepareStatement(INSERT_USER_QUERY);
                        System.out.println("Is statement null? " + (insertStmt ==null));
                        System.out.println("The query string " + INSERT_USER_QUERY);

                        insertStmt.setString(1,user.getUserName());
                        insertStmt.setString(2,user.getPassword());
                        insertStmt.setString(3,user.getFullName());
                        insertStmt.setString(4,user.getAffiliation());
                        insertStmt.setString(5,user.getWorkPhone());
                        insertStmt.setString(6,user.getEmailAddr());
                        insertStmt.setBoolean(7,user.isInAdminGroup());
                        insertStmt.setBoolean(8,user.isAcctDisabled());
                        
                        insertStmt.executeUpdate();
                        
                        
                        // Close the result set, statement and the connection
                        insertStmt.close() ;
                        conn.close() ;
                    }//conn not null
                }//ds not null
                }catch(SQLException ex){
                    ex.printStackTrace();
                    throw new Exception(ex);
                }

        System.out.println("End insert user");
    }
}

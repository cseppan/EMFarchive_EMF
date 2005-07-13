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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
    private static final String GET_USER_QUERY="select * from users where user_name='";
    
    /**
     * 
     */
    public UserManagerDAO() {
        super();
        try{
            Context ctx = new InitialContext();
            if(ctx == null ) 
                throw new Exception("No Context");
            System.out.println("BEFORE: Is datasource null? " + (ds ==null));
            ds = (DataSource)ctx.lookup("java:/comp/env/jdbc/EMFDB");
            System.out.println("AFTER: Is datasource null? " + (ds ==null));
        }catch(Exception e) {
            e.printStackTrace();
        }   
    }//constructor
    
    
    public User getUser(String userName){
      System.out.println("in getUser for username= " + userName);
  
      User emfUser = new User();

      try{
      if (ds != null) {
          Connection conn = ds.getConnection();
          System.out.println("Is connection null? " + (conn ==null));

          if(conn != null)  {
              Statement stmt = conn.createStatement();
              System.out.println("Is statement null? " + (stmt ==null));
              System.out.println("The query string " + GET_USER_QUERY + userName + "'");

              ResultSet rst = stmt.executeQuery(GET_USER_QUERY + userName + "'");
              System.out.println("Is result set null? " + (rst ==null));
              
              // Loop through the result set
//              if (rst.next()){
//                  System.out.println("RST has records? " + (rst.next()));
//                  emfUser = new User();
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
//              }//rst.next
              
              // Close the result set, statement and the connection
              rst.close() ;
              stmt.close() ;
              conn.close() ;
          }//conn not null
      }//ds not null
      }catch(SQLException ex){
          
      }

      System.out.println("End of getUser with fullname= " + emfUser.getFullName());

      return emfUser;
    }//getUser

 
        
        
}

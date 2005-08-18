/*
 * Created on Jul 29, 2005
 *
 * Eclipse Project Name: EMF
 * Package: package gov.epa.emissions.framework.dao;
 * File Name: StatusDAO.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.dao;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.Status;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * @author Conrad F. D'Cruz
 *
 */
public class StatusDAO {

    private static final String GET_STATUS_QUERY="select stat from Status as stat where stat.userName=:username";
    private static final String GET_READ_STATUS_QUERY="select stat from Status as stat where stat.msgRead=true and stat.userName=:username";

//  private static final String INSERT_USER_QUERY="INSERT INTO users (user_name,user_pass,fullname,affiliation,workphone,emailaddr,inadmingrp,acctdisabled) VALUES (?,?,?,?,?,?,?,?)";
//    private static final String DELETE_USER_QUERY="DELETE FROM users where user_name=?";
//  private static final String UPDATE_USER_QUERY="UPDATE users SET user_name=?,user_pass=?,fullname=?,affiliation=?,workphone=?,emailaddr=?,inadmingrp=?,acctdisabled=? WHERE user_name=?";

    public static List getMessages(String userName, Session session) throws EmfException{
        System.out.println("In getMessages");
        deleteMessages(userName, session);
        ArrayList allStatus = new ArrayList();
        
        Transaction tx = session.beginTransaction();
        
        Query query = session.createQuery(GET_STATUS_QUERY);
        query.setParameter("username", userName, Hibernate.STRING);

        Iterator iter = query.iterate();
        while (iter.hasNext()){
            Status aStatus = (Status)iter.next();
            aStatus.setMsgRead();
            allStatus.add(aStatus);  
        }
        
        tx.commit();
        System.out.println("End getMessages");
        return allStatus;
    }//getMessages(uname)
    
    public static void insertStatusMessage(Status status, Session session){
        System.out.println("StatusDAO: insertStatusMessage: " + status.getUserName()+ "\n" + session.toString());
        Transaction tx = session.beginTransaction();
        System.out.println("StatusDAO: insertStatusMessage before session.save");
        session.save(status);
        session.flush();
        System.out.println("StatusDAO: insertStatusMessage after session.save");
        tx.commit();
    }
    
    private static void deleteMessages(String userName, Session session){
        System.out.println("In deleteMessages");
              
        Query query = session.createQuery(GET_READ_STATUS_QUERY);
        query.setParameter("username", userName, Hibernate.STRING);

        Iterator iter = query.iterate();
        while (iter.hasNext()){
            Status aStatus = (Status)iter.next();
            session.delete(aStatus);
        }
        session.flush();
        System.out.println("End deleteMessages");
        
    }//deleteMessages

}//StatusDAO

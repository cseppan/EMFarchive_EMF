/*
 * Created on Aug 10, 2005
 *
 * Eclipse Project Name: EMF
 * Package: package gov.epa.emissions.framework.dao;
 * File Name: DataSourceFactory.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.dao;

import gov.epa.emissions.framework.InfrastructureException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * @author Conrad F. D'Cruz
 *
 */
public class DataSourceFactory {

    public static DataSource getDataSource() throws InfrastructureException{
        DataSource ds = null;
        
        try{
            Context ctx = new InitialContext();
            if(ctx == null ) 
                throw new Exception("No Context");
            System.out.println("BEFORE: Is datasource null? " + (ds ==null));
            ds = (DataSource)ctx.lookup("java:/comp/env/jdbc/EMFDB");
            System.out.println("AFTER: Is datasource null? " + (ds ==null));
        }catch (NamingException ex){
            ex.printStackTrace();
            throw new InfrastructureException("Server configuration error");
        }catch(Exception ex) {
            ex.printStackTrace();
            throw new InfrastructureException("Server configuration error");
        }
        return ds;  
    }

}

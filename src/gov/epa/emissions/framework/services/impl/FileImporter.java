/*
 * Created on Aug 10, 2005
 *
 * Eclipse Project Name: EMF
 * Package: package gov.epa.emissions.framework.commons;
 * File Name: DummyImporter.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.InfrastructureException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.StringTokenizer;

import javax.sql.DataSource;

	
/**
 * @author Conrad F. D'Cruz
 *
 */
public class FileImporter {
	private DataSource ds = null;
	private File file = null;
    private static final String INSERT_RECORD_QUERY="INSERT INTO actypes (carriername,aircrafttype,count) VALUES (?,?,?)";
	
    private static String carrierName=null;
    private String[] acTypes=null;
    private int[] acCnt=null;
    
    /**
     * @param ds
     * @param file
     */
    public FileImporter(DataSource ds, File file) {
        super();
        this.ds = ds;
        this.file = file;
    }
    /**
     * 
     */
    public FileImporter() {
        super();
    }

  
    public void run() throws EmfException{

        readFile();
        writeDataToDb();
        
    }//run
    
    /**
     * @throws InfrastructureException
     * 
     */
    private void writeDataToDb() throws InfrastructureException {
        for (int i=0; i<acCnt.length;i++){
            insertRecord(carrierName, acTypes[i], acCnt[i]);
        }
    }
    /**
     * @throws EmfException
     * 
     */
    private void readFile() throws EmfException {

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = null;
            
            int linNum=0;
            while ((line = reader.readLine()) != null)
            {
                linNum++;
                if (line.indexOf(",")>0){
                    System.out.println(linNum + " Data line has " + getNumber(line) + " elements");
                    if (linNum == 2) readNames(line);
                    if (linNum ==3) readCount(line);
                }else{
                    carrierName=line.trim();
                    System.out.println("Carrier Name: " + carrierName);
                }
            }// while file is not empty

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new EmfException("File was not found: " + file.getName());
        } catch (IOException e) {
            e.printStackTrace();
            throw new EmfException("Error reading file: " + file.getName());
        }// while file is not empty
        
    }
    
    private void insertRecord(String carrierName, String acType, int number) throws InfrastructureException{
        try{
            if (ds != null) {
                Connection conn = ds.getConnection();
                System.out.println("Is connection null? " + (conn ==null));

                if(conn != null)  {
                    PreparedStatement insertStmt = conn.prepareStatement(INSERT_RECORD_QUERY);
                    System.out.println("Is statement null? " + (insertStmt ==null));

                    insertStmt.setString(1,carrierName);
                    insertStmt.setString(2,acType);
                    insertStmt.setInt(3,number);
                    
                    insertStmt.executeUpdate();
                    
                    
                    // Close the result set, statement and the connection
                    insertStmt.close() ;
                    conn.close() ;
                }//conn not null
            }//ds not null
            }catch(SQLException ex){
                ex.printStackTrace();
                throw new InfrastructureException("Database error");
            }


    }
    
    /**
     * @param line
     */
    private void readCount(String line) {
        StringTokenizer strTkn = new StringTokenizer(line, ",", false);
        int cnt = 0;
        acCnt = new int[strTkn.countTokens()];
        
        while (strTkn.hasMoreTokens()){
            acCnt[cnt]=Integer.parseInt(strTkn.nextToken());
            cnt++;
        }
        
    }

    /**
     * @param line
     */
    private void readNames(String line) {

        StringTokenizer strTkn = new StringTokenizer(line, ",", false);
        int cnt = 0;
        acTypes = new String[strTkn.countTokens()];
        
        while (strTkn.hasMoreTokens()){
            acTypes[cnt]=strTkn.nextToken();
            cnt++;
        }
    }

    /**
     * @param line
     * @return
     */
    private int getNumber(String line) {
        StringTokenizer strTkn = new StringTokenizer(line, ",", false);
        
        return strTkn.countTokens();
    }
}//DummyImporter

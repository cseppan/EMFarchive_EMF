/*
 * Creation on Nov 16, 2005
 * Eclipse Project Name: EMF
 * File Name: SimpleDataVersioningDAOTest.java
 * Author: Conrad F. D'Cruz
 */
/**
 * 
 */

package gov.epa.emissions.framework.version;

import junit.framework.TestCase;

/**
 * @author Conrad F. D'Cruz
 *
 */
public class SimpleDataVersioningDAOTest extends TestCase {
    SimpleVersioningDAO sdvDAO = null;

    protected void setUp() throws Exception {
        super.setUp();
        sdvDAO = new SimpleVersioningDAO();
    }

    protected void tearDown() throws Exception {
        sdvDAO.closeConnection();
        sdvDAO = null;
        super.tearDown();
    }

    public void testGetRecordsForVersionZero() throws Exception {
        int versNum = 0;       
        DTVTRecord[] allRecsForVers = sdvDAO.getRecordsByVersionNumber(versNum);
        assertTrue(allRecsForVers.length>0);
    }
    
    public void testInsertsTwoNewRecordInVersionZero() throws Exception{
        int versNum = 0;       
        DTVTRecord[] allRecsForVersZero = sdvDAO.getRecordsByVersionNumber(versNum);
        int numberOfOriginalRecords = allRecsForVersZero.length;
        
        int lastVersionNumber = sdvDAO.getLastVersionNumber();
        
        DTVTRecord[] newRecs = new DTVTRecord[2];
        newRecs[0] = new DTVTRecord(1,"new rec #1","",-99);
        newRecs[1] = new DTVTRecord(1,"new rec #2","",-99);
        
        sdvDAO.insertNewRecords(newRecs,versNum);
        DTVTRecord[] newRecsForVers = sdvDAO.getRecordsByVersionNumber(lastVersionNumber+1);
        
        assertTrue(newRecsForVers.length==(numberOfOriginalRecords+2));      
    }

    public void testDeletesOneRecordFromVersionZero() throws Exception {
        int versNum = 0;       
        DTVTRecord[] allRecsForVersZero = sdvDAO.getRecordsByVersionNumber(versNum);
        int numberOfOriginalRecords = allRecsForVersZero.length;        
        int recordIdToDelete = allRecsForVersZero[0].getRecordId();
        int lastVersionNumber = sdvDAO.getLastVersionNumber();
        
        sdvDAO.deleteRecordFromVersion(recordIdToDelete,versNum);
        DTVTRecord[] newRecsForVers = sdvDAO.getRecordsByVersionNumber(lastVersionNumber+1);
        
        assertTrue(newRecsForVers.length==(numberOfOriginalRecords-1));      
        
    }

    public void testUpdatesTwoRecordsInVersionZero() throws Exception {
        int versNum = 0;       
        DTVTRecord[] allRecsForVersZero = sdvDAO.getRecordsByVersionNumber(versNum);
        int numberOfOriginalRecords = allRecsForVersZero.length;        

        DTVTRecord[] modifiedRecords = new DTVTRecord[2];
        modifiedRecords[0]=allRecsForVersZero[0];
        modifiedRecords[1]=allRecsForVersZero[1];
        
        //Update the first two records from Version Zero
        modifiedRecords[0].setDescription("Modified record 0 desc");
        modifiedRecords[1].setDescription("Modified record 1 desc");
        int lastVersionNumber = sdvDAO.getLastVersionNumber();
        
        sdvDAO.updateRecord(modifiedRecords,versNum);
        DTVTRecord[] newRecsForVers = sdvDAO.getRecordsByVersionNumber(lastVersionNumber+1);
        assertTrue(newRecsForVers[0].getDescription().equals("Modified record 0 desc"));
        assertTrue(newRecsForVers[1].getDescription().equals("Modified record 1 desc"));
    }

}

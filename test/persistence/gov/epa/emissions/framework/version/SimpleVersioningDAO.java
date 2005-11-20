 /*
 * Creation on Nov 16, 2005
 * Eclipse Project Name: EMF
 * File Name: SimpleVersioningDAO.java
 * Author: Conrad F. D'Cruz
 */
/**
 * 
 */

package gov.epa.emissions.framework.version;

import gov.epa.emissions.commons.db.DatabaseSetup;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.framework.InfrastructureException;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Conrad F. D'Cruz
 *
 */
public class SimpleVersioningDAO {
    private static Log log = LogFactory.getLog(SimpleVersioningDAO.class);
    private String INSERT_DT_QUERY = "INSERT INTO emissions.data_table (record_id,dataset_id,description) VALUES (default,?,?)";
    private String SELECT_DATA_TABLE_SEQUENCE_NUMBER = "SELECT currval('emissions.data_table_record_id_seq')";
    private String INSERT_VT_QUERY = "INSERT INTO emissions.version_table (record_id,version_number,version_name) VALUES (?,?,?)";
    private String SELECT_VERSION_NUMBER = "select version_number from emissions.version_table order by version_number";
    private String SELECT_RECORDS_FOR_VERSION="select emissions.data_table.record_id,version_name, version_number,dataset_id,description from emissions.data_table, emissions.version_table where emissions.data_table.record_id=emissions.version_table.record_id and version_number=?";
    Connection connection = null;

    private PreparedStatement insertDTStmt = null;
    private PreparedStatement insertVTStmt = null;
    private PreparedStatement seqPSStmt = null;
    private PreparedStatement selectVersionStmt = null;
    private PreparedStatement selectRecsStmt = null;

    private Datasource datasource = null;
    private DatabaseSetup dbSetup= null;
    private File fieldDefsFile= null;
    private File referenceFilesDir= null;
    private SqlDataTypes sqlDataTypes= null;
    private Datasource emissionsSchema;

    private DTVTRecord[] allRecs = null;
    
    /**
     * @throws InfrastructureException 
     * 
     */
    public SimpleVersioningDAO() throws Exception {
        super();
        setup();
        int lastVersNum = getLastVersionNumber();
        System.out.println("lastversionnumber" + lastVersNum);
        allRecs = getRecordsByVersionNumber(lastVersNum);
        DTVTRecord rec = new DTVTRecord(1,"new rec","",-99);
        //insertNewRecord(rec, lastVersNum);
        lastVersNum = getLastVersionNumber();
        System.out.println("lastversionnumber" + lastVersNum);
        allRecs = getRecordsByVersionNumber(lastVersNum);
//        printRecs(allRecs);
//        deleteRecordFromVersion(5,lastVersNum);
        updateRecord(221);
        
        closeConnection();
        
    }
    
    private void updateRecord(int updateRecordId) throws Exception {
        int lastVersNum = getLastVersionNumber();
        DTVTRecord[] versRecs = getRecordsByVersionNumber(lastVersNum);
        DTVTRecord modRec = null;
        int newVersNum=lastVersNum+1;
        String versionName = "V_" + newVersNum;
        
        for (int k=0; k<versRecs.length;k++){
            if (versRecs[k].getRecordId()==updateRecordId){
                versRecs[k].setDescription("modifiedrecord original recid= " + updateRecordId);
                modRec = versRecs[k];                

                //insert the updated record into the data table
                insertDTStmt.setInt(1, modRec.getDatasetId());
                insertDTStmt.setString(2, modRec.getDescription());
                insertDTStmt.executeUpdate();

                ResultSet rs = seqPSStmt.executeQuery();
                int recordId=-99;
                
                while (rs.next()){
                    recordId=rs.getInt(1);
                }
                rs.close();
                versRecs[k].setRecordId(recordId);
            }            
            //insert all into the version table
            insertVTStmt.setInt(1, versRecs[k].getRecordId());
            insertVTStmt.setInt(2, newVersNum);
            insertVTStmt.setString(3, versionName);
            insertVTStmt.executeUpdate();

        }
        
    }

    private void printRecs(DTVTRecord[] allRecs2) {

        System.out.println(allRecs.length);
    }

    private int getLastVersionNumber() throws SQLException {
        int lastVersionNumber = -99;
        
        ResultSet rs = selectVersionStmt.executeQuery();
        
        while (rs.next()){
            int recVersNum=rs.getInt("version_number");
            
            if (recVersNum > lastVersionNumber) lastVersionNumber = recVersNum;
            
        }

        rs.close();
        return lastVersionNumber;
    }

    private void deleteRecordFromVersion(int recId, int versNum) throws Exception {
        DTVTRecord[] versRecs = getRecordsByVersionNumber(versNum);
        DTVTRecord[] temp = new DTVTRecord[versRecs.length-1];
        int k=0;
        for (int i = 0; i<versRecs.length;i++){
            if (versRecs[i].getRecordId()!= recId){
                temp[k++]=versRecs[i];
            }
        }
        
        int newVersNum=versNum+1;
        String versionName = "V_" + newVersNum;

        //Insert the versions of all the other records from the previous version with the new version id
        for (int i=0; i< temp.length; i++){
            insertVTStmt.setInt(1, temp[i].getRecordId());
            insertVTStmt.setInt(2, newVersNum);
            insertVTStmt.setString(3, versionName);
            insertVTStmt.executeUpdate();
            
        }
       
    }

    private void closeConnection() throws Exception {
        selectVersionStmt.close();
        insertDTStmt.close();
        insertVTStmt.close();
        selectRecsStmt.close();

        connection.close();
    }

    private void insertNewRecord(DTVTRecord newRec, int lastVersionNumber) throws Exception {
        
        
        int newVersNum=lastVersionNumber+1;
        String versionName = "V_" + newVersNum;

        insertDTStmt.setInt(1, newRec.getDatasetId());
        insertDTStmt.setString(2, newRec.getDescription());
        insertDTStmt.executeUpdate();

        ResultSet rs = seqPSStmt.executeQuery();
        int recordId=-99;
        
        while (rs.next()){
            recordId=rs.getInt(1);
        }

        insertVTStmt.setInt(1, recordId);
        insertVTStmt.setInt(2, newVersNum);
        insertVTStmt.setString(3, versionName);
        insertVTStmt.executeUpdate();

        //Insert the versions of all the other records from the previous version with the new version id
        for (int i=0; i< allRecs.length; i++){
            insertVTStmt.setInt(1, allRecs[i].getRecordId());
            insertVTStmt.setInt(2, newVersNum);
            insertVTStmt.setString(3, versionName);
            insertVTStmt.executeUpdate();
            
        }
        
        
        // Close the result set, statement and the connection
        rs.close();

    }

    private void setup() throws Exception {
        
        try {
            String folder = "test";
            File conf = new File(folder, "test.conf");

            if (!conf.exists() || !conf.isFile()) {
                String error = "File: " + conf + " does not exist. Please copy either of the two TEMPLATE files "
                        + "(from " + folder + "), name it test.conf, configure " + "it as needed, and rerun.";
                throw new RuntimeException(error);
            }

            Properties properties = new Properties();
            properties.load(new FileInputStream(conf));

            dbSetup = new DatabaseSetup(properties);

            emissionsSchema = dbSetup.getDbServer().getEmissionsDatasource();
            connection = emissionsSchema.getConnection();
            insertDTStmt = connection.prepareStatement(INSERT_DT_QUERY);
            insertVTStmt = connection.prepareStatement(INSERT_VT_QUERY);
            seqPSStmt = connection.prepareStatement(SELECT_DATA_TABLE_SEQUENCE_NUMBER);
            selectVersionStmt = connection.prepareStatement(SELECT_VERSION_NUMBER);
            selectRecsStmt = connection.prepareStatement(SELECT_RECORDS_FOR_VERSION);
        } catch (Exception ex) {
            log.error("could not initialize EMF datasource", ex);
            throw new InfrastructureException("Server configuration error");
        }
    }

    public DTVTRecord[] getRecordsByVersionNumber(int versionNum) throws Exception{
        DTVTRecord[] dtvtRecs = null;
        
        try {
            selectRecsStmt.setInt(1,versionNum);
            ResultSet rs = selectRecsStmt.executeQuery();
            dtvtRecs = getRecords(rs);
            rs.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
        return dtvtRecs;
    }

    private DTVTRecord[] getRecords(ResultSet rs) throws Exception {
        List allRecs = new ArrayList();
        while (rs.next()){
            int recId = rs.getInt("record_id");
            int dsId  = rs.getInt("dataset_id");
            int versNum = rs.getInt("version_number");
            String versName = rs.getString("version_name");
            String desc = rs.getString("description");
            
            DTVTRecord rec = new DTVTRecord(recId,dsId,desc,versName,versNum);
            allRecs.add(rec);
        }
        
        
        return (DTVTRecord[])allRecs.toArray(new DTVTRecord[0]);

        
    }

    private void printData(ResultSet rs) throws Exception {
        ResultSetMetaData rsmd = rs.getMetaData();
        int numberOfColumns = rsmd.getColumnCount();

        String delim="::";
        
        while (rs.next()){
            String result = "";
            result += rs.getInt("record_id") + delim;
            result += rs.getInt("dataset_id") + delim;
            result += rs.getInt("version_number") + delim;
            result += rs.getString("version_name") + delim;
            result += rs.getString("description") + delim;
            System.out.println(result);
        }
    }
    
    /**
     * @param rsmd
     * @param numberOfColumns
     * @throws SQLException
     */
    private void printMetaData(ResultSet rs) throws Exception {
        ResultSetMetaData rsmd = rs.getMetaData();
        int numberOfColumns = rsmd.getColumnCount();

        System.out.println("*****************************************************");

        for (int i=1; i<= numberOfColumns;i++){
            System.out.println("Column#: " + i);
            System.out.println("SchemaName: " + rsmd.getSchemaName(i));
            System.out.println("TableName: " + rsmd.getTableName(i));
            System.out.println("Catalog name: " + rsmd.getCatalogName(i));
            System.out.println("ColumnClassName: " + rsmd.getColumnClassName(i));
            System.out.println("ColumnClassName: " + rsmd.getColumnClassName(i));
            System.out.println("ColumnLabel: " + rsmd.getColumnLabel(i));
            System.out.println("ColumnName: " + rsmd.getColumnName(i));
            System.out.println("ColumnType: " + rsmd.getColumnType(i));
            System.out.println("ColumnTypeName: " + rsmd.getColumnTypeName(i));
            System.out.println("Precision: " + rsmd.getPrecision(i));
            System.out.println("Scale: " + rsmd.getScale(i));

            System.out.println("autoincrement: " + rsmd.isAutoIncrement(i));
            System.out.println("Nullable: " + rsmd.isNullable(i));
            System.out.println("Writable: " + rsmd.isWritable(i));
            System.out.println("CaseSensitive: " + rsmd.isCaseSensitive(i));                
            System.out.println("Currency: " + rsmd.isCurrency(i));
            System.out.println("DefinitelyWritable: " + rsmd.isDefinitelyWritable(i));
            System.out.println("ReadOnly: " + rsmd.isReadOnly(i));
            System.out.println("Searchable: " + rsmd.isSearchable(i));
            System.out.println("Signed: " + rsmd.isSigned(i));
            System.out.println("*****************************************************");
        }
    }

    
    public static void main(String[] args) throws Exception{
        new SimpleVersioningDAO();
    }

}

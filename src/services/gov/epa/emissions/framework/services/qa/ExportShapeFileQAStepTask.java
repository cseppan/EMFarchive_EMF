package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.ProjectionShapeFile;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.postgres.PostgresSQLToShapeFile;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.ExporterException;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.EmfProperty;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class ExportShapeFileQAStepTask implements Runnable {

    private QAStep qastep;

    private User user;

    private StatusDAO statusDao;

    private Log log = LogFactory.getLog(ExportShapeFileQAStepTask.class);

    private File file;

    private HibernateSessionFactory sessionFactory;

    private QAStepResult result;

    private String dirName;
    
    private String fileName;
    
    private boolean overide;

    private boolean verboseStatusLogging = true;

    private DbServerFactory dbServerFactory;

    private ProjectionShapeFile projectionShapeFile;

    private Pollutant pollutant;
    
    public ExportShapeFileQAStepTask(String dirName, String fileName, 
            boolean overide, QAStep qaStep,
            User user, HibernateSessionFactory sessionFactory, 
            DbServerFactory dbServerFactory, ProjectionShapeFile projectionShapeFile, boolean verboseStatusLogging, Pollutant pollutant) {
        this.dirName = dirName;
        this.fileName = fileName;
        this.overide = overide;
        this.qastep = qaStep;
        this.user = user;
        this.sessionFactory = sessionFactory;
        this.statusDao = new StatusDAO(sessionFactory);
        this.dbServerFactory = dbServerFactory;
        this.projectionShapeFile = projectionShapeFile;
        this.verboseStatusLogging = verboseStatusLogging;
        this.pollutant = pollutant;
    }

    public void run() {
        String suffix = "";
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            getStepResult();
            file = exportFile(dirName);
            PostgresSQLToShapeFile exporter = new PostgresSQLToShapeFile(dbServer);
            // Exporter exporter = new DatabaseTableCSVExporter(result.getTable(), dbServer.getEmissionsDatasource(),
            // batchSize(sessionFactory));
            suffix = suffix();
            prepare(suffix);
            if (projectionShapeFile == null)
                throw new ExporterException("The projection/shape file is missing.");
            exporter.create(getProperty("postgres-bin-dir"), getProperty("postgres-db"), getProperty("postgres-user"),
                    getProperty("pgsql2shp-info"), file.getAbsolutePath(), overide, prepareSQLStatement(), projectionShapeFile);
            complete(suffix);
        } catch (Exception e) {
            logError("Failed to export QA step : " + qastep.getName() + suffix, e);
            setStatus("Failed to export QA step " + qastep.getName() + suffix + ". Reason: " + e.getMessage());
        } finally {
            if (dbServer != null)
                try {
                    dbServer.disconnect();
                } catch (Exception e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                }
        }
    }

    private String getProperty(String propertyName) {
        Session session = sessionFactory.getSession();
        try {
            EmfProperty property = new EmfPropertiesDAO().getProperty(propertyName, session);
            return property.getValue();
        } finally {
            session.close();
        }
    }

    private String prepareSQLStatement() throws ExporterException {

        DbServer dbServer = null;
        boolean hasFipsCol = false;
        boolean hasFipsStCol = false;
        boolean hasPlantIdCol = false;
        boolean hasLatCol = false;
        boolean hasLonCol = false;
        boolean hasLatitudeCol = false;
        boolean hasLongitudeCol = false;
        boolean hasXLocCol = false;
        boolean hasYLocCol = false;
        String pollCol = "";
        // will hold unique list of column names, pqsql2shp doesn't like multiple columns with the same...
        Map<String, String> cols = new HashMap<String, String>();
        String colNames = "";
        String sql = "";
        String poll = pollutant.getName();

        try {
            dbServer = dbServerFactory.getDbServer();
            ResultSet rs = dbServer.getEmissionsDatasource().query().executeQuery(
                    "select * from emissions." + result.getTable() + " where 1 = 0");
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            String colName = "";
            for (int i = 1; i <= columnCount; i++) {
                colName = md.getColumnName(i).toLowerCase();
                if (!cols.containsKey(colName)) {
                    cols.put(colName, "qa." + colName);
                    colNames += (colNames.length() > 0 ? "," : "") + "qa." + colName;
                }

                if (colName.equals("fips")) {
                    hasFipsCol = true;
                } else if (colName.equals("fipsst")) {
                    hasFipsStCol = true;
                } else if (colName.equals("plantid") || colName.equals("plant_id")) {
                    hasPlantIdCol = true;
                } else if (colName.equals("longitude")) {
                    hasLongitudeCol = true;
                } else if (colName.equals("latitude")) {
                    hasLatitudeCol = true;
                } else if (colName.equals("lon")) {
                    hasLonCol = true;
                } else if (colName.equals("lat")) {
                    hasLatCol = true;
                } else if (colName.equals("xloc")) {
                    hasXLocCol = true;
                } else if (colName.equals("yloc")) {
                    hasYLocCol = true;
                } else if (colName.equals("poll") || colName.equals("pollutant")) {
                    pollCol = colName;
                }
            }
            if (!
                    (
                        hasPlantIdCol && 
                        (
                                (hasLongitudeCol && hasLatitudeCol) || (hasLonCol && hasLatCol) || (hasXLocCol && hasYLocCol)
                        )
                    )
                    && (
                            (hasFipsCol && projectionShapeFile.getType().equals("county")) 
                            || (hasFipsStCol && projectionShapeFile.getType().equals("state"))
                       )
                    ) {
                rs = dbServer.getEmissionsDatasource().query().executeQuery(
                        "select * from " + projectionShapeFile.getTableSchema() + "."
                                + projectionShapeFile.getTableName() + " where 1 = 0");
                md = rs.getMetaData();
                columnCount = md.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    colName = md.getColumnName(i).toLowerCase();
                    if (!cols.containsKey(colName)) {
                        cols.put(colName, "sh." + colName);
                        colNames += (colNames.length() > 0 ? "," : "") + "sh." + colName;
                    }
                }
            }
        } catch (SQLException e) {
            throw new ExporterException(e.getMessage());
        } finally {
            if (dbServer != null)
                try {
                    dbServer.disconnect();
                } catch (Exception e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                }
        }
        // Make sure the right projection/shape file was specified
        // Also, make sure the point query has enough info to proceed...
         if (hasPlantIdCol) {
             if (
             !(
                 (hasLongitudeCol && hasLatitudeCol)
                 || (hasLonCol && hasLatCol)
                 || (hasXLocCol && hasYLocCol)
             )) 
                 throw new ExporterException("The point query must have latitude and longitude columns.");
        // county level...
         } else if (hasFipsCol) {
            if (!projectionShapeFile.getType().equals("county"))
                throw new ExporterException("A county-level projection/shape file must be used.");
            // state level...
        } else if (hasFipsStCol) {
            if (!projectionShapeFile.getType().equals("state"))
                throw new ExporterException("A state-level projection/shape file must be used.");
        }

        // build the sql select statement using the specified projection/shape file
        // county level...
        if (hasPlantIdCol
                && ((hasLongitudeCol && hasLatitudeCol) || (hasLonCol && hasLatCol) || (hasXLocCol && hasYLocCol))) {
            sql = "select "
                    + colNames
                    + ", GeomFromText('POINT(' || "
                    + ((hasLongitudeCol && hasLatitudeCol) ? "longitude || ' ' || latitude"
                            : (hasLonCol && hasLatCol) ? "long || ' ' || lat" : "xloc || ' ' || yloc") + "|| ')', "
                    + projectionShapeFile.getSrid() + ") as the_geom" + " from emissions." + result.getTable() + " qa";
        } else if (hasFipsCol) {
            sql = "select " + colNames + " from emissions." + result.getTable() + " qa" + " inner join "
                    + projectionShapeFile.getTableSchema() + "." + projectionShapeFile.getTableName() + " sh"
                    + " on sh.fips = qa.fips";
            // state level...
        } else if (hasFipsStCol) {
            sql = "select " + colNames + " from emissions." + result.getTable() + " qa" + " inner join "
                    + projectionShapeFile.getTableSchema() + "." + projectionShapeFile.getTableName() + " sh"
                    + " on sh.statefp = qa.fipsst";
            // point level...
        } else {
            throw new ExporterException(
                    "QA result does not have a fips, fips state code, or plantid/latitude/longitude columns.");
        }
        System.out.println(sql + " where " + pollCol + " = '" + poll + "'");
        return sql + " where " + pollCol + " = '" + poll + "'";
    }

    private void prepare(String suffixMsg) {
        if (verboseStatusLogging)
            setStatus("Started exporting QA step '" + qastep.getName() + "'" + suffixMsg);
    }

    private void complete(String suffixMsg) {
        if (verboseStatusLogging)
            setStatus("Completed exporting QA step '" + qastep.getName() + "'" + suffixMsg);
    }

    private void logError(String message, Exception e) {
        log.error(message, e);

    }

    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("ExportQAStep");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusDao.add(endStatus);

    }

    private String suffix() {
        return " for Version '" + versionName() + "' of Dataset '" + datasetName() + "' to " + file.getAbsolutePath();
    }

    private String versionName() {
        Session session = sessionFactory.getSession();
        try {
            return new Versions().get(qastep.getDatasetId(), qastep.getVersion(), session).getName();
        } finally {
            session.close();
        }
    }

    private String datasetName() {
        Session session = sessionFactory.getSession();
        try {
            DatasetDAO dao = new DatasetDAO();
            return dao.getDataset(session, qastep.getDatasetId()).getName();
        } finally {
            session.close();
        }
    }

    private void getStepResult() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            result = new QADAO().qaStepResult(qastep, session);
            if (result == null || result.getTable() == null)
                throw new EmfException("You have to first run the QA Step before export");
        } finally {
            session.close();
        }
    }

    private File exportFile(String dirName) throws EmfException {
        return new File(validateDir(dirName), fileName());
    }

    private String fileName() {
        if ( fileName == null || fileName.trim().length()==0)
            return result.getTable();
        return fileName;
    }

    private File validateDir(String dirName) throws EmfException {
        File file = new File(dirName);

        if (!file.exists() || !file.isDirectory()) {
            log.error("Folder " + dirName + " does not exist");
            throw new EmfException("Folder does not exist: " + dirName);
        }
        return file;
    }
}

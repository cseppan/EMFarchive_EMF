package gov.epa.emissions.framework.services.fast.shapefile;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.postgres.PostgresSQLToShapeFile;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.ExporterException;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.EmfProperty;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.fast.FastDAO;
import gov.epa.emissions.framework.services.fast.Grid;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class ExportFastOutputToShapeFileTask implements Runnable {

    private String user;

    private StatusDAO statusDao;

    private Log log = LogFactory.getLog(ExportFastOutputToShapeFileTask.class);

    private File file;

    private HibernateSessionFactory sessionFactory;

    private String dirName;

    private DbServerFactory dbServerFactory;

    private String pollutant;

    private String userName;

    private int datasetId;

    private int gridId;

    private int datasetVersion;
    
    public ExportFastOutputToShapeFileTask(int datasetId, int datasetVersion, int gridId, String userName, String dirName,
            String pollutant, DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory) {
        this.datasetId = datasetId;
        this.datasetVersion = datasetVersion;
        this.gridId = gridId;
        this.dirName = dirName;
        this.userName = userName;
        this.sessionFactory = sessionFactory;
        this.statusDao = new StatusDAO(sessionFactory);
        this.dbServerFactory = dbServerFactory;
        this.pollutant = pollutant;
    }

    public void run() {
        String suffix = "";
        DbServer dbServer = dbServerFactory.getDbServer();
        EmfDataset dataset = getDataset(datasetId);
        try {
            Version datasetVersion = version(this.datasetId, this.datasetVersion);
            Grid grid = getGrid(gridId);
            

            PostgresSQLToShapeFile exporter = new PostgresSQLToShapeFile(dbServer);
            // Exporter exporter = new DatabaseTableCSVExporter(result.getTable(), dbServer.getEmissionsDatasource(),
            // batchSize(sessionFactory));
            for (String sector : getDatasetSectors(dataset, datasetVersion)) {
                file = exportFile(dirName, dataset, sector);
                suffix = suffix();
                prepare(suffix, dataset);
                String sql = prepareSQLStatement(dataset, datasetVersion, grid, this.pollutant, sector);
                exporter.create(getProperty("postgres-bin-dir"), getProperty("postgres-db"), getProperty("postgres-user"),
                        getProperty("pgsql2shp-info"), file.getAbsolutePath(), sql, null);
                complete(suffix, dataset);
            }
        } catch (Exception e) {
            logError("Failed to export dataset : " + dataset.getName() + suffix, e);
            setStatus("Failed to export dataset " + dataset.getName() + suffix + ". Reason: " + e.getMessage());
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

    private EmfDataset getDataset(int datasetId) {
        Session session = sessionFactory.getSession();
        try {
            return new DatasetDAO().getDataset(session, datasetId);
        } finally {
            session.close();
        }
    }

    private Grid getGrid(int gridId) {
        Session session = sessionFactory.getSession();
        try {
            return new FastDAO().getGrid(session, gridId);
        } finally {
            session.close();
        }
    }

    private Version version(int datasetId, int version) {
        Session session = sessionFactory.getSession();
        try {
            Versions versions = new Versions();
            return versions.get(datasetId, version, session);
        } finally {
            session.close();
        }
    }

    private String emissionTableName(Dataset dataset) {
        InternalSource[] internalSources = dataset.getInternalSources();
        return internalSources[0].getTable().toLowerCase();
    }

    private String[] getDatasetSectors(EmfDataset dataset, Version datasetVersion) throws EmfException {
        DbServer dbServer = null;
        List<String> sectorList = new ArrayList<String>();
        try {
            dbServer = dbServerFactory.getDbServer();
            VersionedQuery datasetVersionedQuery = new VersionedQuery(datasetVersion, "i");
            ResultSet rs = dbServer.getEmissionsDatasource().query().executeQuery(
                    "select distinct coalesce(sector, '') as sector from emissions." + emissionTableName(dataset) + " as i where " + datasetVersionedQuery.query() + " order by sector ");
            
            while (rs.next()) {
                sectorList.add(rs.getString(1));
            }
        } catch (SQLException e) {
            throw new EmfException(e.getMessage(), e);
        } finally {
            if (dbServer != null)
                try {
                    dbServer.disconnect();
                } catch (Exception e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                }
        }
        return sectorList.toArray(new String[0]);    }

    private String prepareSQLStatement(EmfDataset dataset, Version datasetVersion, Grid grid, String pollutant, String sector) throws ExporterException {

        DbServer dbServer = null;
        boolean hasXCol = false;
        boolean hasYCol = false;
        boolean hasPollutantCol = false;
        boolean hasCMAQPollutantCol = false;
        // will hold unique list of column names, pqsql2shp doesn't like multiple columns with the same name...
        Map<String, String> cols = new HashMap<String, String>();
        String colNames = "";
        String sql = "";
        String tableName = dataset.getInternalSources()[0].getTable();
        VersionedQuery datasetVersionedQuery = new VersionedQuery(datasetVersion, "i");

        try {
            dbServer = dbServerFactory.getDbServer();
            ResultSet rs = dbServer.getEmissionsDatasource().query().executeQuery(
                    "select * from emissions." + tableName + " where 1 = 0");
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            String colName = "";
            for (int i = 1; i <= columnCount; i++) {
                colName = md.getColumnName(i).toLowerCase();
                if (!cols.containsKey(colName)) {
                    cols.put(colName, colName);
                    if (colName.equals("x")) {
                        colNames += (colNames.length() > 0 ? "," : "") + "i." + colName;
                    } else if (colName.equals("y")) {
                        colNames += (colNames.length() > 0 ? "," : "") + "i." + colName;
                    } else {
                        colNames += (colNames.length() > 0 ? "," : "") + colName;
                    }
                }

                if (colName.equals("x")) {
                    hasXCol = true;
                } else if (colName.equals("y")) {
                    hasYCol = true;
                } else if (colName.equals("pollutant")) {
                    hasPollutantCol = true;
                } else if (colName.equals("cmaq_pollutant")) {
                    hasCMAQPollutantCol = true;
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
        sql = "select " + colNames + ", ST_translate(origin_grid.boxrep, hor.x * " + grid.getXcell() + ", vert.y * " + grid.getYcell() + ") As the_geom "
            + " from generate_series(0," + (grid.getNcols() - 1) + ") as hor(x) "
            + " cross join generate_series(0," + (grid.getNrows() - 1) + ") as vert(y) "
            + " cross join (SELECT ST_SetSRID(CAST('BOX(" + grid.getXcent() + " " + grid.getYcent() + "," + (grid.getXcent() + grid.getXcell()) + " " + (grid.getYcent() + grid.getYcell()) + ")' as box2d), 104307) as boxrep) as origin_grid "
            + " left outer join emissions." + tableName + " i "
            + " on i.x = hor.x + 1 "
            + " and i.y = vert.y + 1 "
            + " and " + datasetVersionedQuery.query() + " "
            + " and " + (hasPollutantCol ? "i.pollutant" : "i.cmaq_pollutant") + "='" + pollutant + "'"
            + " and i.sector='" + sector + "'"
            + " order by hor.x, vert.y ";
        
        System.out.println(sql);
    
        return sql;
    }

    private void prepare(String suffixMsg, EmfDataset dataset) {
        setStatus("Started exporting QA step '" + dataset.getName() + "'" + suffixMsg);
    }

    private void complete(String suffixMsg, EmfDataset dataset) {
        setStatus("Completed exporting QA step '" + dataset.getName() + "'" + suffixMsg);
    }

    private void logError(String message, Exception e) {
        log.error(message, e);

    }

    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(userName);
        endStatus.setType("ExportQAStep");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusDao.add(endStatus);

    }

    private String suffix() {
        return " for Version '" + "" + "' of Dataset '" + "" + "' to " + file.getAbsolutePath();
//        return " for Version '" + versionName() + "' of Dataset '" + datasetName() + "' to " + file.getAbsolutePath();
    }

//    private String versionName() {
//        Session session = sessionFactory.getSession();
//        try {
//            return new Versions().get(qastep.getDatasetId(), qastep.getVersion(), session).getName();
//        } finally {
//            session.close();
//        }
//    }
//
//    private String datasetName() {
//        Session session = sessionFactory.getSession();
//        try {
//            DatasetDAO dao = new DatasetDAO();
//            return dao.getDataset(session, qastep.getDatasetId()).getName();
//        } finally {
//            session.close();
//        }
//    }

    private File exportFile(String dirName, EmfDataset dataset, String sector) throws EmfException {
        return new File(validateDir(dirName), fileName(dataset, sector));
    }

    private String fileName(EmfDataset dataset, String sector) {
        return sector + "_" + dataset.getName();
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

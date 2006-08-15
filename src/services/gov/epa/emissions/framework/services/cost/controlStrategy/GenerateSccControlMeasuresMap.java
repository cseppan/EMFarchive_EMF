package gov.epa.emissions.framework.services.cost.controlStrategy;

import java.sql.ResultSet;
import java.sql.SQLException;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;

public class GenerateSccControlMeasuresMap {

    private Datasource emissionDatasource;

    private Datasource emfDatasource;

    private ControlStrategy controlStrategy;

    private SccControlMeasuresMap map;

    private String emissionTableName;

    public GenerateSccControlMeasuresMap(DbServer dbServer, String emissionTableName, ControlStrategy controlStrategy) {
        this.emissionDatasource = dbServer.getEmissionsDatasource();
        this.emfDatasource = dbServer.getEmfDatasource();
        this.emissionTableName = emissionTableName;
        this.controlStrategy = controlStrategy;
        map = new SccControlMeasuresMap();
    }

    public SccControlMeasuresMap create() throws EmfException {
        String query = query(emissionTableName);
        ResultSet rs = null;
        try {
            rs = emissionDatasource.query().executeQuery(query);
            read(rs);
        } catch (SQLException e) {
            throw new EmfException("Could not create a SccControlMeasuresMap: " + e.getMessage());
        } finally {
            if (rs != null)
                closeResultSet(rs);
        }
        return map;
    }

    private void closeResultSet(ResultSet rs) throws EmfException {
        try {
            rs.close();
        } catch (SQLException e) {
            throw new EmfException("Could not close result set after creating a SccControlMeasuresMap: \n"
                    + e.getMessage());
        }

    }

    private void read(ResultSet rs) throws SQLException {
        while (rs.next()) {
            String scc = rs.getString(1);
            int id = rs.getInt(2);
            map.add(scc, id);
        }

    }

    private String query(String emissionTableName) {
        return "SELECT DISTINCT a.scc,b.control_measures_id FROM " + qualifiedName(emissionTableName, emissionDatasource)
                + " AS a, " + qualifiedName("control_measure_sccs", emfDatasource) + " AS b WHERE a.poll='"
                + controlStrategy.getTargetPollutant() + "' AND a.scc=b.name";
    }

    private String qualifiedName(String tableName, Datasource datasource) {
        return datasource.getName() + "." + tableName;
    }

}

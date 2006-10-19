package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RetrieveSCC {

    private ControlMeasure measure;

    private DbServer dbServer;

    public RetrieveSCC(ControlMeasure measure, DbServer dbServer) throws Exception {
        this.measure = measure;
        this.dbServer = dbServer;
    }

    public Scc[] sccs() throws SQLException {
        int id = measure.getId();

        Scc[] sccs;
        try {
            ResultSet set = dbServer.getReferenceDatasource().query().executeQuery(query(id));
            sccs = values(set);
        } catch (SQLException e) {
            throw e;
        } finally {
            dbServer.disconnect();
        }

        return sccs;
    }

    public String[] cmAbbrevAndSccs() throws SQLException {
        int id = measure.getId();

        String[] cmAbbrevAndSccs;
        try {
            ResultSet set = dbServer.getReferenceDatasource().query().executeQuery(cmAbbrevAndSccQuery(id));
            cmAbbrevAndSccs = getAbbrevAndSccStrings(set);
        } catch (SQLException e) {
            throw e;
        } finally {
            dbServer.disconnect();
        }

        return cmAbbrevAndSccs;
    }

    private String[] getAbbrevAndSccStrings(ResultSet rs) throws SQLException {
        List abbrevSccs = new ArrayList();
        try {
            while (rs.next()) {
                abbrevSccs.add(rs.getString(1) + "," + rs.getString(2));
            }
        } finally {
            rs.close();
        }
        return (String[]) abbrevSccs.toArray(new String[0]);
    }

    private Scc[] values(ResultSet rs) throws SQLException {
        List sccs = new ArrayList();
        try {
            while (rs.next()) {
                Scc scc = new Scc(rs.getString(1), rs.getString(2));
                sccs.add(scc);
            }
        } finally {
            rs.close();
        }
        return (Scc[]) sccs.toArray(new Scc[0]);
    }

    private String query(int id) {
        String query = "SELECT e.name,r.scc_description FROM emf.control_measure_sccs AS e, reference.scc AS r "
                + "WHERE e.control_measures_id=" + id + " AND e.name=r.scc";
        return query;
    }

    private String cmAbbrevAndSccQuery(int id) {
        String query = "SELECT cm.abbreviation, e.name FROM emf.control_measures AS cm, emf.control_measure_sccs AS e "
                + "WHERE cm.id=" + id + " AND e.control_measures_id=" + id;
        return query;
    }

}

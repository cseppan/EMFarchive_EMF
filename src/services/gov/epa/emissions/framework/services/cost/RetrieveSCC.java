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

    public RetrieveSCC(ControlMeasure measure, DbServer dbServer) {
        this.measure = measure;
        this.dbServer = dbServer;
    }

    public Scc[] sccs() throws SQLException {
        int id = measure.getId();

        ResultSet set = dbServer.getReferenceDatasource().query().executeQuery(query(id));
        Scc[] sccs = values(set);

        return sccs;
    }

    private Scc[] values(ResultSet rs) throws SQLException {
        List sccs = new ArrayList();
        try {
            while (rs.next()) {
                Scc scc = new Scc(rs.getString(1), rs.getString(2));
                sccs.add(scc);
            }
        }finally{
            rs.close();
        }
        return (Scc[]) sccs.toArray(new Scc[0]);
    }

    private String query(int id) {
        String query = "SELECT e.name,r.scc_description FROM emf.control_measure_sccs AS e, reference.scc AS r "
                + "WHERE e.control_measures_id=" + id + " AND e.name=r.scc";
        return query;
    }

}

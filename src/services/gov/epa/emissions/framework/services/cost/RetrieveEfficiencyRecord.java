package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RetrieveEfficiencyRecord {

    private int controlMeasureId;

    private DbServer dbServer;

    public RetrieveEfficiencyRecord(int controlMeasureId, DbServer dbServer) throws Exception {
        this.controlMeasureId = controlMeasureId;
        this.dbServer = dbServer;
    }

    public EfficiencyRecord[] getEfficiencyRecords() throws SQLException {
        EfficiencyRecord[] efficiencyRecords;
        try {
            ResultSet set = dbServer.getReferenceDatasource().query().executeQuery(query(controlMeasureId));
            efficiencyRecords = values(set);
        } catch (SQLException e) {
            throw e;
        } finally {
            dbServer.disconnect();
        }

        return efficiencyRecords;
    }

    private EfficiencyRecord[] values(ResultSet rs) throws SQLException {
        List effRecs = new ArrayList();
        try {
            while (rs.next()) {
                EfficiencyRecord effRec = new EfficiencyRecord();
                effRec.setId(rs.getInt(1));
                effRec.setId(rs.getInt(1));

//rs.getString(1), desc
//                id serial NOT NULL,
//                control_measures_id int4 NOT NULL,
//                list_index int4,
//                record_id int4,
//                pollutant_id int4 NOT NULL,
//                existing_measure_abbr varchar(10),
//                existing_dev_code int4,
//                locale varchar(10),
//                efficiency float4,
//                percent_reduction float4,
//                cost_year int4,
//                cost_per_ton float4,
//                rule_effectiveness float4,
//                rule_penetration float4,
//                equation_type varchar(128),
//                cap_rec_factor float4,
//                discount_rate float4,
//                detail varchar(128),
//                effective_date timestamp,

                effRecs.add(effRec);
            }
        } finally {
            rs.close();
        }
        return (EfficiencyRecord[]) effRecs.toArray(new EfficiencyRecord[0]);
    }

    private String query(int controlMeasureId) {
        String query = "SELECT e.name,r.scc_description FROM emf.control_measure_sccs AS e LEFT OUTER JOIN reference.scc AS r "
                + "ON (e.name=r.scc) WHERE e.control_measures_id=" + controlMeasureId;
        return query;
    }
}

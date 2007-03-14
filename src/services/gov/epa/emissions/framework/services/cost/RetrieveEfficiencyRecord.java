package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class RetrieveEfficiencyRecord {

    private int controlMeasureId;

    private DbServer dbServer;
    private HashMap<String, String> effRecColumnMap;

    public RetrieveEfficiencyRecord(int controlMeasureId, DbServer dbServer) throws Exception {
        this.controlMeasureId = controlMeasureId;
        this.dbServer = dbServer;
        populateEffRecColumnMap();
    }

    private void populateEffRecColumnMap() {
        effRecColumnMap = new HashMap<String, String>();
        effRecColumnMap.put("Pollutant", "p.name");
        effRecColumnMap.put("Locale", "er.locale");
        effRecColumnMap.put("Effective Date", "er.effective_date");
        effRecColumnMap.put("Existing Measure", "er.existing_measure_abbr");
        effRecColumnMap.put("Existing NEI Dev", "er.existing_dev_code");
        effRecColumnMap.put("Cost Year", "er.cost_year");
        effRecColumnMap.put("Cost Per Ton", "er.cost_per_ton");
        effRecColumnMap.put("Control Efficiency", "er.efficiency");
        effRecColumnMap.put("Rule Effectiveness", "er.rule_effectiveness");
        effRecColumnMap.put("Rule Penetration", "er.rule_penetration");
        effRecColumnMap.put("Equation Type", "er.equation_type");
        effRecColumnMap.put("Capital Rec Fac", "er.cap_rec_factor");
        effRecColumnMap.put("Discount Rate", "er.discount_rate");
        effRecColumnMap.put("Last Modifed By", "er.last_modified_by");
        effRecColumnMap.put("Last Modifed Date", "er.last_modified_time");
        effRecColumnMap.put("Details", "er.detail");
//        "Pollutant", "Locale", "Effective Date", "Existing Measure", "Existing NEI Dev",
//        "Cost Year", "Cost Per Ton", "Control Efficiency", "Rule Effectiveness", "Rule Penetration",
//        "Equation Type", "Capital Rec Fac", "Discount Rate", "Last Modifed By", "Last Modifed Date", "Details"
//        
//        String query = "select er.id, er.control_measures_id"
//            + ", er.record_id, er.pollutant_id , p.name, er.existing_measure_abbr, er.existing_dev_code, er.locale"
//            + ", er.efficiency, er.cost_year, er.cost_per_ton, er.rule_effectiveness, er.rule_penetration"
//            + ", er.equation_type, er.cap_rec_factor, er.discount_rate, er.detail, er.effective_date, er.last_modified_by, er.last_modified_time "

    }

    public EfficiencyRecord[] getEfficiencyRecords(int recordLimit, String filter) throws SQLException, EmfException {
        EfficiencyRecord[] efficiencyRecords = {};
        try {
            ResultSet set = dbServer.getReferenceDatasource().query().executeQuery(query(controlMeasureId, recordLimit, filter));
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
                effRec.setControlMeasureId(rs.getInt(2));
                effRec.setRecordId(rs.getInt(3));
                effRec.setPollutant(new Pollutant(rs.getInt(4), rs.getString(5)));
                effRec.setExistingMeasureAbbr(rs.getString(6));
                effRec.setExistingDevCode(rs.getInt(7));
                effRec.setLocale(rs.getString(8));
                effRec.setEfficiency(rs.getFloat(9));
                effRec.setCostYear(rs.getInt(10));
                effRec.setCostPerTon(rs.getFloat(11));
                effRec.setRuleEffectiveness(rs.getFloat(12));
                effRec.setRulePenetration(rs.getFloat(13));
                effRec.setEquationType(rs.getString(14));
                effRec.setCapRecFactor(rs.getFloat(15));
                effRec.setDiscountRate(rs.getFloat(16));
                effRec.setDetail(rs.getString(17));
                effRec.setEffectiveDate(rs.getTimestamp(18));
                effRec.setLastModifiedBy(rs.getString(19));
                effRec.setLastModifiedTime(rs.getTimestamp(20));
                effRec.setRefYrCostPerTon(rs.getFloat(21));

//                id serial NOT NULL,
//                control_measures_id int4 NOT NULL,
//                record_id int4,
//              pollutant_id int4 NOT NULL,
//              pollutant_NAME string,
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
//                last_modified_by varchar(255) NOT NULL DEFAULT ''::character varying,
//                last_modified_time timestamp NOT NULL DEFAULT now(),

                effRecs.add(effRec);
            }
        } finally {
            rs.close();
        }
        return (EfficiencyRecord[]) effRecs.toArray(new EfficiencyRecord[0]);
    }

    private String query(int controlMeasureId, int recordLimit, String filter) throws EmfException {

        //validate and translate the filter...
        filter = translateFilter(filter);
                
        String query = "select er.id, er.control_measures_id"
            + ", er.record_id, er.pollutant_id , p.name, er.existing_measure_abbr, er.existing_dev_code, er.locale"
            + ", er.efficiency, er.cost_year, er.cost_per_ton, er.rule_effectiveness, er.rule_penetration"
            + ", er.equation_type, er.cap_rec_factor, er.discount_rate, er.detail, er.effective_date, er.last_modified_by, er.last_modified_time, er.ref_yr_cost_per_ton "
            + "from emf.control_measure_efficiencyrecords er "
            + "inner join emf.pollutants p "
            + "on p.id = er.pollutant_id "
            + "WHERE er.control_measures_id=" + controlMeasureId
            + ((filter.length() > 0) ? " and " + filter : "")
            + " order by p.name, er.locale "
            + "LIMIT " + recordLimit;

        return query;
    }

    private String translateFilter(String filter) throws EmfException {
        if (filter.trim().length() == 0) return "";
        filter = filter.trim().toUpperCase();
        // Parse a line with and's and or's
        String patternStr = "(AND|OR)";//"\\+(AND|OR)*";
        String[] fields = filter.split(patternStr, -1);
        boolean found = false;
        for (int i = 0; i < fields.length; i++) {
            found = false;
            Iterator k = effRecColumnMap.keySet().iterator();
            while (k.hasNext()) {
                String key = (String) k.next();
                String value = effRecColumnMap.get(key);
                key = key.toUpperCase();
                if (fields[i].indexOf(key) >= 0) {
                    filter = filter.replaceAll(key, value);
                    found = true;
                }
            }
            if (!found) throw new EmfException("The filter column was not found in the dataset : " + fields[i]);
        }
        return filter;
    }

}

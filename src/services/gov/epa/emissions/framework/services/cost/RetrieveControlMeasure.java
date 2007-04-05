package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.data.SourceGroup;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.cost.data.AggregatedPollutantEfficiencyRecord;
import gov.epa.emissions.framework.services.cost.data.ControlTechnology;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

public class RetrieveControlMeasure {
//    private static Log log = LogFactory.getLog(RetrieveControlMeasure.class);

    private DbServer dbServer;
    
    public RetrieveControlMeasure(DbServer dbServer) throws Exception {
        this.dbServer = dbServer;
    }

    public ControlMeasure[] getControlMeasures() throws SQLException {
        ControlMeasure[] controlMeasures = {};
        try {
            String sql = query();
//            log.error("start db - " + sql);
            ResultSet set = dbServer.getReferenceDatasource().query().executeQuery(sql);
//            log.error("end db, build measures");
            controlMeasures = values(set);
//            log.error("end measures");
        } catch (SQLException e) {
            throw e;
        } finally {
            dbServer.disconnect();
        }

        return controlMeasures;
    }

    public ControlMeasure[] getControlMeasures(int majorPollutantId) throws SQLException {
        ControlMeasure[] controlMeasures = {};
        try {
            String sql = query(majorPollutantId);
//            log.error("start db - " + sql);
            ResultSet set = dbServer.getReferenceDatasource().query().executeQuery(sql);
//            log.error("end db, build measures");
            controlMeasures = values(set);
//            log.error("end measures");
        } catch (SQLException e) {
            throw e;
        } finally {
            dbServer.disconnect();
        }

        return controlMeasures;
    }

    private ControlMeasure[] values(ResultSet rs) throws SQLException {
        List cms = new ArrayList();
        int cmId = Integer.MIN_VALUE;
        String pollutant = "";
        String sector = "";
        ControlMeasure cm = null;
        try {
            boolean next = rs.next();
            boolean hasRecords = next;
            while (next) {
                if (cmId != rs.getInt(1)) {
                    if (cmId != Integer.MIN_VALUE && cmId != rs.getInt(1)) {
                        //add measure
                        cms.add(cm);
                    }
                    //reset
                    pollutant = "";
                    sector = "";
                    cm = new ControlMeasure();
                    cm.setId(rs.getInt(1));
                    cm.setName(rs.getString(2));
                    cm.setAbbreviation(rs.getString(3));
                    cm.setDescription(rs.getString(4));
                    cm.setDeviceCode(rs.getInt(5));
                    cm.setMajorPollutant(new Pollutant(rs.getInt(6), rs.getString(7)));
                    cm.setCostYear(rs.getInt(8));
                    cm.setLastModifiedTime(rs.getTimestamp(9));
                    cm.setDateReviewed(rs.getTimestamp(10));
                    cm.setLastModifiedBy(rs.getString(11));
                    cm.setCreator(new User(rs.getString(13)));
                    cm.setControlTechnology(new ControlTechnology(rs.getString(15)));
                    cm.setSourceGroup(new SourceGroup(rs.getString(17)));
                    cm.setCmClass(new ControlMeasureClass(rs.getString(19)));
                    cm.setEquipmentLife(rs.getFloat(31));
                    cm.setDataSouce(rs.getString(32));
                }
                if (rs.getString(22) != null && !pollutant.trim().equalsIgnoreCase(rs.getString(22).trim())) {
                    cm.addAggregatedPollutantEfficiencyRecord(new AggregatedPollutantEfficiencyRecord(
                            new Pollutant(rs.getInt(21), rs.getString(22)), rs.getFloat(23), 
                            rs.getFloat(24), rs.getFloat(25),
                            rs.getFloat(26), rs.getFloat(27),
                            rs.getFloat(28), rs.getFloat(29),
                            rs.getFloat(30)));
                }
                if (rs.getString(20) != null && !sector.trim().equalsIgnoreCase(rs.getString(20).trim())) {
                  cm.addSector(new Sector("", rs.getString(20)));
                }
                cmId = rs.getInt(1);
                pollutant = rs.getString(22) != null ? rs.getString(22) : "";
                sector = rs.getString(20) != null ? rs.getString(20) : "";
                next = rs.next();
            }
            if (hasRecords) {
                cms.add(cm);
            }
        } finally {
            rs.close();
        }
        return (ControlMeasure[]) cms.toArray(new ControlMeasure[0]);
    }

    private String query() {

        String query = "select cm.id, cm.name, " +
                "cm.abbreviation, cm.description, " +
                "cm.device_code, cm.major_pollutant, mp.name, " +
                "cm.cost_year, cm.last_modified_time, " +
                "cm.date_reviewed, cm.last_modified_by, " +
                "cm.creator, u.name, " +
                "cm.control_technology, ct.name, " +
                "cm.source_group, sg.name, " +
                "cm.cm_class_id, cmc.name, " +
                "s.name, null as pollutant_id /*er.pollutant_id*/, " +
                "null as name /*p.name*/, " +
                "null as maxefficiency, null as minefficiency, " +
                "null as avgefficiency, null as maxcpt, " +
                "null as mincpt, null as avgcpt, " +
                "null as avgruleff, null as avgrulpen, " +
                "cm.equipment_life, cm.data_souce, cms.sector_id " +
                "from emf.control_measures cm " +
                "left outer join emf.control_measurs_sectors cms " +
                "on cms.control_measure_id = cm.id " +
                "left outer join emf.sectors s " +
                "on s.id = cms.sector_id " +
//                "left outer join ( " +
//                "select cms.control_measure_id, s.name || '...' as SCCNameList " +
//                "from control_measurs_sectors cms " +
//                "inner join sectors s " +
//                "on s.id = cms.sector_id " +
//                "limit 1) scclist " +
//                "on scclist.control_measure_id = cm.id " +
                "left outer join emf.users u " +
                "on u.id = cm.creator " +
                "left outer join emf.control_technologies ct " +
                "on ct.id = cm.control_technology " +
                "left outer join emf.source_groups sg " +
                "on sg.id = cm.source_group " +
                "left outer join emf.control_measure_classes cmc " +
                "on cmc.id = cm.cm_class_id " +
//                "left outer join ( " +
//                "select  control_measures_id, pollutant_id, max(efficiency) as maxefficiency, min(efficiency) as minefficiency, " +
//                "avg(efficiency) as avgefficiency, max(cost_per_ton) as maxcpt, " +
//                "min(cost_per_ton) as mincpt, avg(cost_per_ton) as avgcpt, " +
//                "avg(rule_effectiveness) as avgruleff, avg(rule_penetration) as avgrulpen " +
//                "from emf.control_measure_efficiencyrecords " +
//                "group by control_measures_id, pollutant_id)  er " +
//                "on er.control_measures_id = cm.id " +
//                "left outer join emf.pollutants p " +
//                "on p.id = er.pollutant_id " +
                "left outer join emf.pollutants mp " +
                "on mp.id = cm.major_pollutant " +
                "order by cm.name, cm.id/*, p.name*/";

        query = "select cm.id, cm.name, " +
                "cm.abbreviation, cm.description, " +
                "cm.device_code, cm.major_pollutant, mp.name, " +
                "cm.cost_year, cm.last_modified_time, " +
                "cm.date_reviewed, cm.last_modified_by, " +
                "cm.creator, u.name, " +
                "cm.control_technology, ct.name, " +
                "cm.source_group, sg.name, " +
                "cm.cm_class_id, cmc.name, " +
                "s.name, aer.pollutant_id, " +
                "p.name, " +
                "max_efficiency, min_efficiency, " +
                "avg_efficiency, max_cost_per_ton, " +
                "min_cost_per_ton, avg_cost_per_ton, " +
                "avg_rule_effectiveness, avg_rule_penetration, " +
                "cm.equipment_life, cm.data_souce, cms.sector_id " +
                "from emf.control_measures cm " +
                "left outer join emf.control_measurs_sectors cms " +
                "on cms.control_measure_id = cm.id " +
                "left outer join emf.sectors s " +
                "on s.id = cms.sector_id " +
//                "left outer join ( " +
//                "select cms.control_measure_id, s.name || '...' as SCCNameList " +
//                "from control_measurs_sectors cms " +
//                "inner join sectors s " +
//                "on s.id = cms.sector_id " +
//                "limit 1) scclist " +
//                "on scclist.control_measure_id = cm.id " +
                "left outer join emf.users u " +
                "on u.id = cm.creator " +
                "left outer join emf.control_technologies ct " +
                "on ct.id = cm.control_technology " +
                "left outer join emf.source_groups sg " +
                "on sg.id = cm.source_group " +
                "left outer join emf.control_measure_classes cmc " +
                "on cmc.id = cm.cm_class_id " +
//              "left outer join ( " +
                "left outer join emf.aggregrated_efficiencyrecords aer " +
                "on aer.control_measures_id = cm.id " +
//                "left outer join ( " +
//                "select  control_measures_id, pollutant_id, max(efficiency) as maxefficiency, min(efficiency) as minefficiency, " +
//                "avg(efficiency) as avgefficiency, max(cost_per_ton) as maxcpt, " +
//                "min(cost_per_ton) as mincpt, avg(cost_per_ton) as avgcpt, " +
//                "avg(rule_effectiveness) as avgruleff, avg(rule_penetration) as avgrulpen " +
//                "from emf.control_measure_efficiencyrecords " +
//                "group by control_measures_id, pollutant_id)  er " +
//                "on er.control_measures_id = cm.id " +
                "left outer join emf.pollutants p " +
                "on p.id = aer.pollutant_id " +
                "left outer join emf.pollutants mp " +
                "on mp.id = cm.major_pollutant " +
                "order by cm.name, cm.id, p.name";

        return query;
    }

    private String query(int majorPollutantId) {

        String query = "select cm.id, cm.name, " +
                "cm.abbreviation, cm.description, " +
                "cm.device_code, cm.major_pollutant, mp.name, " +
                "cm.cost_year, cm.last_modified_time, " +
                "cm.date_reviewed, cm.last_modified_by, " +
                "cm.creator, u.name, " +
                "cm.control_technology, ct.name, " +
                "cm.source_group, sg.name, " +
                "cm.cm_class_id, cmc.name, " +
                "s.name, null as pollutant_id /*er.pollutant_id*/, " +
                "null as name /*p.name*/, " +
                "null as maxefficiency, null as minefficiency, " +
                "null as avgefficiency, null as maxcpt, " +
                "null as mincpt, null as avgcpt, " +
                "null as avgruleff, null as avgrulpen, " +
                "cm.equipment_life, cm.data_souce " +
                "from emf.control_measures cm " +
                "left outer join emf.control_measurs_sectors cms " +
                "on cms.control_measure_id = cm.id " +
                "left outer join emf.sectors s " +
                "on s.id = cms.sector_id " +
//                "left outer join ( " +
//                "select cms.control_measure_id, s.name || '...' as SCCNameList " +
//                "from control_measurs_sectors cms " +
//                "inner join sectors s " +
//                "on s.id = cms.sector_id " +
//                "limit 1) scclist " +
//                "on scclist.control_measure_id = cm.id " +
                "left outer join emf.users u " +
                "on u.id = cm.creator " +
                "left outer join emf.control_technologies ct " +
                "on ct.id = cm.control_technology " +
                "left outer join emf.source_groups sg " +
                "on sg.id = cm.source_group " +
                "left outer join emf.control_measure_classes cmc " +
                "on cmc.id = cm.cm_class_id " +
//                "left outer join ( " +
//                "select  control_measures_id, pollutant_id, " +
//                "max(efficiency) as maxefficiency, min(efficiency) as minefficiency, " +
//                "avg(efficiency) as avgefficiency, max(ref_yr_cost_per_ton) as maxcpt, " +
//                "min(ref_yr_cost_per_ton) as mincpt, avg(ref_yr_cost_per_ton) as avgcpt, " +
//                "avg(rule_effectiveness) as avgruleff, avg(rule_penetration) as avgrulpen " +
//                "from emf.control_measure_efficiencyrecords " +
//                "group by control_measures_id, pollutant_id)  er " +
//                "on er.control_measures_id = cm.id " +
//                "left outer join emf.pollutants p " +
//                "on p.id = er.pollutant_id " +
                "left outer join emf.pollutants mp " +
                "on mp.id = cm.major_pollutant " +
                "where cm.major_pollutant = " + majorPollutantId + " " +
                "order by cm.name, cm.id/*, p.name*/";

        query = "select cm.id, cm.name, " +
        "cm.abbreviation, cm.description, " +
        "cm.device_code, cm.major_pollutant, mp.name, " +
        "cm.cost_year, cm.last_modified_time, " +
        "cm.date_reviewed, cm.last_modified_by, " +
        "cm.creator, u.name, " +
        "cm.control_technology, ct.name, " +
        "cm.source_group, sg.name, " +
        "cm.cm_class_id, cmc.name, " +
        "s.name, aer.pollutant_id, " +
        "p.name, " +
        "max_efficiency, min_efficiency, " +
        "avg_efficiency, max_cost_per_ton, " +
        "min_cost_per_ton, avg_cost_per_ton, " +
        "avg_rule_effectiveness, avg_rule_penetration, " +
        "cm.equipment_life, cm.data_souce, cms.sector_id " +
        "from emf.control_measures cm " +
        "left outer join emf.control_measurs_sectors cms " +
        "on cms.control_measure_id = cm.id " +
        "left outer join emf.sectors s " +
        "on s.id = cms.sector_id " +
//        "left outer join ( " +
//        "select cms.control_measure_id, s.name || '...' as SCCNameList " +
//        "from control_measurs_sectors cms " +
//        "inner join sectors s " +
//        "on s.id = cms.sector_id " +
//        "limit 1) scclist " +
//        "on scclist.control_measure_id = cm.id " +
        "left outer join emf.users u " +
        "on u.id = cm.creator " +
        "left outer join emf.control_technologies ct " +
        "on ct.id = cm.control_technology " +
        "left outer join emf.source_groups sg " +
        "on sg.id = cm.source_group " +
        "left outer join emf.control_measure_classes cmc " +
        "on cmc.id = cm.cm_class_id " +
//      "left outer join ( " +
        "left outer join emf.aggregrated_efficiencyrecords aer " +
        "on aer.control_measures_id = cm.id " +
//        "left outer join ( " +
//        "select  control_measures_id, pollutant_id, max(efficiency) as maxefficiency, min(efficiency) as minefficiency, " +
//        "avg(efficiency) as avgefficiency, max(cost_per_ton) as maxcpt, " +
//        "min(cost_per_ton) as mincpt, avg(cost_per_ton) as avgcpt, " +
//        "avg(rule_effectiveness) as avgruleff, avg(rule_penetration) as avgrulpen " +
//        "from emf.control_measure_efficiencyrecords " +
//        "group by control_measures_id, pollutant_id)  er " +
//        "on er.control_measures_id = cm.id " +
        "left outer join emf.pollutants p " +
        "on p.id = aer.pollutant_id " +
        "left outer join emf.pollutants mp " +
        "on mp.id = cm.major_pollutant " +
        "where cm.major_pollutant = " + majorPollutantId + " " +
        "order by cm.name, cm.id, p.name";

        return query;
    }
}

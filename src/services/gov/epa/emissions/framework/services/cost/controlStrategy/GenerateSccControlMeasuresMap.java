package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureClass;
import gov.epa.emissions.framework.services.cost.ControlMeasureDAO;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.LightControlMeasure;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.Session;

public class GenerateSccControlMeasuresMap {

//    private static Log log = LogFactory.getLog(GenerateSccControlMeasuresMap.class);

    private Datasource emissionDatasource;

    private Datasource emfDatasource;

    private ControlStrategy controlStrategy;

    private String qualifiedEmissionTableName;

    private HibernateSessionFactory sessionFactory;

    private String classFilterIds;
    
    private String controlMeasureFilterIds;

    public GenerateSccControlMeasuresMap(DbServer dbServer, String qualifiedEmissionTableName, ControlStrategy controlStrategy,
            HibernateSessionFactory sessionFactory) {
        this.emissionDatasource = dbServer.getEmissionsDatasource();
        this.emfDatasource = dbServer.getEmfDatasource();
        this.qualifiedEmissionTableName = qualifiedEmissionTableName;
        this.controlStrategy = controlStrategy;
        this.sessionFactory = sessionFactory;
        //add additional class filter, if necessary, when selecting control measure for the calculations...
        this.classFilterIds = classIdList();
        this.controlMeasureFilterIds = controlMeasureIdList();
    }

    public SccControlMeasuresMap create() throws EmfException {
        String query = query();
        ResultSet rs = null;
        try {
            rs = emissionDatasource.query().executeQuery(query);
            return buildMap(rs);
        } catch (SQLException e) {
            throw new EmfException("Could not create a SccControlMeasuresMap: " + e.getMessage());
        } finally {
            if (rs != null)
                closeResultSet(rs);
        }
    }

    public SccControlMeasuresMap create(String scc) throws EmfException {
        String query = query(scc);
        ResultSet rs = null;
        try {
            rs = emissionDatasource.query().executeQuery(query);
            return buildMap(rs);
        } catch (SQLException e) {
            throw new EmfException("Could not create a SccControlMeasuresMap: " + e.getMessage());
        } finally {
            if (rs != null)
                closeResultSet(rs);
        }
    }

    private void closeResultSet(ResultSet rs) throws EmfException {
        try {
            rs.close();
        } catch (SQLException e) {
            throw new EmfException("Could not close result set after creating a SccControlMeasuresMap: \n"
                    + e.getMessage());
        }

    }

    private SccControlMeasuresMap buildMap(ResultSet rs) throws SQLException {
        SccControlMeasuresMap map = new SccControlMeasuresMap();
        while (rs.next()) {
            String scc = rs.getString(1);
            int id = rs.getInt(2);
            map.add(scc, controlMeasure(id));
        }
        return map;
    }

    private ControlMeasure controlMeasure(int id) {
        Session session = sessionFactory.getSession();
        try {
            ControlMeasure measure;
            ControlMeasureDAO dao = new ControlMeasureDAO();
            measure = dao.current(id, session);
/*
            //pouplate the Rule Pen and Effect, that was set during the strat...
            LightControlMeasure[] cms = controlStrategy.getControlMeasures();
            for (int i = 0; i < cms.length; i++) {
                LightControlMeasure cm = cms[i];
                if (measure.getId() == cm.getId()) {
                    measure.setRuleEffectiveness(cm.getRuleEffectiveness());
                    measure.setRulePenetration(cm.getRulePenetration());
                }
            }
*/
            EfficiencyRecord[] ers = (EfficiencyRecord[]) dao.getEfficiencyRecords(measure.getId(), session).toArray(new EfficiencyRecord[0]);
            //no need to keep these in the hibernate cache
            session.clear();
//            log.error("Measure = " + measure.getName() + " EfficiencyRecord length = " + ers.length);
            measure.setEfficiencyRecords(ers);
            return measure;
        } finally {
            session.close();
        }
    }

    private String query() {
        return "SELECT DISTINCT a.scc,b.control_measures_id FROM " + qualifiedEmissionTableName + " AS a, "
                + qualifiedName("control_measure_sccs", emfDatasource) + " AS b, "
                + qualifiedName("control_measures", emfDatasource) + " AS c"
                + " WHERE b.name=a.scc"
                + " AND c.id=b.control_measures_id"
                + " AND a.poll='" + controlStrategy.getTargetPollutant() + "'"
                + (controlMeasureFilterIds.length() == 0 ? (classFilterIds.length() == 0 ? "" : " AND c.cm_class_id in (" + classFilterIds + ")") : "")
                + (controlMeasureFilterIds.length() == 0 ? "" : " AND b.control_measures_id in (" + controlMeasureFilterIds + ")");
    }

    private String query(String scc) {
        return "SELECT DISTINCT b.name as scc,b.control_measures_id "
                + " FROM " + qualifiedName("control_measure_sccs", emfDatasource) + " AS b, "
                + qualifiedName("control_measures", emfDatasource) + " AS c"
                + " WHERE b.name= '" + scc + "'"
                + " AND c.id=b.control_measures_id"
                + (controlMeasureFilterIds.length() == 0 ? (classFilterIds.length() == 0 ? "" : " AND c.cm_class_id in (" + classFilterIds + ")") : "")
                + (controlMeasureFilterIds.length() == 0 ? "" : " AND b.control_measures_id in (" + controlMeasureFilterIds + ")");
    }

    private String qualifiedName(String tableName, Datasource datasource) {
        return datasource.getName() + "." + tableName;
    }

    private String controlMeasureIdList() {
        String controlMeasureFilterIds = "";
        LightControlMeasure[] controlMeasures = controlStrategy.getControlMeasures();

        if (controlMeasures != null) 
            for (int i = 0; i < controlMeasures.length; i++)
                controlMeasureFilterIds += (controlMeasureFilterIds.length() != 0 ? "," + controlMeasures[i].getId() : controlMeasures[i].getId());
        return controlMeasureFilterIds;
    }

    private String classIdList() {
        String classFilterIds = "";
        ControlMeasureClass[] classes = controlStrategy.getControlMeasureClasses();

        if (classes != null) 
            for (int i = 0; i < classes.length; i++)
                classFilterIds += (classFilterIds.length() != 0 ? "," + classes[i].getId() : classes[i].getId());
        return classFilterIds;
    }
}
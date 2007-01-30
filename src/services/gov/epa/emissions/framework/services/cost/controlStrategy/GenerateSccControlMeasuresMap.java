package gov.epa.emissions.framework.services.cost.controlStrategy;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.Session;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureClass;
import gov.epa.emissions.framework.services.cost.ControlMeasureDAO;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

public class GenerateSccControlMeasuresMap {

    private Datasource emissionDatasource;

    private Datasource emfDatasource;

    private ControlStrategy controlStrategy;

    private SccControlMeasuresMap map;

    private String qualifiedEmissionTableName;

    private HibernateSessionFactory sessionFactory;

    public GenerateSccControlMeasuresMap(DbServer dbServer, String qualifiedEmissionTableName, ControlStrategy controlStrategy,
            HibernateSessionFactory sessionFactory) {
        this.emissionDatasource = dbServer.getEmissionsDatasource();
        this.emfDatasource = dbServer.getEmfDatasource();
        this.qualifiedEmissionTableName = qualifiedEmissionTableName;
        this.controlStrategy = controlStrategy;
        this.sessionFactory = sessionFactory;
        map = new SccControlMeasuresMap();
    }

    public SccControlMeasuresMap create() throws EmfException {
        String query = query(qualifiedEmissionTableName);
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
            map.add(scc, controlMeasure(id));
        }
    }

    private ControlMeasure controlMeasure(int id) {
        Session session = sessionFactory.getSession();
        try {
            ControlMeasureDAO dao = new ControlMeasureDAO();
            return dao.current(id, ControlMeasure.class, session);
        } finally {
            session.close();
        }
    }

    private String query(String qualifiedEmissionTableName) {
        //add additional class filter, if necessary, when selecting control measure for the calculations...
        String classFilterIds = "";
        ControlMeasureClass[] classes = controlStrategy.getControlMeasureClasses();

        if (classes != null) 
            for (int i = 0; i < classes.length; i++)
                classFilterIds += (classFilterIds.length() != 0 ? "," + classes[i].getId() : classes[i].getId());
        
        return "SELECT DISTINCT a.scc,b.control_measures_id FROM " + qualifiedEmissionTableName + " AS a, "
                + qualifiedName("control_measure_sccs", emfDatasource) + " AS b, "
                + qualifiedName("control_measures", emfDatasource) + " AS c"
                + " WHERE b.name=a.scc"
                + " AND c.id=b.control_measures_id"
                + " AND a.poll='" + controlStrategy.getTargetPollutant() + "'"
                + (classFilterIds.length() == 0 ? "" : " AND c.cm_class_id in (" + classFilterIds + ")");
    }

    private String qualifiedName(String tableName, Datasource datasource) {
        return datasource.getName() + "." + tableName;
    }

}

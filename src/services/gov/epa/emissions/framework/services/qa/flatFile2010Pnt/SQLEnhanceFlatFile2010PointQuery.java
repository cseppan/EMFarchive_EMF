package gov.epa.emissions.framework.services.qa.flatFile2010Pnt;

import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.services.qa.SQLQAProgramQuery;

public class SQLEnhanceFlatFile2010PointQuery extends SQLQAProgramQuery {

    public SQLEnhanceFlatFile2010PointQuery(HibernateSessionFactory sessionFactory, String emissioDatasourceName,
            String tableName, QAStep qaStep) {
        super(sessionFactory, emissioDatasourceName, tableName, qaStep);
        // NOTE Auto-generated constructor stub
    }

}

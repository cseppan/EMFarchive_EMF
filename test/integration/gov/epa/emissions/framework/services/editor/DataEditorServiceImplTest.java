package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.framework.services.DataEditorServiceTestCase;
import gov.epa.emissions.framework.services.editor.DataEditorServiceImpl;
import gov.epa.emissions.framework.services.impl.HibernateSessionFactory;

public class DataEditorServiceImplTest extends DataEditorServiceTestCase {

    protected void setUp() throws Exception {
        super.setUp();

        HibernateSessionFactory sessionFactory = new HibernateSessionFactory(sessionFactory());
        DataEditorServiceImpl service = new DataEditorServiceImpl(super.dbServer());
        super.setUpService(service);
    }

}

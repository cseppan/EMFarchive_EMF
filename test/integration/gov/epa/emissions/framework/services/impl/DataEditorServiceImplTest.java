package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.framework.services.DataEditorServiceTestCase;
import gov.epa.emissions.framework.services.editor.DataEditorServiceImpl;

public class DataEditorServiceImplTest extends DataEditorServiceTestCase {

    protected void doSetUp() throws Exception {
        HibernateSessionFactory sessionFactory = new HibernateSessionFactory(sessionFactory());
        DataEditorServiceImpl service = new DataEditorServiceImpl(emf(), super.dbServer(),  sessionFactory);

        super.setUpService(service);
    }

}

package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.framework.services.DataEditorService_DataTestCase;
import gov.epa.emissions.framework.services.editor.DataEditorServiceImpl;

public class DataViewServiceImplTest extends DataEditorService_DataTestCase {

    protected void doSetUp() throws Exception {
        HibernateSessionFactory sessionFactory = new HibernateSessionFactory(sessionFactory());
        DataEditorServiceImpl service = new DataEditorServiceImpl(emf(), super.dbServer(), sessionFactory);

        super.setUpService(service);
    }

}

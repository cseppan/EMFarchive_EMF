package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.framework.services.DataEditorService_VersionsTestCase;
import gov.epa.emissions.framework.services.UserService;
import gov.epa.emissions.framework.services.editor.DataEditorServiceImpl;

public class DataEditorServiceImpl_VersionsTest extends DataEditorService_VersionsTestCase{

    protected void doSetUp() throws Exception {
        HibernateSessionFactory sessionFactory = new HibernateSessionFactory(sessionFactory());
        DataEditorServiceImpl service = new DataEditorServiceImpl(emf(), super.dbServer(),  sessionFactory);
        UserService userService = new UserServiceImpl(sessionFactory);

        super.setUpService(service, userService);
    }

}

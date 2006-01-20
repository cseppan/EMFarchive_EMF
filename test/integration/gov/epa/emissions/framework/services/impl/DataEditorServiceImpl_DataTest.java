package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.framework.services.DataEditorService_DataTestCase;
import gov.epa.emissions.framework.services.UserService;
import gov.epa.emissions.framework.services.editor.DataEditorServiceImpl;

public class DataEditorServiceImpl_DataTest extends DataEditorService_DataTestCase {

    protected void doSetUp() throws Exception {
        DataEditorServiceImpl service = new DataEditorServiceImpl(emf(), super.dbServer(), sessionFactory());
        UserService userService = new UserServiceImpl(sessionFactory());

        super.setUpService(service, userService);
    }

}

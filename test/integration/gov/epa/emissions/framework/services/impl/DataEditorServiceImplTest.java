package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.framework.client.data.DataEditorServiceTestCase;

public class DataEditorServiceImplTest extends DataEditorServiceTestCase {

    protected void setUp() throws Exception {
        super.setUp();
        super.setUpService(new DataEditorServiceImpl(super.dbServer()));
    }

}

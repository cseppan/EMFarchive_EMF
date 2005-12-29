package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.framework.services.DataEditorServiceTestCase;
import gov.epa.emissions.framework.services.editor.DataEditorServiceImpl;

public class DataEditorServiceImplTest extends DataEditorServiceTestCase {

    protected void doSetUp() throws Exception {
        DataEditorServiceImpl service = new DataEditorServiceImpl(super.dbServer());
        super.setUpService(service);
    }

}

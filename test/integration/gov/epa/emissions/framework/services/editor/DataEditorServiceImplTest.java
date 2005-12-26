package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.framework.services.DataEditorServiceTestCase;

public class DataEditorServiceImplTest extends DataEditorServiceTestCase {

    protected void doSetUp() throws Exception {
        DataEditorServiceImpl service = new DataEditorServiceImpl(super.dbServer());
        super.setUpService(service);
    }

}

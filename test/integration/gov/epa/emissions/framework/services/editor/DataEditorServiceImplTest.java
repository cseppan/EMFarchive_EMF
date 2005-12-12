package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.framework.services.DataEditorServiceTestCase;
import gov.epa.emissions.framework.services.editor.DataEditorServiceImpl;

public class DataEditorServiceImplTest extends DataEditorServiceTestCase {

    protected void setUp() throws Exception {
        super.setUp();
        super.setUpService(new DataEditorServiceImpl(super.dbServer()));
    }

}

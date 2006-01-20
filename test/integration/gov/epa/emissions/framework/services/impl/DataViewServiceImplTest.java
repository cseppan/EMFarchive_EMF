package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.framework.services.DataViewServiceTestCase;
import gov.epa.emissions.framework.services.editor.DataViewServiceImpl;

public class DataViewServiceImplTest extends DataViewServiceTestCase {

    protected void doSetUp() throws Exception {
        DataViewServiceImpl service = new DataViewServiceImpl(emf(), super.dbServer(), sessionFactory());

        super.setUpService(service);
    }

}

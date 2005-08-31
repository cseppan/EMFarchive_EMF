package gov.epa.emissions.commons.io.importer.orl;

import gov.epa.emissions.commons.io.importer.CommonsTestCase;
import gov.epa.emissions.commons.io.importer.ReferenceTablesCreator;

public class ReferenceTablesCreatorTest extends CommonsTestCase {

    public void testCreateAddtionalTablesUsingMysql() throws Exception {
        ReferenceTablesCreator tables = new ReferenceTablesCreator(referenceFilesDir, dbSetup.getDbServer().getTypeMapper());
        tables.createAdditionRefTables(dbSetup.getDbServer().getReferenceDatasource());
    }

}

package gov.epa.emissions.commons.io.importer.orl;

import gov.epa.emissions.commons.io.importer.ReferenceTables;

public class ReferenceTablesTest extends EmissionsDataSetupTestCase {

    public void testCreateAddtionalTablesUsingMysql() throws Exception {
        ReferenceTables tables = new ReferenceTables(referenceFilesDir, dbSetup.getDbServer().getTypeMapper());
        tables.createAdditionRefTables(dbSetup.getDbServer().getReferenceDatasource());
    }

}

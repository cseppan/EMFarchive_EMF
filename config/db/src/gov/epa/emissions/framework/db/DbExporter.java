package gov.epa.emissions.framework.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;

import org.dbunit.Assertion;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSet;

public class DbExporter extends EmfDatabaseTestCase {

    public void XtestExportEntireDatabase() throws Exception {
        IDataSet fullDataSet = connection.createDataSet();
        FlatXmlDataSet.write(fullDataSet, new FileOutputStream("config/db/sql/full.xml"));
    }

    public void testExportEmfSchema() throws Exception {
        IDataSet schema = connection.createDataSet(new String[] { "users", "datasettypes" });
        FlatXmlDataSet.write(schema, new FileOutputStream("config/db/sql/emf.xml"));
    }

    public void testExportUsers() throws Exception {
        String tablename = "users";
        File file = new File("config/db/sql/users.xml");

        doExport(tablename, file);
        assertExportedFileSameAsDatabase(tablename, file);
    }

    public void testExportDatasetTypes() throws Exception {
        String tablename = "datasettypes";
        File file = new File("config/db/sql/datasettypes.xml");

        doExport(tablename, file);
        assertExportedFileSameAsDatabase(tablename, file);
    }

    private void doExport(String tablename, File file) throws SQLException, IOException, DataSetException,
            FileNotFoundException {
        QueryDataSet dataset = new QueryDataSet(connection);
        dataset.addTable(tablename, "SELECT * FROM " + tablename);
        FlatXmlDataSet.write(dataset, new FileOutputStream(file));
    }

    private void assertExportedFileSameAsDatabase(String tablename, File file) throws SQLException, Exception,
            DataSetException, IOException, DatabaseUnitException {
        IDataSet databaseDataSet = getConnection().createDataSet();
        ITable actualUsersTable = databaseDataSet.getTable(tablename);

        IDataSet expectedDataSet = new FlatXmlDataSet(file);
        ITable expectedTable = expectedDataSet.getTable(tablename);

        Assertion.assertEquals(expectedTable, actualUsersTable);
    }
}

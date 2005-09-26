package gov.epa.emissions.framework.db;

import java.sql.SQLException;

import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;

public class ExImDbUpdate extends DbUpdate {

    public ExImDbUpdate() throws Exception {
        super();
    }

    public void deleteDatasetAccessLogs(String datasetName) throws Exception {
        String accessLogId = getAccessLogId(datasetName);

        QueryDataSet dataset = new QueryDataSet(connection);
        dataset.addTable("dataset_access_logs", "SELECT * from dataset_access_logs WHERE access_log_id = "
                + accessLogId);

        super.doDelete(dataset);
    }

    private String getAccessLogId(String datasetName) throws SQLException, DataSetException {
        QueryDataSet dataset = new QueryDataSet(connection);
        dataset.addTable("access_logs", "SELECT access_log_id from datasets d, dataset_access_logs l WHERE  d.name='"
                + datasetName + "' AND l.dataset_id=d.dataset_id");

        ITableIterator iter = dataset.iterator();
        iter.next();
        ITable tableObj = iter.getTable();

        Integer result = (Integer) tableObj.getValue(0, "access_log_id");

        return result.toString();
    }

    public void deleteDataset(String datasetName) throws Exception {
        deleteDatasetAccessLogs(datasetName);
        super.delete("datasets", "name", datasetName);
    }

}

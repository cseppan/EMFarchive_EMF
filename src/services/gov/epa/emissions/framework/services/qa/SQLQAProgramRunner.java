package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.TableCreator;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.QAStep;

import java.util.Date;

public class SQLQAProgramRunner implements QAProgramRunner {

    private DbServer dbServer;

    private QAStep qaStep;

    private TableCreator tableCreator;

    public SQLQAProgramRunner(DbServer dbServer, QAStep qaStep) {
        this.dbServer = dbServer;
        this.qaStep = qaStep;
        this.tableCreator = new TableCreator(dbServer.getEmissionsDatasource());
    }

    public void run() throws EmfException {
        String programArguments = qaStep.getProgramArguments();
        if (programArguments == null || programArguments.trim().length() == 0) {
            throw new EmfException("Please specify the sql query");
        }
        String tableName = tableName(qaStep);
        String query = query(qaStep, tableName);
        try {
            dropTable(tableName);
            dbServer.getEmissionsDatasource().query().execute(query);
            success(qaStep, tableName);
        } catch (Exception e) {
            failure(qaStep);
            throw new EmfException("Check the query - " + query);
        }
    }

    private void dropTable(String tableName) throws EmfException {
        try {
            if (tableCreator.exists(tableName)) {
                tableCreator.drop(tableName);
            }
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    private void success(QAStep qaStep, String tableName) {
        qaStep.setTableCreationDate(new Date());
        qaStep.setTableCreationStatus("Success");

        InternalSource source = new InternalSource();
        source.setTable(tableName);
        qaStep.setTableSource(source);
    }

    private void failure(QAStep qaStep) {
        qaStep.setTableCreationStatus("Failed");
        qaStep.setTableSource(new InternalSource());
    }

    private String query(QAStep qaStep, String tableName) {
        return "CREATE TABLE " + qualifiedName(dbServer.getEmissionsDatasource(), tableName) + " AS "
                + qaStep.getProgramArguments();
    }

    private String qualifiedName(Datasource datasource, String tableName) {
        return datasource.getName() + "." + tableName;
    }

    private String tableName(QAStep qaStep) {
        String result = qaStep.getName() + "_" + qaStep.getDatasetId();

        for (int i = 0; i < result.length(); i++) {
            if (!Character.isJavaLetterOrDigit(result.charAt(i))) {
                result = result.replace(result.charAt(i), '_');
            }
        }

        if (Character.isDigit(result.charAt(0))) {
            result = result.replace(result.charAt(0), '_');
            result = "QA" + result;
        }
        return result.trim().replaceAll(" ", "_");
    }

}

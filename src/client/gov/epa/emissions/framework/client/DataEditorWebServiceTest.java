package gov.epa.emissions.framework.client;

import gov.epa.emissions.commons.io.ColumnMetaData;
import gov.epa.emissions.commons.io.TableMetaData;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.services.DataEditorService;

public class DataEditorWebServiceTest {
    private static final String DEFAULT_URL = "http://localhost:8080/emf/services";// default

    private DataEditorService des = null;

    public DataEditorWebServiceTest() throws Exception {
        super();
        RemoteServiceLocator rl = new RemoteServiceLocator(DEFAULT_URL);
        des = rl.dataEditorService();
        getTableMetaData();
    }

    private void getTableMetaData() {
        try {
            TableMetaData tmd = des.getTableMetaData("conrad");
            
            String name = tmd.getTable();
            System.out.println(name);
            
            ColumnMetaData[] cmds = tmd.getCols();
            System.out.println(cmds.length);
        } catch (EmfException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            new DataEditorWebServiceTest();
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

}

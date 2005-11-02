import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dataset.EMFStatusService;
import dataset.Status;

/*
 * Created on Aug 1, 2005
 *
 * Eclipse Project Name: Hib
 * Package: 
 * File Name: HibClient.java
 * Author: Conrad F. D'Cruz
 */

/**
 * @author Conrad F. D'Cruz
 *
 */
public class HibClient {

    private static Log log = LogFactory.getLog(HibClient.class);

    /**
     * 
     */
    public HibClient() {
        super();
        log.info("IN CONSTRUCTOR");
        //callServiceForGet();
        //callServiceForInsert();
        callDatasetTypes();
        //insertDSTS();
        log.info("END CONSTRUCTOR");

    }

    /**
     * 
     */
    private void insertDSTS() {
        ExImServicesImpl eximsvc = new ExImServicesImpl();
//        DatasetType dst1 = new DatasetType();
//        dst1.setDescription("ORL Nonpoint Inventory");
//        dst1.setName("ORL Nonpoint Inventory");
//        dst1.setMaxfiles(1);
//        dst1.setMinfiles(1);
//        eximsvc.insertDatasetType(dst1);
//
//        DatasetType dst2 = new DatasetType();
//        dst2.setDescription("ORL Nonroad Inventory");
//        dst2.setName("ORL Nonroad Inventory");
//        dst2.setMaxfiles(1);
//        dst2.setMinfiles(1);
//        eximsvc.insertDatasetType(dst2);

//      DatasetType dst3 = new DatasetType();
//      dst3.setDescription("ORL Onroad Inventory");
//      dst3.setName("ORL Onroad Inventory");
//      dst3.setMaxfiles(1);
//      dst3.setMinfiles(1);
//      eximsvc.insertDatasetType(dst3);
//
//      DatasetType dst4 = new DatasetType();
//      dst4.setDescription("ORL Point Inventory");
//      dst4.setName("ORL Point Inventory");
//      dst4.setMaxfiles(1);
//      dst4.setMinfiles(1);
//      eximsvc.insertDatasetType(dst4);


        
        
        
    }

    /**
     * 
     */
    private void callDatasetTypes() {
        
        ExImServicesImpl eximsvc = new ExImServicesImpl();
        try {
            DatasetType[] datasettypes = eximsvc.getDatasetTypes();
            System.out.println("Total number of datasettypes: " + datasettypes.length);
            
            for (int i=0; i<datasettypes.length; i++){
                DatasetType dst = datasettypes[i];
                //System.out.println("" + aStat.getStatusid());
                System.out.println("" + dst.getId());
                System.out.println("" + dst.getName());
                System.out.println("" + dst.getDescription());
                System.out.println("" + dst.getMinfiles());
                System.out.println("" + dst.getMaxfiles());
                
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

    /**
     * 
     */
    private void callServiceForInsert() {
        Status aStat = new Status();
        aStat.setMessage("import started for file XYZABC");
        aStat.setMsgType("INFOMATICS");
        aStat.setTimestamp(new Date());
        aStat.setUserName("cdcruz");
        EMFStatusService emfStatusSvc = new EMFStatusService();
        System.out.println("HibClient: Before call to setStatus");
        emfStatusSvc.setStatus(aStat);
        System.out.println("HibClient: After call to setStatus");

    }

    /**
     * 
     */
    private void callServiceForGet() {
        EMFStatusService emfStatusSvc = new EMFStatusService();
        try {
            Status[] stats = emfStatusSvc.getMessages("cdcruz");
            System.out.println("Total number of status messages: " + stats.length);
            
            for (int i=0; i<stats.length; i++){
                Status aStat = stats[i];
                //System.out.println("" + aStat.getStatusid());
                System.out.println("" + aStat.getUserName());
                System.out.println("" + aStat.getTimestamp());
                System.out.println("" + aStat.getMsgType());
                System.out.println("" + aStat.getMessage());
                System.out.println("" + aStat.isMsgRead());
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new HibClient();
    }
}

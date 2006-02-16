package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Exporter;
import gov.epa.emissions.framework.services.impl.DataCommonsServiceImpl;
import gov.epa.emissions.framework.services.impl.HibernateSessionFactory;
import gov.epa.emissions.framework.services.impl.VersionedExporterFactory;

public class ExImFactoryTestCase extends ServicesTestCase {
    private DataCommonsService service;

    // private VersionedImporterFactory importerFactory;
    private VersionedExporterFactory exporterFactory;

    private EmfDataset dataset;

    // private String fileName="a.txt";
    // private File path = new File("..\\EMF\\test\\data");
    DatasetType[] allDatasetTypes;

    protected void doSetUp() throws Exception {
        // ServiceLocator serviceLocator = serviceLocator();
        HibernateSessionFactory sessionFactory = sessionFactory();
        service = new DataCommonsServiceImpl(sessionFactory);

        allDatasetTypes = service.getDatasetTypes();

        // importerFactory = new VersionedImporterFactory(dbServer(), sqlDataTypes());
        exporterFactory = new VersionedExporterFactory(dbServer(), sqlDataTypes());
        dataset = new EmfDataset();
    }

    protected void doTearDown() throws Exception {// no op
        super.tearDown();
    }

    public void testShouldCreateAllExportersFromDatasetTypesTable() throws Exception {

        try {
            for (int i = 0; i < allDatasetTypes.length; i++) {
                DatasetType dst = allDatasetTypes[i];
//                Class dstClass = Class.forName(dst.getImporterClassName());
                dataset.setDatasetType(dst);

                if (!ignore(dst)) {
                    Exporter exporter = exporterFactory.create(dataset, 0);
                    assertTrue("Importer cannot be null ", exporter != null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    // public void testShouldCreateAllImportersFromDatasetTypesTable() throws EmfException{
    //        
    // try {
    // System.out.println("Total number of datasettypes= " + allDatasetTypes.length);
    // for (int i = 0; i < allDatasetTypes.length; i++) {
    // DatasetType dst = allDatasetTypes[i];
    // //Class dstClass = Class.forName(dst.getImporterClassName());
    // dataset.setDatasetType(dst);
    //                
    // System.out.println("#" + i + ":" + dst.getName());
    // if (!ignore(dst)){
    // Importer importer = importerFactory.create(dataset, path, fileName);
    // assertTrue("Importer cannot be null ", importer!=null);
    // }
    // }
    // } catch (ImporterException e) {
    // e.printStackTrace();
    // throw new EmfException("importer factory failed to create importer. Reason: Importer Exception");
    // }
    //
    // }

    /*
     * This method is a hack because these importers need files present to be created
     */
    private boolean ignore(DatasetType dst) {
        boolean ignoreit = false;

        if (dst.getName().equals("Comma Separated Values (CSV)"))
            ignoreit = true;
        if (dst.getName().equals("External File (External)"))
            ignoreit = true;
        if (dst.getName().equals("IDA Mobile"))
            ignoreit = true;
        if (dst.getName().equals("IDA Nonpoint/Nonroad"))
            ignoreit = true;
        if (dst.getName().equals("IDA Point"))
            ignoreit = true;
        if (dst.getName().equals("Meteorology File (External)"))
            ignoreit = true;
        if (dst.getName().equals("Model Ready Emissions File"))
            ignoreit = true;
        if (dst.getName().equals("NIF3.0 Nonpoint Inventory"))
            ignoreit = true;
        if (dst.getName().equals("NIF3.0 Nonroad Inventory"))
            ignoreit = true;
        if (dst.getName().equals("NIF3.0 Onroad Inventory"))
            ignoreit = true;
        if (dst.getName().equals("NIF3.0 Point Inventory"))
            ignoreit = true;
        if (dst.getName().equals("ORL Nonpoint Inventory (ARINV)"))
            ignoreit = true;
        if (dst.getName().equals("ORL Nonroad Inventory (ARINV)"))
            ignoreit = true;
        if (dst.getName().equals("ORL Onroad Inventory (MBINV)"))
            ignoreit = true;
        if (dst.getName().equals("ORL Point Inventory (PTINV)"))
            ignoreit = true;
        if (dst.getName().equals("SMOKE Report"))
            ignoreit = true;
        if (dst.getName().equals("Shapefile (External)"))
            ignoreit = true;
        if (dst.getName().equals("Shapefile Catalog (CSV)"))
            ignoreit = true;
        if (dst.getName().equals("Surrogate Code Mapping (CSV)"))
            ignoreit = true;
        if (dst.getName().equals("Surrogate Specifications (CSV)"))
            ignoreit = true;
        if (dst.getName().equals("Surrogate Tool Control Variables (CSV)"))
            ignoreit = true;
        if (dst.getName().equals("Surrogate Tool Generation Controls (CSV)"))
            ignoreit = true;
        if (dst.getName().equals("Text file (Line-based)"))
            ignoreit = true;

        return ignoreit;
    }

}

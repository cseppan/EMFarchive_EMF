package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.io.XFileFormat;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;

import java.util.Date;
import java.util.HashMap;

public class NewDatasetTypePresenter {
    
    private final String EXTERNAL = "External File";
    
    private final String CSV = "CSV File";
    
    private final String LINE_BASED = "Line-based";
    
    private final String SMOKE = "SMOKE Report File";
    
    private final String FLEXIBLE = "Flexible File Format";
    
    private final String EXTERNAL_IMPORTER = "gov.epa.emissions.commons.io.external.ExternalFilesImporter";
    
    private final String CSV_IMPORTER = "gov.epa.emissions.commons.io.csv.CSVImporter";
    
    private final String LINE_IMPORTER = "gov.epa.emissions.commons.io.generic.LineImporter";
    
    private final String SMOKE_IMPORTER = "gov.epa.emissions.commons.io.other.SMKReportImporter";
    
    private final String FLEXIBLE_IMPORTER = "gov.epa.emissions.commons.io.orl.NewORLImporter";
    
    private final String EXTERNAL_EXPORTER = "gov.epa.emissions.commons.io.external.ExternalFilesExporter";
    
    private final String CSV_EXPORTER = "gov.epa.emissions.commons.io.csv.CSVExporter";
    
    private final String LINE_EXPORTER = "gov.epa.emissions.commons.io.generic.LineExporter";
    
    private final String SMOKE_EXPORTER = "gov.epa.emissions.commons.io.other.SMKReportExporter";
    
    private final String FLEXIBLE_EXPORTER = "gov.epa.emissions.commons.io.orl.NewORLExporter";
    
    private NewDatasetTypeView view;

    private EmfSession session;

    private HashMap<String, String> mapImport, mapExport;

    public NewDatasetTypePresenter(EmfSession session, NewDatasetTypeView view) {
        this.session = session;
        this.view = view;
        mapImport = new HashMap<String, String>();
        mapExport = new HashMap<String, String>();
        setMap();
    }

    private void setMap() {
        mapImport.put(EXTERNAL, EXTERNAL_IMPORTER);
        mapImport.put(CSV, CSV_IMPORTER);
        mapImport.put(LINE_BASED, LINE_IMPORTER);
        mapImport.put(SMOKE, SMOKE_IMPORTER);
        mapImport.put(FLEXIBLE, FLEXIBLE_IMPORTER);
        
        mapExport.put(EXTERNAL, EXTERNAL_EXPORTER);
        mapExport.put(CSV, CSV_EXPORTER);
        mapExport.put(LINE_BASED, LINE_EXPORTER);
        mapExport.put(SMOKE, SMOKE_EXPORTER);
        mapExport.put(FLEXIBLE, FLEXIBLE_EXPORTER);
    }

    public void doDisplay() {
        view.observe(this);
        view.display();
    }

    public void doClose() {
        closeView();
    }

    private void closeView() {
        view.disposeView();
    }

    public void doSave(String name, String desc, String minfiles, String maxfiles, String type, XFileFormat fileFormat, String formatFile) throws EmfException {
        if (type.equals(FLEXIBLE))
            saveTypeWithFileFormat(name, desc, type, fileFormat, formatFile, new Date());
        else {
            DatasetType newType = setNewDatasetType(name, desc, minfiles, maxfiles, type, new Date());
            service().addDatasetType(newType);
        }
        
        closeView();
    }

    private DatasetType setNewDatasetType(String name, String desc, String minfiles, String maxfiles, 
            String type, Date date) {
        DatasetType newType = new DatasetType(name);
        newType.setDescription(desc);
        newType.setMinFiles(Integer.parseInt(minfiles));
        newType.setMaxFiles(Integer.parseInt(maxfiles));
        newType.setDefaultSortOrder("");
        if (type.equalsIgnoreCase(LINE_BASED))
            newType.setDefaultSortOrder("Line_Number");
        newType.setImporterClassName(mapImport.get(type));
        newType.setExporterClassName(mapExport.get(type));
        newType.setExternal(type.equalsIgnoreCase(EXTERNAL));
        
        newType.setCreationDate(date);
        newType.setLastModifiedDate(date);
        newType.setCreator(session.user());

        return newType;
    }
    
    private void saveTypeWithFileFormat(String name, String desc, String type, 
            XFileFormat fileFormat, String formatFile, Date date) throws EmfException {
        DatasetType newType = setNewDatasetType(name, desc, 1+"", 1+"", type, date);
        newType.setTablePerDataset(1);
        fileFormat.setCreator(session.user());
        fileFormat.setLastModifiedDate(date);
        fileFormat.setDateAdded(date);
        
        service().addDatasetTypeWithFileFormat(newType, fileFormat, formatFile);
    }

    private DataCommonsService service() {
        return session.dataCommonsService();
    }

}

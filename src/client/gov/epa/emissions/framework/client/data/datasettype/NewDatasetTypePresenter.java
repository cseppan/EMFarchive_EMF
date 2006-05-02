package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;

import java.util.HashMap;

public class NewDatasetTypePresenter {
    private NewDatasetTypeView view;

    private EmfSession session;

    private HashMap mapImport, mapExport;

    public NewDatasetTypePresenter(EmfSession session, NewDatasetTypeView view) {
        this.session = session;
        this.view = view;
        mapImport = new HashMap();
        mapExport = new HashMap();
        setMap();
    }

    private void setMap() {
        mapImport.put("External File", "gov.epa.emissions.commons.io.external.ExternalFilesImporter");
        mapImport.put("CSV File", "gov.epa.emissions.commons.io.csv.CSVImporter");
        mapImport.put("Line-based File", "gov.epa.emissions.commons.io.generic.LineImporter");
        mapImport.put("SMOKE Report File", "gov.epa.emissions.commons.io.other.SMKReportImporter");
        mapExport.put("External File", "");
        mapExport.put("CSV File", "gov.epa.emissions.commons.io.csv.CSVExporter");
        mapExport.put("Line-based File", "gov.epa.emissions.commons.io.generic.LineExporter");
        mapExport.put("SMOKE Report File", "gov.epa.emissions.commons.io.other.SMKReportExporter");
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

    public void doSave(String name, String minfiles, String maxfiles, String type) throws EmfException {
        DatasetType newType = setNewDatasetType(name, minfiles, maxfiles, type);
        service().addDatasetType(newType);
        closeView();
    }

    private DatasetType setNewDatasetType(String name, String minfiles, String maxfiles, String type) {
        DatasetType newType = new DatasetType(name);
        newType.setDescription("");
        newType.setMinFiles(Integer.parseInt(minfiles));
        newType.setMaxFiles(Integer.parseInt(maxfiles));
        newType.setDefaultSortOrder("");
        if (type.equalsIgnoreCase("Line-based File"))
            newType.setDefaultSortOrder("Line_Number");
        newType.setImporterClassName((String) mapImport.get(type));
        newType.setExporterClassName((String) mapExport.get(type));
        newType.setExternal(type.equalsIgnoreCase("External File"));

        return newType;
    }

    private DataCommonsService service() {
        return session.dataCommonsService();
    }

}

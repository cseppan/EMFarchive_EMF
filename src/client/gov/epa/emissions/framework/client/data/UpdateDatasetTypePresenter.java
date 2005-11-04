package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DatasetTypesServices;
import gov.epa.emissions.framework.services.InterDataServices;

import java.util.Set;
import java.util.TreeSet;

public class UpdateDatasetTypePresenter {

    private UpdateDatasetTypeView view;

    private DatasetType type;

    private DatasetTypesServices datasetTypesServices;

    private InterDataServices interdataServices;

    public UpdateDatasetTypePresenter(UpdateDatasetTypeView view, DatasetType type,
            DatasetTypesServices datasetTypesServices, InterDataServices interdataServices) {
        this.view = view;
        this.type = type;
        this.datasetTypesServices = datasetTypesServices;
        this.interdataServices = interdataServices;
    }

    public void doDisplay() throws EmfException {
        view.observe(this);
        view.display(type, interdataServices.getKeywords());
    }

    public void doClose() {
        view.close();
    }

    public void doSave(String name, String description, Keyword[] keywords, DatasetTypesManagerView manager)
            throws EmfException {
        update(name, description, keywords);
        datasetTypesServices.updateDatasetType(type);

        manager.refresh();
        doClose();
    }

    private void update(String name, String description, Keyword[] keywords) throws EmfException {
        type.setName(name);
        type.setDescription(description);

        verifyDuplicates(keywords);
        type.setKeywords(keywords);
    }

    private void verifyDuplicates(Keyword[] keywords) throws EmfException {
        Set set = new TreeSet();
        for (int i = 0; i < keywords.length; i++) {
            String name = keywords[i].getName();
            if (!set.add(name))
                throw new EmfException("Duplicate keyword: '" + name + "' not allowed");
        }
    }
}

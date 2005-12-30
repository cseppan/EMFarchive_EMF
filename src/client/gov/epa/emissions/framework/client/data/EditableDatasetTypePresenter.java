package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DatasetTypeService;
import gov.epa.emissions.framework.services.DataCommonsService;

import java.util.Set;
import java.util.TreeSet;

public class EditableDatasetTypePresenter {

    private EditableDatasetTypeView view;

    private DatasetType type;

    private DatasetTypeService datasetTypesService;

    private DataCommonsService dataCommonsService;

    public EditableDatasetTypePresenter(EditableDatasetTypeView view, DatasetType type,
            DatasetTypeService datasetTypesService, DataCommonsService dataCommonsService) {
        this.view = view;
        this.type = type;
        this.datasetTypesService = datasetTypesService;
        this.dataCommonsService = dataCommonsService;
    }

    public void doDisplay() throws EmfException {
        view.observe(this);
        view.display(type, dataCommonsService.getKeywords());
    }

    public void doClose() {
        view.close();
    }

    public void doSave(String name, String description, Keyword[] keywords, DatasetTypesManagerView manager)
            throws EmfException {
        update(name, description, keywords);
        datasetTypesService.updateDatasetType(type);

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
                throw new EmfException("duplicate keyword: '" + name + "'");
        }
    }
}

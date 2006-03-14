package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.util.Set;
import java.util.TreeSet;

public class EditableKeywordsTabPresenterImpl implements EditableKeywordsTabPresenter {

    private EditableKeywordsTabView view;

    private EmfDataset dataset;

    public EditableKeywordsTabPresenterImpl(EditableKeywordsTabView view, EmfDataset dataset) {
        this.view = view;
        this.dataset = dataset;
    }

    public void display(Keywords masterKeywords) {
        view.display(dataset, masterKeywords);
    }

    public void doSave() throws EmfException {
        KeyVal[] updates = view.updates();
        verifyDuplicates(updates);

        dataset.setKeyVals(updates);
    }

    private void verifyDuplicates(KeyVal[] updates) throws EmfException {
        Set set = new TreeSet();
        for (int i = 0; i < updates.length; i++) {
            String name = updates[i].getKeyword().getName();
            if (!set.add(name))
                throw new EmfException("duplicate keyword '" + name + "'");
        }
    }

}

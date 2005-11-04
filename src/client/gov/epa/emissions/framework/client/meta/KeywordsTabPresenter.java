package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.EmfDataset;

import java.util.Set;
import java.util.TreeSet;

public class KeywordsTabPresenter {

    private KeywordsTabView view;

    private EmfDataset dataset;

    public KeywordsTabPresenter(KeywordsTabView view, EmfDataset dataset) {
        this.view = view;
        this.dataset = dataset;
    }

    public void init(Keyword[] keywords) {
        view.display(dataset.getKeyVals(), keywords);
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
                throw new EmfException("Duplicate keyword: '" + name + "' not allowed");
        }
    }

}

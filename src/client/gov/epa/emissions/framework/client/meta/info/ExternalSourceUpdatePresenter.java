package gov.epa.emissions.framework.client.meta.info;

import gov.epa.emissions.commons.data.ExternalSource;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.keywords.Keywords;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExternalSourceUpdatePresenter {

    private ExternalSourceUpdateView view;

    private EmfDataset dataset;

    private EmfSession session;
    
    private InfoTabPresenter sourceTabPresenter;

    private static String lastFolder = null;

    public ExternalSourceUpdatePresenter(InfoTabPresenter sourceTabPresenter) {
        this.dataset = sourceTabPresenter.getDataset();
        this.session = sourceTabPresenter.getSession();
        this.sourceTabPresenter = sourceTabPresenter;
    }

    public void notifyDone() {
        view.disposeView();
    }

    public void display(ExternalSourceUpdateView view) {
        this.view = view;
        view.observe(this);
        view.setMostRecentUsedFolder(getFolder());

        view.display();
    }

    private String getFolder() {
        return (lastFolder != null) ? lastFolder : getDefaultFolder();
    }

    public void update(String folder, String purpose, boolean isWorkLoc, boolean isMassLoc) throws EmfException {
        if (!isWorkLoc && !isMassLoc)
            throw new EmfException("Selected folder must be either a work location or a mass storage location.");
        
        KeyVal[] keys = dataset.getKeyVals();
        int workLoc = -1;
        int massLoc = -1;
        Keywords keywords = new Keywords(session.dataCommonsService().getKeywords());
        Keyword massLocKeyword = keywords.get("MASS_STORE_LOCATION");
        Keyword workLocKeyword = keywords.get("WORK_LOCATION");

        for (int i = 0; i < keys.length; i++) {
            if (keys[i].getKeyword().equals(workLocKeyword))
                workLoc = i;

            if (keys[i].getKeyword().equals(massLocKeyword))
                massLoc = i;
        }

        if (massLoc == -1 && workLoc == -1) {
            if (isWorkLoc)
                dataset.addKeyVal(new KeyVal(workLocKeyword, folder));

            if (isMassLoc)
                dataset.addKeyVal(new KeyVal(massLocKeyword, folder));
        }

        if (massLoc != -1 && workLoc != -1) {
            if (isMassLoc)
                keys[massLoc].setValue(folder);

            if (isWorkLoc)
                keys[workLoc].setValue(folder);

            dataset.setKeyVals(keys);
        }
        
        if (massLoc == -1 && workLoc != -1) {
            if (isWorkLoc)
                keys[workLoc].setValue(folder);
            
            if (!isMassLoc) {
                dataset.setKeyVals(keys);
                return;
            }
            
            List<KeyVal> all = new ArrayList<KeyVal>();
            all.addAll(Arrays.asList(keys));
            all.add(new KeyVal(massLocKeyword, folder));
            
            dataset.setKeyVals(all.toArray(new KeyVal[0]));
                
        }
        
        if (massLoc != -1 && workLoc == -1) {
            if (isMassLoc)
                keys[massLoc].setValue(folder);
            
            if (!isWorkLoc) {
                dataset.setKeyVals(keys);
                return;
            }
            
            List<KeyVal> all = new ArrayList<KeyVal>();
            all.addAll(Arrays.asList(keys));
            all.add(new KeyVal(workLocKeyword, folder));
            
            dataset.setKeyVals(all.toArray(new KeyVal[0]));
        }
        
        updateDatasetSource();

    }

    private String getDefaultFolder() {
        ExternalSource[] sources = dataset.getExternalSources();

        return new File(sources[0].getDatasource()).getParent();
    }
    
    public void updateDatasetSource() {
        this.sourceTabPresenter.updateKeyVals(dataset.getKeyVals());
    }
}

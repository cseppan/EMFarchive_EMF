package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.framework.client.meta.PropertiesEditorTabPresenter;
import gov.epa.emissions.framework.services.EmfException;

public interface EditableKeywordsTabPresenter extends PropertiesEditorTabPresenter {

    void display(Keywords masterKeywords);
    
    void refreshView();
    
    void refresh() throws EmfException;

}
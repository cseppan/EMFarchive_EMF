package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.DataCommonsService;

import java.util.Set;
import java.util.TreeSet;

public class EditableDatasetTypePresenterImpl implements EditableDatasetTypePresenter {

    private EditableDatasetTypeView editable;

    private DatasetType type;

    private EmfSession session;

    private ViewableDatasetTypeView viewable;

    public EditableDatasetTypePresenterImpl(EmfSession session, EditableDatasetTypeView editable,
            ViewableDatasetTypeView viewable, DatasetType type) {
        this.session = session;
        this.editable = editable;
        this.viewable = viewable;
        this.type = type;
    }

    public void doDisplay() throws EmfException {
        type = service().obtainLockedDatasetType(session.user(), type);

        if (!type.isLocked(session.user())) {// view mode, locked by another user
            new ViewableDatasetTypePresenterImpl(viewable, type).doDisplay();
            return;
        }
        
        editable.observe(this);
        editable.display(type, dataCommonsService().getKeywords());
    }

    private DataCommonsService service() {
        return session.dataCommonsService();
    }

    private DataCommonsService dataCommonsService() {
        return session.dataCommonsService();
    }

    public void doClose() throws EmfException {
        service().releaseLockedDatasetType(session.user(), type);
        closeView();
    }

    private void closeView() {
        editable.close();
    }

    public void doSave(String name, String description, Keyword[] keywords, DatasetTypesManagerView manager)
            throws EmfException {
        update(name, description, keywords);
        type = service().updateDatasetType(type);

        manager.refresh();
        closeView();
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

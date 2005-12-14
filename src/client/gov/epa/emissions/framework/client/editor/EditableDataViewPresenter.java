package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.EditToken;

public class EditableDataViewPresenter {

    private EditableDataView view;

    private DataEditorService service;

    private Version version;

    private String table;

    private EditToken token;

    public EditableDataViewPresenter(Version version, String table, EditableDataView view, DataEditorService service) {
        this.version = version;
        this.table = table;
        token = new EditToken(version, table);
        this.view = view;
        this.service = service;
    }

    public void display() {
        view.observe(this);
        view.display(version, table, service);
    }

    public void doClose() throws EmfException {
        service.close();
        view.close();
    }

    public void doDiscard() throws EmfException {
        service.discard(token);
    }

    public void doSave() throws EmfException {
        service.save(token);
    }

}

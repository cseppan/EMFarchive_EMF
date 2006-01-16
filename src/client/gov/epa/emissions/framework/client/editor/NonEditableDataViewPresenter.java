package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.EditToken;

public class NonEditableDataViewPresenter {

    private NonEditableDataView view;

    private DataEditorService service;

    private Version version;

    private String table;

    private EditToken token;

    public NonEditableDataViewPresenter(Version version, String table, NonEditableDataView view,
            DataEditorService service) {
        this.version = version;
        this.table = table;
        this.view = view;
        this.service = service;

        token = new EditToken(version, table);
    }

    public void display() throws EmfException {
        service.openSession(token);
        view.observe(this);
        view.display(version, table, service);
    }

    public void doClose() throws EmfException {
        service.closeSession(token);
        view.close();
    }

}

package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.editor.DataView;
import gov.epa.emissions.framework.client.editor.DataViewPresenter;
import gov.epa.emissions.framework.client.editor.EditableDataView;
import gov.epa.emissions.framework.client.editor.EditableDataViewPresenter;
import gov.epa.emissions.framework.services.DataEditorService;

public class VersionsPresenter {

    private DataEditorService service;

    private VersionsView view;

    public VersionsPresenter(DataEditorService service) {
        this.service = service;
    }

    public void observe(VersionsView view) {
        this.view = view;
        view.observe(this);
    }

    public void doNew(Version base, String name) throws EmfException {
        Version derived = service.derive(base, name);
        view.add(derived);
    }

    public void doView(Version version, String table, DataView view) throws EmfException {
        DataViewPresenter presenter = new DataViewPresenter(version, table, view, service);
        presenter.display();
    }

    public void doEdit(Version version, String table, EditableDataView view) throws EmfException {
        EditableDataViewPresenter presenter = new EditableDataViewPresenter(version, table, view, service);
        presenter.display();
    }

    public void doMarkFinal(Version[] versions) throws EmfException {
        for (int i = 0; i < versions.length; i++) {
            if (versions[i].isFinalVersion())
                throw new EmfException("Version: " + versions[i].getVersion()
                        + " is already Final. It should be non-final.");
            service.markFinal(versions[i]);
        }
    }

}

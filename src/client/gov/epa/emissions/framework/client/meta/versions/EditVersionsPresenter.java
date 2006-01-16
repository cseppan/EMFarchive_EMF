package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.editor.NonEditableDataView;
import gov.epa.emissions.framework.client.editor.NonEditableDataViewPresenter;
import gov.epa.emissions.framework.client.editor.EditableDataView;
import gov.epa.emissions.framework.client.editor.EditableDataViewPresenter;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.EmfDataset;

public class EditVersionsPresenter {

    private DataEditorService service;

    private EditVersionsView view;

    private EmfDataset dataset;

    private EmfSession session;

    public EditVersionsPresenter(EmfDataset dataset, EmfSession session, DataEditorService service) {
        this.session = session;
        this.dataset = dataset;
        this.service = service;
    }

    public void display(EditVersionsView view) throws EmfException {
        this.view = view;
        view.observe(this);

        Version[] versions = service.getVersions(dataset.getDatasetid());
        view.display(versions, dataset.getInternalSources());
    }

    public void doNew(Version base, String name) throws EmfException {
        Version derived = service.derive(base, name);
        view.add(derived);
    }

    public void doView(Version version, String table, NonEditableDataView view) throws EmfException {
        NonEditableDataViewPresenter presenter = new NonEditableDataViewPresenter(version, table, view, service);
        presenter.display();
    }

    public void doEdit(Version version, String table, EditableDataView view) throws EmfException {
        if (version.isFinalVersion())
            throw new EmfException("Cannot edit a Version(" + version.getVersion() + ") that is Final.");

        EditableDataViewPresenter presenter = new EditableDataViewPresenter(session, version, table, view, service);
        presenter.display();
    }

    public void doMarkFinal(Version[] versions) throws EmfException {
        for (int i = 0; i < versions.length; i++) {
            if (versions[i].isFinalVersion())
                throw new EmfException("Version: " + versions[i].getVersion()
                        + " is already Final. It should be non-final.");
            service.markFinal(versions[i]);
        }

        reload(dataset);
    }

    private void reload(EmfDataset dataset) throws EmfException {
        Version[] updatedVersions = service.getVersions(dataset.getDatasetid());
        view.reload(updatedVersions);
    }

}

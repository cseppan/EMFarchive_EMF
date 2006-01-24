package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.editor.DataEditorPresenter;
import gov.epa.emissions.framework.client.editor.DataEditorView;
import gov.epa.emissions.framework.client.editor.DataView;
import gov.epa.emissions.framework.client.editor.DataViewPresenter;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.DataViewService;
import gov.epa.emissions.framework.services.EmfDataset;

public class EditVersionsPresenter {

    private DataEditorService service;

    private EditVersionsView view;

    private EmfDataset dataset;

    private DataViewService viewService;

    private User user;

    public EditVersionsPresenter(User user, EmfDataset dataset, DataEditorService service, DataViewService viewService) {
        this.user = user;
        this.dataset = dataset;
        this.service = service;
        this.viewService = viewService;
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

    public void doView(Version version, String table, DataView view) throws EmfException {
        if (!version.isFinalVersion())
            throw new EmfException("Cannot view a Version(" + version.getVersion() + ") that is not Final. Please choose edit.");

        DataViewPresenter presenter = new DataViewPresenter(version, table, view, viewService);
        presenter.display();
    }

    public void doEdit(Version version, String table, DataEditorView view) throws EmfException {
        if (version.isFinalVersion())
            throw new EmfException("Cannot edit a Version(" + version.getVersion() + ") that is Final. Please choose 'view'.");

        DataEditorPresenter presenter = new DataEditorPresenter(user, version, table, service);
        presenter.display(view);
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

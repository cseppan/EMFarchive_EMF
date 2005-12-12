package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.editor.VersionedDataView;
import gov.epa.emissions.framework.client.editor.VersionedDataViewPresenter;
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

    public void doView(Version version, String table, VersionedDataView view) {
        VersionedDataViewPresenter presenter = new VersionedDataViewPresenter(version, table, view, service);
        presenter.display();
    }

    public void doNew(Version base, String name) throws EmfException {
        Version derived = service.derive(base, name);
        view.add(derived);
    }

}

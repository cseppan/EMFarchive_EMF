package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.data.editor.DataEditorPresenter;
import gov.epa.emissions.framework.client.data.editor.DataEditorView;
import gov.epa.emissions.framework.client.data.viewer.DataView;
import gov.epa.emissions.framework.client.data.viewer.DataViewPresenter;
import gov.epa.emissions.framework.services.DataAccessToken;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.EmfDataset;

public class EditVersionsPresenter {

    private EditVersionsView view;

    private EmfDataset dataset;

    private EmfSession session;

    public EditVersionsPresenter(EmfDataset dataset, EmfSession session) {
        this.dataset = dataset;
        this.session = session;
    }

    public void display(EditVersionsView view) throws EmfException {
        this.view = view;
        view.observe(this);

        Version[] versions = editorService().getVersions(dataset.getId());
        view.display(versions, dataset.getInternalSources());
    }

    private DataEditorService editorService() {
        return session.dataEditorService();
    }

    public void doNew(Version base, String name) throws EmfException {
        Version derived = editorService().derive(base, name);
        view.add(derived);
    }

    public void doView(Version version, String table, DataView view) throws EmfException {
        if (!version.isFinalVersion())
            throw new EmfException("Cannot view a Version(" + version.getVersion()
                    + ") that is not Final. Please choose edit.");

        DataViewPresenter presenter = new DataViewPresenter(dataset, version, table, view, session);
        presenter.display();
    }

    public void doEdit(Version version, String table, DataEditorView view) throws EmfException {
        if (version.isFinalVersion())
            throw new EmfException("Cannot edit a Version(" + version.getVersion()
                    + ") that is Final. Please choose 'View'.");

        DataEditorPresenter presenter = new DataEditorPresenter(dataset, version, table, session);
        presenter.display(view);
    }

    public void doMarkFinal(Version[] versions) throws EmfException {
        for (int i = 0; i < versions.length; i++) {
            if (versions[i].isFinalVersion())
                throw new EmfException("Version: " + versions[i].getVersion()
                        + " is already Final. It should be non-final.");
            editorService().markFinal(token(versions[i]));
        }

        reload(dataset);
    }

    private DataAccessToken token(Version version) {
        return new DataAccessToken(version, null);
    }

    private void reload(EmfDataset dataset) throws EmfException {
        Version[] updatedVersions = editorService().getVersions(dataset.getId());
        view.reload(updatedVersions);
    }

}

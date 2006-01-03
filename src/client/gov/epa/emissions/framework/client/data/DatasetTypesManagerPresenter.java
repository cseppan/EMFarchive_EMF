package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.ui.ViewLayout;

public class DatasetTypesManagerPresenter {

    private DatasetTypesManagerView view;

    private ViewLayout viewLayout;

    private EmfSession session;

    public DatasetTypesManagerPresenter(EmfSession session, DatasetTypesManagerView view, ViewLayout layout) {
        this.session = session;
        this.view = view;
        this.viewLayout = layout;
    }

    public void doDisplay() throws EmfException {
        view.observe(this);
        view.display(serviceLocator().datasetTypeService());
    }

    private ServiceLocator serviceLocator() {
        return session.serviceLocator();
    }

    public void doClose() {
        view.close();
    }

    public void doEdit(DatasetType type, EditableDatasetTypeView editable, ViewableDatasetTypeView viewable)
            throws EmfException {
        if (viewLayout.activate("Edit" + type.getName()))
            return;

        viewLayout.add(editable, "Edit" + type.getName());
        EditableDatasetTypePresenter p = new EditableDatasetTypePresenter(session, editable, viewable, type);
        p.doDisplay();
    }

    public void doView(DatasetType type, ViewableDatasetTypeView viewable) throws EmfException {
        if (viewLayout.activate(type.getName()))
            return;

        viewLayout.add(viewable, type.getName());
        ViewableDatasetTypePresenter p = new ViewableDatasetTypePresenter(viewable, type, serviceLocator()
                .dataCommonsService());
        p.doDisplay();
    }
}

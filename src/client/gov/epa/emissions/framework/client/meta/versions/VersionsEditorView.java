package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.EmfDataset;

public interface VersionsEditorView extends ManagedView {
    void observe(VersionsEditorPresenter presenter);

    void display(EmfDataset dataset, DataEditorService service);
}

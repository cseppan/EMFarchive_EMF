package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.framework.services.SimplePage;

public interface PageView {
    void observe(PageViewPresenter presenter);

    void display(SimplePage page);

}

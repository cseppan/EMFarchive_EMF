package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.framework.services.Page;

public interface PageView {
    void observe(PageViewPresenter presenter);

    void display(Page page);

    void refresh();
}

package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.Page;

public interface PageView {
    void observe(PageViewPresenter presenter);

    void display(Page page);

}

package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataEditorServices;
import gov.epa.emissions.framework.services.Page;

public class PageViewPresenter {

    private DataEditorServices services;

    private PageView view;

    private String table;

    private int pageIndex;

    public PageViewPresenter(DataEditorServices services, PageView view, String table) {
        this.services = services;
        this.view = view;
        this.table = table;
    }

    public void doDisplayNext() throws EmfException {
        pageIndex++;
        displayPage(pageIndex);
    }

    public void doDisplayPrevious() throws EmfException {
        pageIndex--;
        displayPage(pageIndex);
    }

    private void displayPage(int pageIndex) throws EmfException {
        Page page = services.getPage(table, pageIndex);
        view.display(page);
    }
}

package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataEditorServices;
import gov.epa.emissions.framework.services.Page;

public class PageViewPresenter {

    private DataEditorServices services;

    private PageView view;

    private String table;

    private int pageNumber;

    private Page page;

    public PageViewPresenter(DataEditorServices services, PageView view, String table) {
        this.services = services;
        this.view = view;
        this.table = table;
    }

    public void observeView() {
        view.observe(this);
    }

    public void doDisplayNext() throws EmfException {
        if (pageNumber < pageCount())
            pageNumber++;
        doDisplay(pageNumber);
    }

    public void doDisplayPrevious() throws EmfException {
        if (pageNumber > 1)
            pageNumber--;
        doDisplay(pageNumber);
    }

    public void doDisplay(int pageNumber) throws EmfException {
        this.pageNumber = pageNumber;
        page = services.getPage(table, pageNumber);
        view.display(page);
    }

    public void doDisplayFirst() throws EmfException {
        doDisplay(1);
    }

    public void doDisplayLast() throws EmfException {
        doDisplay(pageCount());
    }

    private int pageCount() throws EmfException {
        return services.getPageCount(table);
    }

    public void doDisplayPageWithRecord(int record) throws EmfException {
        page = services.getPageWithRecord(table, record);
        view.display(page);
    }

    public int totalRecords() throws EmfException {
        return services.getTotalRecords(table);
    }

}

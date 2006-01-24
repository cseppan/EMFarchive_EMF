package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataAccessService;
import gov.epa.emissions.framework.services.DataAccessToken;

public class TablePaginator {

    private DataAccessService service;

    private String table;

    private Page page;

    private Version version;

    private TableView view;

    public TablePaginator(Version version, String table, TableView view, DataAccessService service) {
        page = new Page();// page 0, uninitialized

        this.version = version;
        this.table = table;

        this.view = view;
        this.service = service;
    }

    public void doDisplayNext() throws EmfException {
        int pageNumber = pageNumber();
        if (pageNumber < pageCount())
            pageNumber++;

        doDisplay(pageNumber);
    }

    public void doDisplayPrevious() throws EmfException {
        int pageNumber = pageNumber();
        if (pageNumber > 1)
            pageNumber--;

        doDisplay(pageNumber);
    }

    int pageNumber() {
        return page.getNumber();
    }

    public void doDisplay(int pageNumber) throws EmfException {
        if (pageNumber() == pageNumber)
            return;

        page = service.getPage(token(), pageNumber);

        view.display(page);
    }

    public void doDisplayFirst() throws EmfException {
        if (pageNumber() != 1)
            doDisplay(1);
    }

    public void doDisplayLast() throws EmfException {
        int pageCount = pageCount();
        if (pageNumber() != pageCount)
            doDisplay(pageCount);
    }

    private int pageCount() throws EmfException {
        return service.getPageCount(token());
    }

    public void doDisplayPageWithRecord(int record) throws EmfException {
        page = service.getPageWithRecord(token(), record);
        view.display(page);
    }

    public int totalRecords() throws EmfException {
        return service.getTotalRecords(token());
    }

    DataAccessToken token() {
        return new DataAccessToken(version, table);
    }

}

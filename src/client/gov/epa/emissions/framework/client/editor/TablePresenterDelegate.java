package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.DataAccessToken;

public class TablePresenterDelegate {

    private DataEditorService service;

    private String table;

    private Page page;

    private Version version;

    private TableView view;

    public TablePresenterDelegate(Version version, String table, TableView view, DataEditorService service) {
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
        if(pageNumber() == pageNumber)
            return;
        
        page = service.getPage(editToken(), pageNumber);

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
        return service.getPageCount(editToken());
    }

    public void doDisplayPageWithRecord(int record) throws EmfException {
        page = service.getPageWithRecord(editToken(), record);
        view.display(page);
    }

    public int totalRecords() throws EmfException {
        return service.getTotalRecords(editToken());
    }

    DataAccessToken editToken() {
        return new DataAccessToken(version, table);
    }

}

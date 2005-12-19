package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.EditToken;

public class TablePresenterDelegate {

    private DataEditorService service;

    private String table;

    private int pageNumber;

    private Page page;

    private Version version;

    private TableView view;

    public TablePresenterDelegate(Version version, String table, TableView view, DataEditorService service) {
        this.version = version;
        this.table = table;

        this.view = view;
        this.service = service;
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
        page = service.getPage(editToken(), pageNumber);

        view.display(page);
    }

    public void doDisplayFirst() throws EmfException {
        doDisplay(1);
    }

    public void doDisplayLast() throws EmfException {
        doDisplay(pageCount());
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

    private EditToken editToken() {
        return new EditToken(version, table);
    }

}

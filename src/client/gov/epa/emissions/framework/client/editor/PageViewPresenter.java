package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.EditToken;

public class PageViewPresenter {

    private DataEditorService services;

    private PageView view;

    private String table;

    private int pageNumber;

    private Page page;

    private Dataset dataset;

    public PageViewPresenter(DataEditorService services, PageView view, Dataset dataset, String table) {
        this.services = services;
        this.view = view;
        this.dataset = dataset;
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
        page = services.getPage(editToken(), pageNumber);
        view.display(page);
    }

    public void doDisplayFirst() throws EmfException {
        doDisplay(1);
    }

    public void doDisplayLast() throws EmfException {
        doDisplay(pageCount());
    }

    private int pageCount() throws EmfException {
        return services.getPageCount(editToken());
    }

    public void doDisplayPageWithRecord(int record) throws EmfException {
        page = services.getPageWithRecord(editToken(), record);
        view.display(page);
    }

    public int totalRecords() throws EmfException {
        return services.getTotalRecords(editToken());
    }

    private EditToken editToken() {
        return new EditToken(dataset.getDatasetid(), 0, table);//TODO: use specific version
    }

}

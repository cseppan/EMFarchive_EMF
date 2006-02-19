package gov.epa.emissions.framework.client.editor;

import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataAccessToken;
import gov.epa.emissions.framework.services.DataEditorService;

public class EditableTablePresenterImpl implements EditableTablePresenter {

    private EditablePageManagerView view;

    private TablePaginator paginator;

    private DataEditorService service;

    private InternalSource source;

    public EditableTablePresenterImpl(Version version, String table, InternalSource source,
            EditablePageManagerView view, DataEditorService service) {
        this.service = service;
        this.source = source;
        this.view = view;

        paginator = new TablePaginator(version, table, view, service);
    }

    public void observe() {
        view.observe(this);
    }

    public void doDisplayNext() throws EmfException {
        submitChanges();
        paginator.doDisplayNext();
    }

    public void doDisplayPrevious() throws EmfException {
        submitChanges();
        paginator.doDisplayPrevious();
    }

    public void doDisplay(int pageNumber) throws EmfException {
        submitChanges();
        paginator.doDisplay(pageNumber);
    }

    public void doDisplayFirst() throws EmfException {
        submitChanges();
        paginator.doDisplayFirst();
    }

    public void doDisplayLast() throws EmfException {
        submitChanges();
        paginator.doDisplayLast();
    }

    public void doDisplayPageWithRecord(int record) throws EmfException {
        if (paginator.isCurrent(record))
            return;

        submitChanges();
        paginator.doDisplayPageWithRecord(record);
    }

    public int totalRecords() throws EmfException {
        return paginator.totalRecords();
    }

    public void submitChanges() throws EmfException {
        ChangeSet changeset = view.changeset();
        if (changeset.hasChanges()) {
            service.submit(token(), changeset, paginator.pageNumber());
            changeset.clear();
        }

        updateFilteredCount();
    }

    private void updateFilteredCount() throws EmfException {
        view.updateFilteredRecordsCount(paginator.totalRecords());
    }

    private DataAccessToken token() {
        return paginator.token();
    }

    public boolean hasChanges() {
        return view.changeset().hasChanges();
    }

    public void doApplyConstraints(String rowFilter, String sortOrder) throws EmfException {
        validateColsInSortOrder(sortOrder, source.getCols());
        Page page = service.applyConstraints(token(), rowFilter, sortOrder);
        view.display(page);
        updateFilteredCount();
    }

    private void validateColsInSortOrder(String sortOrder, String[] cols) throws EmfException {
        List colsList = Arrays.asList(cols);
        for (StringTokenizer tokenizer = new StringTokenizer(sortOrder.trim(), ","); tokenizer.hasMoreTokens();) {
            String col = tokenizer.nextToken().trim().toLowerCase();
            if (!colsList.contains(col) && !colsList.contains(col.toUpperCase()))
                throw new EmfException("Sort Order contains an invalid column: " + col);
        }
    }

}

package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.ColumnMetaData;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.data.TablePaginator;
import gov.epa.emissions.framework.client.data.TablePaginatorImpl;
import gov.epa.emissions.framework.services.DataAccessToken;
import gov.epa.emissions.framework.services.DataEditorService;

import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

public class EditableTablePresenterImpl implements EditableTablePresenter {

    private EditablePageManagerView view;

    private TablePaginator paginator;

    private DataEditorService service;

    private TableMetadata tableMetadata;

    private DatasetType datasetType;

    public EditableTablePresenterImpl(DatasetType datasetType, Version version, String table,
            TableMetadata tableMetadata, EditablePageManagerView view, DataEditorService service) {
        this(datasetType, new TablePaginatorImpl(version, table, view, service), tableMetadata, view, service);
    }

    public EditableTablePresenterImpl(DatasetType datasetType, TablePaginator paginator, TableMetadata tableMetadata,
            EditablePageManagerView view, DataEditorService service) {
        this.datasetType = datasetType;
        this.service = service;
        this.tableMetadata = tableMetadata;
        this.view = view;

        this.paginator = paginator;
    }

    public void observe() {
        view.observe(this);
    }

    public void doDisplay() throws EmfException {
        paginator.doDisplayFirst();
        doApplyConstraints("", datasetType.getDefaultSortOrder());
    }

    public void reloadCurrent() throws EmfException {
        paginator.reloadCurrent();
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
        validateColsInSortOrder(sortOrder, cols(tableMetadata));
        Page page = service.applyConstraints(token(), rowFilter, sortOrder);
        view.display(page);
        updateFilteredCount();
    }

    private String[] cols(TableMetadata tableMetadata) {
        ColumnMetaData[] cols = tableMetadata.getCols();
        String[] colNames = new String[cols.length];
        for (int i = 0; i < cols.length; i++) {
            colNames[i] = cols[i].getName();
        }
        return colNames;
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

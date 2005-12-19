package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.framework.EmfException;

public interface TablePresenter {

    void observe();

    void doDisplayNext() throws EmfException;

    void doDisplayPrevious() throws EmfException;

    void doDisplay(int pageNumber) throws EmfException;

    void doDisplayFirst() throws EmfException;

    void doDisplayLast() throws EmfException;

    void doDisplayPageWithRecord(int record) throws EmfException;

    int totalRecords() throws EmfException;

}
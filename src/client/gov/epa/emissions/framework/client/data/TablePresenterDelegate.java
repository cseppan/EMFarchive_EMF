package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataAccessToken;

public interface TablePresenterDelegate {

    void display() throws EmfException;

    void reloadCurrent() throws EmfException;

    void doDisplayNext() throws EmfException;

    void doDisplayPrevious() throws EmfException;

    void doDisplay(int pageNumber) throws EmfException;

    void doDisplayFirst() throws EmfException;

    void doDisplayLast() throws EmfException;

    void doDisplayPageWithRecord(int record) throws EmfException;

    int totalRecords() throws EmfException;

    void updateFilteredCount() throws EmfException;

    DataAccessToken token();

    void doApplyConstraints(String rowFilter, String sortOrder) throws EmfException;

    int pageNumber();

}
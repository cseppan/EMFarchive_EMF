package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.io.TableMetadata;

public interface TableView {

    void display(Page page);

    void scrollToPageEnd();
    
    TableMetadata tableMetadata();

}
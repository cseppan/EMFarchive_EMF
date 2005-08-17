package gov.epa.emissions.framework.client.exim;

import java.awt.Dimension;

import gov.epa.emissions.framework.client.EmfInteralFrame;
import gov.epa.emissions.framework.commons.ExImServices;
import gov.epa.emissions.framework.commons.User;

public class ImportWindow extends EmfInteralFrame implements ImportView {

    public ImportWindow(User user, ExImServices eximServices) {
        super("Import Dataset");
        
        setSize(new Dimension(100, 100));
    }

    public void register(ImportPresenter presenter) {
    }

    public void close() {
    }

    public void display() {
        super.setVisible(true);
    }

}

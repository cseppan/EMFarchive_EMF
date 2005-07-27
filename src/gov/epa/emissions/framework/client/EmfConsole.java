package gov.epa.emissions.framework.client;

import java.awt.Dimension;
import java.awt.Point;

public class EmfConsole extends EmfWindow {
    
    public EmfConsole() throws Exception {

        this.setSize(new Dimension(500, 500));
        this.setLocation(new Point(400, 200));
        this.setTitle("Emissions Modeling Framework (EMF)");
    }
}

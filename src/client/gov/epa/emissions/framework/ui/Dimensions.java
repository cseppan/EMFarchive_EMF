package gov.epa.emissions.framework.ui;

import java.awt.Dimension;
import java.awt.Toolkit;

public class Dimensions {

    public Dimension getSize(double d, double e) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        double height = screen.getHeight();
        double width = screen.getWidth();

        Dimension dim = new Dimension();
        dim.setSize(width * d, height * e);

        return dim;
    }

}

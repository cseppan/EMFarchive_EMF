package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.io.importer.TemporalResolution;
import gov.epa.emissions.framework.services.EmfDataset;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.util.Date;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;

public class MetadataWindowLauncher {

    public static void main(String[] args) throws Exception {
        EmfDataset dataset = new EmfDataset();
        dataset.setName("Test Dataset");
        dataset.setCreator("Jim Bowman");
        dataset.setDatasetType("ORL Non Point");
        dataset.setDescription("ORL data type...blah...blah...blah.  "
                + "The New Orleans downtown district has been reopened for business "
                + "owners to take a look at their properties.");
        dataset.setStartDateTime(new Date());
        dataset.setStopDateTime(new Date());
        dataset.setTemporalResolution(TemporalResolution.MONTHLY.getName());

        MetadataWindow view = new MetadataWindow();
        MetadataPresenter presenter = new MetadataPresenter(dataset, null);
        presenter.display(view);

        JFrame frame = new JFrame();

        addAsInternalFrame(view, frame);

        frame.setSize(new Dimension(800, 600));
        frame.setLocation(new Point(400, 200));
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static void addAsInternalFrame(Container window, JFrame frame) {
        JDesktopPane desktop = new JDesktopPane();
        desktop.setName("EMF Console");
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        desktop.add(window);

        frame.setContentPane(desktop);
    }

}

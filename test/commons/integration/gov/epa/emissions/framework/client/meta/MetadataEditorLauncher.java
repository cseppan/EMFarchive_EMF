package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.io.EmfDataset;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;

public class MetadataEditorLauncher {

    public static void main(String[] args) throws Exception {
        EmfDataset dataset = new EmfDataset();
        dataset.setName("Test Dataset");

        MetadataWindow view = new MetadataWindow();
        MetadataPresenter presenter = new MetadataPresenter(dataset);
        presenter.observe(view);

        presenter.notifyDisplay();

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

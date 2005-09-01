package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.framework.client.EmfInteralFrame;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;

public class DatasetsBrowserLauncher {

    public static void main(String[] args) throws Exception {
        DatasetsBrowserLauncher launcher = new DatasetsBrowserLauncher();

        JFrame frame = new JFrame();
        frame.setSize(new Dimension(800, 600));
        frame.setLocation(new Point(400, 200));
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        
        DatasetsBrowserWindow console = new DatasetsBrowserWindow(launcher.createDatasets(), frame);
        console.setVisible(true);

        launcher.addAsInternalFrame(console, frame);
    }

    private Dataset[] createDatasets() {
        List datasets = new ArrayList();

        Dataset dataset1 = new EmfDataset();
        dataset1.setName("name1");
        dataset1.setCreator("creator1");
        dataset1.setRegion("region1");
        dataset1.setStartDateTime(new Date());
        dataset1.setStopDateTime(new Date());
        datasets.add(dataset1);

        Dataset dataset2 = new EmfDataset();
        dataset2.setName("name2");
        dataset2.setCreator("creator2");
        dataset2.setRegion("region2");
        dataset2.setStartDateTime(new Date());
        dataset2.setStopDateTime(new Date());
        datasets.add(dataset2);

        return (Dataset[]) datasets.toArray(new Dataset[0]);
    }

    private void addAsInternalFrame(EmfInteralFrame console, JFrame frame) {
        JDesktopPane desktop = new JDesktopPane();
        desktop.setName("EMF Console");
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        desktop.add(console);

        frame.setContentPane(desktop);
    }

}

package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.client.EmfFrame;
import gov.epa.emissions.framework.client.EmfInternalFrame;
import gov.epa.emissions.framework.services.EmfDataset;

import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;

public class DataViewLauncher {

    public static void main(String[] args) throws Exception {
        DataViewLauncher launcher = new DataViewLauncher();

        EmfFrame frame = new EmfFrame("DataView Launcher", "DataView Launcher");
        frame.setSize(new Dimension(900, 700));
        frame.setLocation(new Point(300, 200));
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JDesktopPane desktop = new JDesktopPane();
        DataViewWindow console = new DataViewWindow();
        DataViewPresenter p = new DataViewPresenter(launcher.createDataset(), console);
        p.doDisplay();

        launcher.addAsInternalFrame(console, frame, desktop);
    }

    private EmfDataset createDataset() {
        EmfDataset dataset = new EmfDataset();
        dataset.setName("ORL Point");

        InternalSource source1 = new InternalSource();
        source1.setTable("table1");
        InternalSource source2 = new InternalSource();
        source2.setTable("table2");
        InternalSource[] sources = { source1, source2 };
        dataset.setInternalSources(sources);

        return dataset;
    }

    private void addAsInternalFrame(EmfInternalFrame console, JFrame frame, JDesktopPane desktop) {
        desktop.setName("EMF Console");
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        desktop.add(console);

        frame.setContentPane(desktop);
    }

}

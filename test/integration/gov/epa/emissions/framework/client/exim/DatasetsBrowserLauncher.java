package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.client.EmfFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.data.DatasetsBrowserWindow;
import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.EmfDataset;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;

import org.jmock.Mock;
import org.jmock.core.constraint.IsEqual;
import org.jmock.core.matcher.InvokeAtLeastOnceMatcher;
import org.jmock.core.stub.ReturnStub;

public class DatasetsBrowserLauncher {

    public static void main(String[] args) throws Exception {
        DatasetsBrowserLauncher launcher = new DatasetsBrowserLauncher();

        EmfFrame frame = new EmfFrame("DatasetsBrowserLauncher", "Datasets Browser");
        frame.setSize(new Dimension(900, 700));
        frame.setLocation(new Point(300, 200));
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        EmfDataset[] datasets = launcher.createDatasets();
        Mock services = new Mock(DataServices.class);
        services.expects(new InvokeAtLeastOnceMatcher()).method(new IsEqual("getDatasets")).will(
                new ReturnStub(datasets));

        Mock session = new Mock(EmfSession.class);
        session.stubs().method(new IsEqual("getDataServices")).will(new ReturnStub(services.proxy()));

        JDesktopPane desktop = new JDesktopPane();
        DatasetsBrowserWindow console = new DatasetsBrowserWindow((EmfSession) session.proxy(), frame, desktop);
        console.setVisible(true);

        launcher.addAsInternalFrame(console, frame, desktop);
    }

    private EmfDataset[] createDatasets() {
        List datasets = new ArrayList();

        Dataset dataset1 = new EmfDataset();
        dataset1.setName("name1");
        dataset1.setDatasetType(new DatasetType("type 1"));
        dataset1.setCreator("creator1");
        dataset1.setRegion("region1");
        dataset1.setStartDateTime(new Date());
        dataset1.setStopDateTime(new Date());
        datasets.add(dataset1);

        Dataset dataset2 = new EmfDataset();
        dataset2.setName("name2");
        dataset2.setDatasetType(new DatasetType("type 2"));
        dataset2.setCreator("creator2");
        dataset2.setRegion("region2");
        dataset2.setStartDateTime(new Date());
        dataset2.setStopDateTime(new Date());
        datasets.add(dataset2);

        return (EmfDataset[]) datasets.toArray(new EmfDataset[0]);
    }

    private void addAsInternalFrame(ReusableInteralFrame console, JFrame frame, JDesktopPane desktop) {
        desktop.setName("EMF Console");
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        desktop.add(console);

        frame.setContentPane(desktop);
    }

}

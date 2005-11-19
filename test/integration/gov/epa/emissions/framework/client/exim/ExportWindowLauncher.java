package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.DefaultEmfSession;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.ExImService;
import gov.epa.emissions.framework.services.User;

import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;

import org.jmock.Mock;
import org.jmock.core.constraint.IsEqual;
import org.jmock.core.matcher.InvokeAtLeastOnceMatcher;
import org.jmock.core.stub.ReturnStub;

public class ExportWindowLauncher {

    public static void main(String[] args) throws EmfException {
        Mock exim = new Mock(ExImService.class);
        exim.stubs().method("getExportBaseFolder").will(new ReturnStub("folder/blah"));

        exim.expects(new InvokeAtLeastOnceMatcher()).method(new IsEqual("startExport")).withAnyArguments();

        User user = new User();

        new EmfDataset().setName("ORL Non Road");
        EmfDataset[] datasets = new EmfDataset[] { createDataset("ORL Non Point"), createDataset("ORL Non Point"),
                createDataset("ORL Non Point"), createDataset("ORL Non Point"), createDataset("ORL Non Point"),
                createDataset("ORL Non Point"), createDataset("ORL Non Point"), createDataset("ORL Non Point"),
                createDataset("ORL Non Point") };

        Mock servicesLocator = new Mock(ServiceLocator.class);
        servicesLocator.expects(new InvokeAtLeastOnceMatcher()).method(new IsEqual("getExImServices"))
                .withAnyArguments().will(new ReturnStub(exim.proxy()));

        ExportPresenter presenter = new DefaultExportPresenter(new DefaultEmfSession(user, (ServiceLocator) servicesLocator
                .proxy()));

        JDesktopPane desktop = new JDesktopPane();
        ExportWindow view = new ExportWindow(datasets);
        desktop.add(view);
        presenter.display(view);

        JFrame frame = new JFrame();
        addAsInternalFrame(frame, desktop);

        frame.setSize(new Dimension(800, 600));
        frame.setLocation(new Point(400, 200));
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static EmfDataset createDataset(String name) {
        EmfDataset dataset = new EmfDataset();
        dataset.setName(name);

        return dataset;
    }

    private static void addAsInternalFrame(JFrame frame, JDesktopPane desktop) {
        desktop.setName("EMF Console");
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

        frame.setContentPane(desktop);
    }
}

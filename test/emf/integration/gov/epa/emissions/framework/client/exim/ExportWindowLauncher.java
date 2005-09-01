package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.ExImServices;
import gov.epa.emissions.framework.services.User;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;

import org.jmock.Mock;
import org.jmock.core.constraint.IsEqual;
import org.jmock.core.matcher.InvokeAtLeastOnceMatcher;

public class ExportWindowLauncher {

    public static void main(String[] args) throws EmfException {
        Mock exim = new Mock(ExImServices.class);
        exim.expects(new InvokeAtLeastOnceMatcher()).method(new IsEqual("startExport")).withAnyArguments();

        User user = new User();
        EmfDataset dataset = new EmfDataset();
        dataset.setDescription("ORL Non Road");

        ExportWindow view = new ExportWindow(dataset);
        ExportPresenter presenter = new ExportPresenter(user, (ExImServices) exim.proxy());
        presenter.observe(view);

        view.display();

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

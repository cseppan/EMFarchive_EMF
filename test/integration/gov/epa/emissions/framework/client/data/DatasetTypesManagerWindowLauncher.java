package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.client.EmfInternalFrame;
import gov.epa.emissions.framework.services.DatasetTypesServices;
import gov.epa.emissions.framework.ui.DefaultViewLayout;
import gov.epa.emissions.framework.ui.ViewLayout;

import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;

import org.jmock.Mock;
import org.jmock.core.constraint.IsEqual;
import org.jmock.core.stub.ReturnStub;

public class DatasetTypesManagerWindowLauncher {

    public static void main(String[] args) throws Exception {
        DatasetTypesManagerWindowLauncher launcher = new DatasetTypesManagerWindowLauncher();

        JFrame frame = new JFrame();
        JDesktopPane desktop = new JDesktopPane();

        DatasetTypesManagerWindow view = new DatasetTypesManagerWindow(frame, desktop);
        DatasetType type1 = new DatasetType();
        type1.setName("type1");
        DatasetType type2 = new DatasetType();
        type2.setName("type2");
        
        DatasetType[] types = { type1, type2 };
        Mock services = new Mock(DatasetTypesServices.class);
        services.stubs().method(new IsEqual("getDatasetTypes")).will(new ReturnStub(types));

        ViewLayout layout = new DefaultViewLayout(view);
        DatasetTypesManagerPresenter presenter = new DatasetTypesManagerPresenter(view, (DatasetTypesServices) services
                .proxy(), layout);

        launcher.addAsInternalFrame(view, frame, desktop);

        frame.setSize(new Dimension(800, 600));
        frame.setLocation(new Point(400, 200));
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        presenter.doDisplay();
    }

    private void addAsInternalFrame(EmfInternalFrame console, JFrame frame, JDesktopPane desktop) {
        desktop.setName("EMF Console");
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        desktop.add(console);

        frame.setContentPane(desktop);
    }

}

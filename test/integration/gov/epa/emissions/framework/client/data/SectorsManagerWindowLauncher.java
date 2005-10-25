package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.framework.client.EmfInternalFrame;
import gov.epa.emissions.framework.services.DataServices;

import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;

import org.jmock.Mock;
import org.jmock.core.constraint.IsEqual;
import org.jmock.core.stub.ReturnStub;

public class SectorsManagerWindowLauncher {

    public static void main(String[] args) throws Exception {
        SectorsManagerWindowLauncher launcher = new SectorsManagerWindowLauncher();

        JFrame frame = new JFrame();
        JDesktopPane desktop = new JDesktopPane();

        SectorManagerWindow view = new SectorManagerWindow(frame, desktop);
        Sector sector1 = new Sector("desc1", "name1");
        Sector sector2 = new Sector("desc2", "name2");
        Sector[] sectors = { sector1, sector2 };
        Mock dataServices = new Mock(DataServices.class);
        dataServices.stubs().method(new IsEqual("getSectors")).will(new ReturnStub(sectors));

        SectorManagerPresenter presenter = new SectorManagerPresenter(view, (DataServices) dataServices.proxy());

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

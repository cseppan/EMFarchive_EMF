package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.client.EmfFrame;
import gov.epa.emissions.framework.client.EmfInternalFrame;
import gov.epa.emissions.framework.services.DataEditorServices;
import gov.epa.emissions.framework.services.DbRecord;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.Page;

import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;

import org.jmock.Mock;
import org.jmock.core.stub.ReturnStub;

public class DataViewLauncher {

    public static void main(String[] args) throws Exception {
        DataViewLauncher launcher = new DataViewLauncher();

        EmfFrame frame = new EmfFrame("DataView Launcher", "DataView Launcher");
        frame.setSize(new Dimension(1200, 900));
        frame.setLocation(new Point(100, 50));
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JDesktopPane desktop = new JDesktopPane();

        Mock service = service();
        DataEditorServices servicesProxy = (DataEditorServices) service.proxy();
        DataViewWindow view = new DataViewWindow();
        DataViewPresenter p = new DataViewPresenter(launcher.createDataset(), view, servicesProxy);

        launcher.addAsInternalFrame(view, frame, desktop);
        p.doDisplay();
    }

    private static Mock service() {
        Mock mock = new Mock(DataEditorServices.class);

        DbRecord record1 = new DbRecord(1521);
        record1.setTokens(new String[] { "a", "b", "c" });
        DbRecord record2 = new DbRecord(1522);
        record2.setTokens(new String[] { "x", "y", "z" });
        DbRecord record3 = new DbRecord(1523);
        record3.setTokens(new String[] { "d", "e", "f" });

        DbRecord[] records = { record1, record2, record3 };

        Page page = new Page();
        page.setRecords(records);

        mock.stubs().method("getPage").withAnyArguments().will(new ReturnStub(page));
        mock.stubs().method("getPageCount").withAnyArguments().will(new ReturnStub(new Integer(20)));
        mock.stubs().method("getTotalRecords").withAnyArguments().will(new ReturnStub(new Integer(200)));
        mock.stubs().method("getPageWithRecord").withAnyArguments().will(new ReturnStub(page));

        return mock;
    }

    private EmfDataset createDataset() {
        EmfDataset dataset = new EmfDataset();
        dataset.setName("ORL Point");

        InternalSource source1 = new InternalSource();
        source1.setTable("table1");
        source1.setCols(new String[] { "1", "2", "3" });
        InternalSource source2 = new InternalSource();
        source2.setTable("table2");
        source2.setCols(new String[] { "1", "2", "3" });
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

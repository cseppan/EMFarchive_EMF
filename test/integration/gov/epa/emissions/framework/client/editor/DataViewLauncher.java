package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.client.EmfFrame;
import gov.epa.emissions.framework.client.EmfInternalFrame;
import gov.epa.emissions.framework.services.DataEditorServices;
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
        frame.setSize(new Dimension(900, 700));
        frame.setLocation(new Point(300, 200));
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JDesktopPane desktop = new JDesktopPane();
        
        Mock service = service();
        DataViewWindow view = new DataViewWindow((DataEditorServices) service.proxy());
        DataViewPresenter p = new DataViewPresenter(launcher.createDataset(), view);
        
        launcher.addAsInternalFrame(view, frame, desktop);
        p.doDisplay();
    }

    private static Mock service() {
        Mock mock = new Mock(DataEditorServices.class);

        Record record1 = new Record();
        record1.setTokens(new String[] { "a", "b", "c" });
        Record record2 = new Record();
        record2.setTokens(new String[] { "x", "y", "z" });
        Record record3 = new Record();
        record3.setTokens(new String[] { "d", "e", "f" });
        
        Record[] records = { record1, record2, record3 };

        Page page = new Page();
        page.setRecords(records);

        mock.stubs().method("getPage").withAnyArguments().will(new ReturnStub(page));

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

package gov.epa.emissions.commons.gui;

import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;

import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JFrame;
import javax.swing.table.TableModel;

import org.jmock.Mock;
import org.jmock.core.constraint.IsEqual;
import org.jmock.core.stub.ReturnStub;

public class SortFilterSelectionPanelLauncher {

    public static void main(String[] args) {
        JFrame frame = new JFrame();

        Mock delegate = createModel();

        SortFilterSelectModel model = new SortFilterSelectModel((TableModel) delegate.proxy());
        SortFilterSelectionPanel panel = new SortFilterSelectionPanel(frame, model);

        frame.getContentPane().add(panel);
        
        frame.setSize(new Dimension(500, 200));
        frame.setLocation(new Point(400, 200));

        frame.show();
    }

    private static Mock createModel() {
        Mock delegate = new Mock(TableModel.class);

        delegate.stubs().method("getColumnCount").withNoArguments().will(new ReturnStub(new Integer(3)));

        delegate.stubs().method("getColumnName").with(isEqual(0)).will(new ReturnStub("Name"));
        delegate.stubs().method("getColumnName").with(isEqual(1)).will(new ReturnStub("Age"));
        delegate.stubs().method("getColumnName").with(isEqual(2)).will(new ReturnStub("Country"));

        delegate.stubs().method("getRowCount").withNoArguments().will(new ReturnStub(new Integer(2)));
        
        addRow(delegate, 0, "Jimmy Connors", "46", "US");
        addRow(delegate, 1, "Boris Becker", "37", "Germany");
        
        return delegate;
    }

    private static void addRow(Mock delegate, int row, String name, String age, String country) {
        delegate.stubs().method("getValueAt").with(isEqual(row), isEqual(0)).will(new ReturnStub(name));
        delegate.stubs().method("getValueAt").with(isEqual(row), isEqual(1)).will(new ReturnStub(age));
        delegate.stubs().method("getValueAt").with(isEqual(row), isEqual(2)).will(new ReturnStub(country));
    }

    private static IsEqual isEqual(int val) {
        return new IsEqual(new Integer(val));
    }
}

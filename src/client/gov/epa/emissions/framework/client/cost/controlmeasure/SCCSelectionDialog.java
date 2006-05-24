package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class SCCSelectionDialog extends JDialog implements SCCSelectionView {

    private SortFilterSelectModel selectModel;

    private EmfConsole parent;

    private SCCSelectionPresenter presenter;

    public SCCSelectionDialog(EmfConsole parent) {
        super(parent);
        this.parent = parent;
    }

    public void display(SCCTableData tableData) {
        EmfTableModel tableModel = new EmfTableModel(tableData);
        selectModel = new SortFilterSelectModel(tableModel);
        SortFilterSelectionPanel panel = new SortFilterSelectionPanel(parent, selectModel);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(panel, BorderLayout.CENTER);
        contentPane.add(buttonPanel(), BorderLayout.SOUTH);

        setTitle("Select SCCs");
        this.pack();
        this.setLocation(ScreenUtils.getPointToCenter(parent));
        this.setVisible(true);
    }

    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        panel.add(new Button("OK", okAction()));
        panel.add(new Button("Cancel", cancelAction()));
        return panel;
    }

    private Action cancelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }

        };
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                add();
                setVisible(false);
                dispose();
            }
        };
    }

    private void add() {
        List selected = selectModel.selected();
        Scc[] sccs = (Scc[]) selected.toArray(new Scc[0]);
        presenter.doAdd(sccs);

    }

    public void observe(SCCSelectionPresenter presenter) {
        this.presenter = presenter;
    }

}

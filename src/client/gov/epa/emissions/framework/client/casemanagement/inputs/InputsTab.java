package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.ui.EmfTableModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class InputsTab extends JPanel implements InputsTabView {

    private EmfConsole parentConsole;

    private SortFilterSelectModel selectModel;

    private InputsTabPresenter presenter;

    private DesktopManager desktopManager;

    public InputsTab(EmfConsole parentConsole, DesktopManager desktopManager) {
        super.setName("inputsTab");
        this.parentConsole = parentConsole;
        this.desktopManager = desktopManager;

        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void display(CaseInput[] notes, InputsTabPresenter presenter) {
        this.presenter = presenter;
        super.removeAll();
        super.add(createLayout(notes, parentConsole));
    }

    private JPanel createLayout(CaseInput[] notes, EmfConsole parentConsole) {
        JPanel layout = new JPanel(new BorderLayout());

        layout.add(createSortFilterPane(notes, parentConsole), BorderLayout.CENTER);
        layout.add(createControlPanel(), BorderLayout.SOUTH);

        return layout;
    }

    private JScrollPane createSortFilterPane(CaseInput[] notes, EmfConsole parentConsole) {
        EmfTableModel model = new EmfTableModel(new InputsTableData(notes));
        selectModel = new SortFilterSelectModel(model);

        SortFilterSelectionPanel panel = new SortFilterSelectionPanel(parentConsole, selectModel);
        panel.getTable().setName("notesTable");
        panel.setPreferredSize(new Dimension(450, 60));

        return new JScrollPane(panel);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        JButton viewButton = new JButton("View");
        viewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                viewNotes();
            }
        });
        buttonPanel.add(viewButton);

        panel.add(buttonPanel, BorderLayout.LINE_START);

        return panel;
    }

    private void viewNotes() {
        List selected = selectModel.selected();
        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            ViewInputWindow view = new ViewInputWindow(desktopManager);
            presenter.doViewNote((CaseInput) iter.next(), view);    
        }
    }
}

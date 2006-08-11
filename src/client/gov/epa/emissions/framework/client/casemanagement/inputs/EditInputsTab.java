package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.SortFilterSelectModel;
import gov.epa.emissions.commons.gui.SortFilterSelectionPanel;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class EditInputsTab extends JPanel implements EditInputsTabView {

    private EmfConsole parentConsole;

    private InputsTableData tableData;

    private SortFilterSelectModel selectModel;

    private JPanel tablePanel;

    private ManageChangeables changeables;

    private MessagePanel messagePanel;

    private DesktopManager desktopManager;

    public EditInputsTab(EmfConsole parentConsole, ManageChangeables changeables, MessagePanel messagePanel,
            DesktopManager desktopManager) {
        super.setName("editInputsTab");
        this.parentConsole = parentConsole;
        this.changeables = changeables;
        this.messagePanel = messagePanel;
        this.desktopManager = desktopManager;

        super.setLayout(new BorderLayout());
    }

    public void display(Case caseObj, EditInputsTabPresenter presenter) {
        super.removeAll();
        super.add(createLayout(caseObj, presenter, parentConsole), BorderLayout.CENTER);
    }

    private JPanel createLayout(Case caseObj, EditInputsTabPresenter presenter, EmfConsole parentConsole) {
        JPanel layout = new JPanel(new BorderLayout());

        layout.add(tablePanel(caseObj, parentConsole), BorderLayout.CENTER);
        layout.add(controlPanel(presenter), BorderLayout.PAGE_END);

        return layout;
    }

    private JPanel tablePanel(Case caseObj, EmfConsole parentConsole) {
        tableData = new InputsTableData(caseObj.getCaseInputs());
        changeables.addChangeable(tableData);
        selectModel = new SortFilterSelectModel(new EmfTableModel(tableData));

        tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(createSortFilterPanel(parentConsole), BorderLayout.CENTER);

        return tablePanel;
    }

    private JScrollPane createSortFilterPanel(EmfConsole parentConsole) {
        SortFilterSelectionPanel sortFilterPanel = new SortFilterSelectionPanel(parentConsole, selectModel);

        JScrollPane scrollPane = new JScrollPane(sortFilterPanel);
        sortFilterPanel.setPreferredSize(new Dimension(450, 60));
        return scrollPane;
    }

    private JPanel controlPanel(final EditInputsTabPresenter presenter) {
        JPanel container = new JPanel();

        Button add = new Button("Add", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doNewInput(presenter);
            }
        });
        container.add(add);
 
        Button edit = new Button("Edit", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //
            }
        });
        container.add(edit);

        Button remove = new Button("Remove", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //
            }
        });
        container.add(remove);

        Button view = new Button("View Dataset", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doViewInput(presenter);
            }
        });
        container.add(view);
        
        Button showAll = new Button("Show All", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //
            }
        });
        container.add(showAll);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(container, BorderLayout.WEST);

        return panel;
    }

    protected void doViewInput(EditInputsTabPresenter presenter) {
        List inputs = selectModel.selected();
        for (Iterator iter = inputs.iterator(); iter.hasNext();) {
            ViewInputWindow window = new ViewInputWindow(desktopManager);
            presenter.doViewInput((CaseInput) iter.next(), window);
        }
    }

    protected void doNewInput(EditInputsTabPresenter presenter) {
        NewInputDialog view = new NewInputDialog(parentConsole);
        try {
            presenter.doAddInput(view);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    public void addInput(CaseInput note) {
        tableData.add(note);
        selectModel.refresh();

        tablePanel.removeAll();
        tablePanel.add(createSortFilterPanel(parentConsole));
    }

    public CaseInput[] additions() {
        return tableData.additions();
    }

}

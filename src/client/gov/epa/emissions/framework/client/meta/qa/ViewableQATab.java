package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.gui.EditableTable;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.EditableEmfTableModel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public class ViewableQATab extends JPanel implements ViewableQATabView {

    private EditableQAStepTableData tableData;

    private EditableEmfTableModel tableModel;

    private EditableTable table;
    
    private DesktopManager desktopManager;

    public ViewableQATab(DesktopManager desktopManager) {
        super.setName("aqsteps");
        this.desktopManager = desktopManager;
    }

    public void display(QAStep[] steps) {
        super.setLayout(new BorderLayout());
        super.add(createQAStepsTableSection(steps), BorderLayout.PAGE_START);
        super.add(createButtonsSection(), BorderLayout.CENTER);
        super.setSize(new Dimension(700, 300));
    }
    
    private JPanel createQAStepsTableSection(QAStep[] steps) {
        JPanel container = new JPanel(new BorderLayout());
        container.add(table(steps), BorderLayout.CENTER);
        return container;
    }

    protected JScrollPane table(QAStep[] steps) {
        tableData = new EditableQAStepTableData(steps);
        tableModel = new EditableEmfTableModel(tableData);
        table = new EditableTable(tableModel);
        table.setRowHeight(16);
        table.setPreferredScrollableViewportSize(new Dimension(500, 320));

        return new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    private JPanel createButtonsSection() {
        JPanel container = new JPanel();

        JButton view = new JButton("View");
        view.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                viewQASteps();
            }
        });
        container.add(view);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(container, BorderLayout.LINE_START);

        return panel;
    }

    private void viewQASteps() {
        QAStep[] steps = tableData.getSelected();
        
        for(int i = 0; i < steps.length; i++) {
            ViewQAStepWindow view = new ViewQAStepWindow(steps[i].getName(), desktopManager);
            ViewQAStepPresenter presenter = new ViewQAStepPresenter(view);
            presenter.display(steps[i]);
        }
    }

}

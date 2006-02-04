package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ScrollableTextArea;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.ui.EmfTableModel;
import gov.epa.emissions.framework.ui.ScrollableTable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

public class NonEditableTableViewPanel extends JPanel implements NonEditableTableView {

    private EmfTableModel tableModel;

    private InternalSource source;

    private JPanel pageContainer;

    private PaginationPanel paginationPanel;

    private ScrollableTable table;

    private TablePresenter presenter;

    private MessagePanel messagePanel;

    public NonEditableTableViewPanel(InternalSource source, MessagePanel messagePanel) {
        super(new BorderLayout());
        this.source = source;
        this.messagePanel = messagePanel;

        doLayout(messagePanel);
    }

    private void setBorder() {
        Border outer = BorderFactory.createEmptyBorder(10, 5, 10, 5);
        Border inner = BorderFactory.createLineBorder(Color.GRAY);
        CompoundBorder border = BorderFactory.createCompoundBorder(outer, inner);

        super.setBorder(border);
    }

    private void doLayout(MessagePanel messagePanel) {
        super.add(topPanel(messagePanel), BorderLayout.PAGE_START);

        pageContainer = new JPanel(new BorderLayout());
        super.add(pageContainer, BorderLayout.CENTER);

        add(notesPanel(), BorderLayout.PAGE_END);
        setBorder();
    }

    private JPanel topPanel(MessagePanel messagePanel) {
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(sortFilterPanel(), BorderLayout.LINE_START);

        paginationPanel = new PaginationPanel(messagePanel);
        panel.add(paginationPanel, BorderLayout.LINE_END);

        return panel;
    }

    private JPanel sortFilterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel rowFilterPanel = new JPanel();
        rowFilterPanel.add(new Label("Row Filter"));
        final TextField rowFilter = new TextField("rowFilter", 30);
        rowFilterPanel.add(rowFilter);
        panel.add(rowFilterPanel);

        JPanel sortOrderPanel = new JPanel();
        sortOrderPanel.add(new Label("Sort Order"));
        final TextField sortOrder = new TextField("sortOrder", 30);
        sortOrderPanel.add(sortOrder);
        panel.add(sortOrderPanel);

        JPanel action = new JPanel(new BorderLayout());
        Button apply = new Button("Apply", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    presenter.applyConstraints(rowFilter.getText(), sortOrder.getText());
                    messagePanel.setMessage("Constraints applied - Row Filter(" + rowFilter.getText()
                            + "}, Sort Order(" + sortOrder.getText() + ")");
                } catch (EmfException ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        });
        action.add(apply, BorderLayout.LINE_END);
        panel.add(action);

        return panel;
    }

    private JPanel notesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel notesLabelPanel = new JPanel(new BorderLayout());
        notesLabelPanel.add(new JLabel("Notes"), BorderLayout.LINE_START);
        panel.add(notesLabelPanel);

        TextArea notes = new TextArea("Notes", "Notes will be coming soon...");
        notes.setEditable(false);
        panel.add(new ScrollableTextArea(notes));

        return panel;
    }

    public void observe(TablePresenter presenter) {
        this.presenter = presenter;
        paginationPanel.init(presenter);
    }

    public void display(Page page) {
        // TODO: refresh table w/ new data? or remove and add back the table?
        pageContainer.removeAll();

        paginationPanel.updateStatus(page);
        pageContainer.add(table(page), BorderLayout.CENTER);
    }

    private ScrollableTable table(Page page) {
        tableModel = new EmfTableModel(new ViewablePage(page, cols()));
        table = new ScrollableTable(tableModel);
        return table;
    }

    // Filter out the first four (version-specific cols)
    private String[] cols() {
        // TODO: should these cols come from the Page/Service?
        List cols = new ArrayList();

        String[] allCols = source.getCols();
        for (int i = 4; i < allCols.length; i++)
            cols.add(allCols[i]);

        return (String[]) cols.toArray(new String[0]);
    }

    public void scrollToPageEnd() {
        table.selectLastRow();
    }

}

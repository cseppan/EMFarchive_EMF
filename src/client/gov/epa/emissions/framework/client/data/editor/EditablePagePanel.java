package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.ClipBoardCopy;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.data.ObserverPanel;
import gov.epa.emissions.framework.client.data.viewer.TablePresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.ui.EditableEmfTableModel;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.ScrollableTable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

public class EditablePagePanel extends JPanel {

    private EditableEmfTableModel tableModel;

    private ScrollableTable table;

    private MessagePanel messagePanel;

    private ManageChangeables listOfChangeables;

    private DataEditorTable editableTable;

    private JTextArea rowFilter;

    private JTextArea sortOrder;

    private ObserverPanel observer;

    private DesktopManager desktopManager;

    private EmfSession emfSession;

    private TablePresenter tablePresenter;
    
    private EditablePage tableData;

    public EditablePagePanel(EditablePage page, ObserverPanel observer, MessagePanel messagePanel,
            ManageChangeables listOfChangeables) {
        this.listOfChangeables = listOfChangeables;
        this.messagePanel = messagePanel;
        this.tableData = page; 
        this.observer = observer;
        setupLayout();
    }
    
    private void setupLayout(){
        super.removeAll();
        super.setLayout(new BorderLayout());
        super.add(mainPanel(), BorderLayout.CENTER);
        super.validate();
    }

    private JPanel mainPanel() {
        JPanel container = new JPanel(new BorderLayout());
        JToolBar toolBar = toolBar();
        container.add(toolBar, BorderLayout.PAGE_START);
        container.add(table(), BorderLayout.CENTER);

        setBorder();
        return container;
    }

    private JToolBar toolBar() {
        JToolBar toolbar = new JToolBar();
        String insertAbove = "/toolbarButtonGraphics/table/RowInsertBefore" + 24 + ".gif";
        ImageIcon iconAbove = createImageIcon(insertAbove);
        String nameAbove = "Insert Above";
        JButton buttonAbove = toolbar.add(insertRowAction(tableData, true, nameAbove, iconAbove));
        buttonAbove.setToolTipText(nameAbove);

        String insertBelow = "/toolbarButtonGraphics/table/RowInsertAfter" + 24 + ".gif";
        ImageIcon iconBelow = createImageIcon(insertBelow);
        String nameBelow = "Insert Below";
        JButton buttonBelow = toolbar.add(insertRowAction(tableData, false, nameBelow, iconBelow));
        buttonBelow.setToolTipText(nameBelow);

        String delete = "/toolbarButtonGraphics/table/RowDelete" + 24 + ".gif";
        ImageIcon iconDelete = createImageIcon(delete);
        String nameDelete = "Delete";
        JButton buttonDelete = toolbar.add(deleteAction(tableData, nameDelete, iconDelete));
        buttonDelete.setToolTipText(nameDelete);

        String selectAll = "/selectAll.jpeg";
        ImageIcon iconSelectAll = createImageIcon(selectAll);
        String nameSelectAll = "Select All";
        JButton buttonSelectAll = toolbar.add(selectAction(true, tableData, nameSelectAll, iconSelectAll));
        buttonSelectAll.setToolTipText(nameSelectAll);

        String clearAll = "/clearAll.jpeg";
        ImageIcon iconClearAll = createImageIcon(clearAll);
        String nameClearAll = "Clear All";
        JButton buttonClearAll = toolbar.add(selectAction(false, tableData, nameClearAll, iconClearAll));
        buttonClearAll.setToolTipText(nameClearAll);

        String replace = "/toolbarButtonGraphics/general/Replace24.gif";
        ImageIcon iconReplace = createImageIcon(replace);
        String replaceTip = "Find and Replace Column Values";
        JButton buttonReplace = toolbar.add(replaceAction(tableData, replaceTip, iconReplace));
        buttonReplace.setToolTipText(replaceTip);

        return toolbar;
    }

    private void setBorder() {
        Border outer = BorderFactory.createEmptyBorder(10, 5, 10, 5);
        Border inner = BorderFactory.createLineBorder(Color.GRAY);
        CompoundBorder border = BorderFactory.createCompoundBorder(outer, inner);

        super.setBorder(border);
    }

    private JScrollPane table() {
        if ( tableModel == null ){
            Font monospacedFont = new Font("Monospaced", Font.LAYOUT_NO_LIMIT_CONTEXT, 12);
            tableModel = new EditableEmfTableModel(tableData);
            editableTable = new DataEditorTable(tableModel, tableData.getTableMetadata(), messagePanel);
            listOfChangeables.addChangeable(editableTable);

            table = new ScrollableTable(editableTable, monospacedFont);
            addCopyPasteClipBoard(editableTable);
        }
        else{
            tableModel.refresh(tableData);
            editableTable.setModel(tableModel);
            table.repaint();
        }
        return table;
    }

    private void addCopyPasteClipBoard(JTable viewTable) {
        ClipBoardCopy clipBoardCopy = new ClipBoardCopy(viewTable);
        clipBoardCopy.registerCopyKeyStroke();
    }

    private AbstractAction deleteAction(final EditablePage tableData, String nameDelete, ImageIcon iconDelete) {
        return new AbstractAction(nameDelete, iconDelete) {
            public void actionPerformed(ActionEvent e) {
                try {
                    messagePanel.clear();
                    doRemove(tableData);
                } catch (Exception e1) {
                    e1.printStackTrace();
                    messagePanel.setError(e1.getMessage());
                }
            }
        };
    }

    private Action insertRowAction(final EditablePage tableData, final boolean above, String name, ImageIcon icon) {
        return new AbstractAction(name, icon) {
            public void actionPerformed(ActionEvent e) {
                doAdd(tableData, editableTable, above);
            }
        };
    }

    private Action selectAction(final boolean select, final EditablePage tableData, String name, ImageIcon icon) {
        return new AbstractAction(name, icon) {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();

                if (select)
                    tableData.selectAll();
                else
                    tableData.clearAll();

                refresh();
            }
        };
    }

    private Action replaceAction(final EditablePage tableData, String replaceTip, ImageIcon icon) {
        return new AbstractAction(replaceTip, icon) {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();

                FindReplaceWindowView dialog = new DataFindReplaceWindow(tableData.getDatasetName(), tableData
                        .getTableMetadata().getTable(), tableData.getVersion(), rowFilter, sortOrder,
                        getDesktopManager(), removeFirstCol(tableData.columns()), listOfChangeables);
                FindReplaceViewPresenter findReplacePresenter = new FindReplaceViewPresenter(tablePresenter, dialog,
                        emfSession);
                findReplacePresenter.displayView();
            }
        };
    }

    protected void clearMessagesWithTableRefresh() {
        messagePanel.clear();
        refresh();
    }

    private void refresh() {
        tableModel.refresh();
        super.revalidate();
    }
    
    public void refresh(EditablePage page){
        this.tableData =page; 
        setupLayout();
    }

    private void doAdd(final EditablePage tableData, DataEditorTable editableTable, boolean above) {
        int selectedRow = editableTable.getSelectedRow();
        messagePanel.clear();
        if (selectedRow != -1 || editableTable.getRowCount() == 0) {
            int insertRowNo = insertRowNumber(above, selectedRow, editableTable);
            tableData.addBlankRow(insertRowNo);
            refresh();
            editableTable.setRowSelectionInterval(insertRowNo, insertRowNo);
        } else {
            messagePanel.setError("Please highlight a row before clicking the insert button");
        }

        this.observer.update(1);
    }

    private int insertRowNumber(boolean above, int selectedRow, DataEditorTable editableTable) {
        if (editableTable.getRowCount() == 0) {
            return 0;
        }
        return (above) ? selectedRow : selectedRow + 1;
    }

    private void doRemove(final EditablePage tableData) throws EmfException {
        clearMessagesWithTableRefresh();

        int selected = tableData.getSelected().length;

        if (selected == 0) {
            messagePanel.setError("Please select at least one row to delete.");
            return;
        }

        int option = JOptionPane.NO_OPTION;

        if (selected == tableData.rows().size())
            option = deleteRecords(tableData, getRowCount(tableData));

        if (option != JOptionPane.NO_OPTION)
            return;

        String msg = "Are you sure you want to delete the selected " + selected + " row" + (selected > 1 ? "s" : "")
                + "?";
        int regularDel = JOptionPane.showConfirmDialog((Component) listOfChangeables, msg, "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);

        if (regularDel == JOptionPane.YES_OPTION) {
            tableData.removeSelected();
            int numOfDeltd = tableData.changeset().getDeletedRecords().length;
            this.observer.update(-numOfDeltd);
            refresh();
        }
    }

    private int deleteRecords(final EditablePage tableData, int rowCount) throws EmfException {
        String ls = System.getProperty("line.separator");
        String msg = "You have chosen to delete all records on the current page. " + ls
                + "Would you also like to delete records not shown on this page";
        String filter = rowFilter.getText();

        if (filter != null && !filter.trim().isEmpty()) {
            msg += ls + " that match the row filter:" +ls + filter.trim();
        }

        msg += "?";

        String title = "Delete All Records";

        int option = JOptionPane.YES_OPTION;
        
        if (rowCount > tableData.rows().size()) {
            option = JOptionPane.showConfirmDialog((Component) listOfChangeables, msg, title,
                    JOptionPane.YES_NO_CANCEL_OPTION);

            if (option == JOptionPane.CANCEL_OPTION)
                return JOptionPane.CANCEL_OPTION;
        }

        if (option == JOptionPane.YES_OPTION) {
            String confirm = "This deletion of " + rowCount
                    + " records is not reversible. Are you sure you want to proceed?";
            int goDelete = JOptionPane.showConfirmDialog((Component) listOfChangeables, confirm, title,
                    JOptionPane.YES_NO_OPTION);

            if (goDelete == JOptionPane.YES_OPTION) {
                try {
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    tableData.removeSelected();
                    deleteAllRecords(tableData);
                    refresh();
                    observer.refresh(rowFilter.getText(), sortOrder.getText());
                } catch (RuntimeException e) {
                    e.printStackTrace();
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }

            return JOptionPane.YES_OPTION;
        }

        return JOptionPane.NO_OPTION;
    }

    private void deleteAllRecords(EditablePage tableData) {
        DataService service = emfSession.dataService();
        Version version = tableData.getVersion();
        String filter = (rowFilter.getText() == null ? "" : rowFilter.getText());
        User user = emfSession.user();
        String table = tableData.getTable();

        try {
            service.deleteRecords(user, table, version, filter);
        } catch (EmfException e) {
            e.printStackTrace();
            messagePanel.setError(e.getMessage());
        }
    }

    private int getRowCount(EditablePage tableData) throws EmfException {
        DataService service = emfSession.dataService();
        Version version = tableData.getVersion();
        String filter = (rowFilter.getText() == null ? "" : rowFilter.getText());
        String table = tableData.getTable();

        return service.getNumOfRecords(table, version, filter);
    }

    public void scrollToPageEnd() {
        table.moveToBottom();
    }

    protected ImageIcon createImageIcon(String path) {
        URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        }
        messagePanel.setError("Could not find file: " + path);
        return null;
    }

    private String[] removeFirstCol(String[] cols) {
        List<String> newCols = new ArrayList<String>();

        for (int i = 1; i < cols.length; i++)
            newCols.add(cols[i]);

        return newCols.toArray(new String[0]);
    }

    public DesktopManager getDesktopManager() {
        return this.desktopManager;
    }

    public void setDesktopManager(DesktopManager desktopManager) {
        this.desktopManager = desktopManager;
    }

    public void setEmfSession(EmfSession session) {
        this.emfSession = session;
    }

    public void setRowFilter(JTextArea filter) {
        this.rowFilter = filter;
    }

    public void setTablePresenter(TablePresenter tablePresenter) {
        this.tablePresenter = tablePresenter;
    }

    public void setSortOrder(JTextArea sortOrderText) {
        this.sortOrder = sortOrderText;
    }
    

}

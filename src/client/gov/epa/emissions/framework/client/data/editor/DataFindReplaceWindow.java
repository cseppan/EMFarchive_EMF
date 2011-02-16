package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;

public class DataFindReplaceWindow extends ReusableInteralFrame implements FindReplaceWindowView {

    private String[] cols = new String[0];

    private SingleLineMessagePanel messagePanel;

    private FindReplaceViewPresenter presenter;

    private JPanel layout;

    private ComboBox columnNames;

    private TextField find;

    private TextField replaceWith;

    private JLabel filterLabel;

    private Button okButton;

    private Version version;

    private String table;

    private JTextArea sortOrder;
    
    private ManageChangeables listOfChangeables;

    public DataFindReplaceWindow(String dsName, String table, Version version, JTextArea filter, JTextArea sortOrder,
            DesktopManager desktopManager, String[] cols, ManageChangeables listOfChangeables) {
        super("Find and Replace Column Values", new Dimension(500, 260), desktopManager);
        super.setLabel("Find and Replace Column Values: " + dsName + " (version: " + version.getVersion() + ")");
        
        this.cols = cols;
        this.table = table;
        this.version = version;
        this.listOfChangeables = listOfChangeables;
        this.filterLabel = new JLabel(filter.getText() == null || filter.getText().trim().isEmpty() ? "NO FILTER"
                : filter.getText().trim());
        this.sortOrder = sortOrder;

        layout = new JPanel();
        this.getContentPane().add(layout);
    }

    public void display() {
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();

        layout.add(messagePanel);
        layout.add(selectionPanel());
        layout.add(createButtonPanel());

        setResizable(true);
        super.display();
    }

    private JPanel selectionPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Make Selections",
                0, 0, Font.decode(""), Color.BLUE));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        layoutGenerator.addLabelWidgetPair("Row Filter ", filterLabel, panel);

        columnNames = new ComboBox("Select a column", this.cols);
        columnNames.setToolTipText("Select a column name to find and replace column values");
        layoutGenerator.addLabelWidgetPair("Column  ", columnNames, panel);

        find = new TextField("findColValue", 30);
        layoutGenerator.addLabelWidgetPair("Find  ", find, panel);

        replaceWith = new TextField("replaceColValuesWith", 30);
        layoutGenerator.addLabelWidgetPair("Replace with  ", replaceWith, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 4, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 5);// xPad, yPad

        return panel;
    }

    private Component createButtonPanel() {
        JPanel panel = new JPanel();

        okButton = new Button("Apply", okAction());
        panel.add(okButton);

        Button closeButton = new Button("Close", closeWindowAction());
        panel.add(closeButton);

        return panel;
    }

    public void observe(FindReplaceViewPresenter presenter) {
        this.presenter = presenter;
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                clearMsgPanel();

                try {
                    if (!validateFields())
                        return;

                    String col = columnNames.getSelectedItem().toString();
                    String findString = (find.getText() == null) ?  "": find.getText().trim();
                    String replaceString = (replaceWith.getText() == null) ? "" : replaceWith.getText().trim();
                    String rowFilter = (filterLabel.getText().equals("NO FILTER")) ? "" : filterLabel.getText().trim();

                    presenter.replaceColValues(table, col, findString, replaceString, version, rowFilter);
                    presenter.applyConstraint(rowFilter, sortOrder.getText().trim());
                    resetDataeditorRevisionField();
                    setMsg("Successfully replaced column values.");
                } catch (EmfException e) {
                    if (!e.getMessage().trim().isEmpty())
                        setErrorMsg(e.getMessage());
                }
            }
        };
    }

    private boolean validateFields() throws EmfException {
        String findString = (find.getText() == null) ?  "": find.getText();
        String replaceString = (replaceWith.getText() == null) ? "" : replaceWith.getText().trim();
        
        if (columnNames.getSelectedItem() == null)
            throw new EmfException("Please select a valid column");
        if (findString.equals(replaceString))
            throw new EmfException("Please specify different find and replace values.");
        if (replaceString.isEmpty()){
            String message = "Replace field is empty, would you like to continue?";
            int selection = JOptionPane.showConfirmDialog( this, message, "Warning", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (selection == JOptionPane.NO_OPTION)   
                return false;
        }
        return true; 
    }
    
    private void resetDataeditorRevisionField() {
        boolean nofilter = filterLabel.getText().equals("NO FILTER");
        ((DataEditor)listOfChangeables).setHasReplacedValues(true);
        ((DataEditor)listOfChangeables).append2WhatField("Replaced '" + find.getText() + "' with '" + replaceWith.getText() + "' for column " +
                columnNames.getSelectedItem().toString() + (nofilter ? "" : " using filter '" + filterLabel.getText() + "'"));
    }

    private Action closeWindowAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                disposeView();
            }
        };
    }

    private void clearMsgPanel() {
        messagePanel.clear();
    }

    public void setErrorMsg(String errorMsg) {
        messagePanel.setError(errorMsg);
    }

    private void setMsg(String msg) {
        messagePanel.setMessage(msg);
    }

}

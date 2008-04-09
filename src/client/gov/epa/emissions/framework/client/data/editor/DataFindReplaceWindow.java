package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
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
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class DataFindReplaceWindow extends ReusableInteralFrame implements FindReplaceWindowView {

    private String[] cols = new String[0];

    private SingleLineMessagePanel messagePanel;

    private JPanel layout;

    private ComboBox columnNames;

    private TextField find;

    private TextField replaceWith;

    private Button okButton;

    public DataFindReplaceWindow(DesktopManager desktopManager, String[] cols) {
        super("Find and Replace Column Values", new Dimension(500, 260), desktopManager);

        this.cols = cols;
        layout = new JPanel();
        this.getContentPane().add(layout);
    }

    public void display() {
        setTitle("Find and Replace Column Values");
        setName("Find and Replace Column Values " + Math.random());
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
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY),
                "Make Selections", 0, 0, Font.decode(""), Color.BLUE));
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        columnNames = new ComboBox("Select a column", this.cols);
        columnNames.setToolTipText("Select a column name to find and replace column values");
        layoutGenerator.addLabelWidgetPair("Column  ", columnNames, panel);
        
        find = new TextField("findColValue", 30);
        layoutGenerator.addLabelWidgetPair("Find  ", find, panel);

        replaceWith = new TextField("replaceColValuesWith", 30);
        layoutGenerator.addLabelWidgetPair("Replace with  ", replaceWith, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 3, 2, // rows, cols
                5, 5, // initialX, initialY
                5, 5);// xPad, yPad

        return panel;
    }

    private Component createButtonPanel() {
        JPanel panel = new JPanel();

        okButton = new Button("OK", okAction());
        panel.add(okButton);

        Button closeButton = new Button("Close", closeWindowAction());
        panel.add(closeButton);

        return panel;
    }

    public void observe(FindReplaceViewPresenter presenter) {
        //
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                clearMsgPanel();
                String msg = "Find and replace column values";
                setMsg(msg);
            }
        };
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

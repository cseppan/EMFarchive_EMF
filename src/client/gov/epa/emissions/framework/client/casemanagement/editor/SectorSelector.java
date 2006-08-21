package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.ui.ListWidget;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class SectorSelector extends JDialog {

    private Sector[] allSectors;

    protected boolean clickedOk;

    private ListWidget widget;

    private Sector[] selectedValues;

    public SectorSelector(Sector[] allSectors, EmfConsole parentConsole) {
        super(parentConsole);
        setTitle("Select Sectors");
        this.allSectors = allSectors;
    }

    public void display() {
        JScrollPane pane = listWidget();
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(pane);
        panel.add(buttonPanel(), BorderLayout.SOUTH);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(panel);

        pack();
        setSize(300,300);
        setLocation(ScreenUtils.getPointToCenter(this));
        setModal(true);
        setVisible(true);
    }

    private JScrollPane listWidget() {
        widget = new ListWidget(allSectors);
        JScrollPane pane = new JScrollPane(widget);
        return pane;
    }

    private JPanel buttonPanel() {
        OKButton okButton = new OKButton(okAction());
        CancelButton cancelButton = new CancelButton(cancelAction());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        return buttonPanel;
    }

    private Action cancelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                disposeView();
                clickedOk = false;
            }

        };
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setSelectedValues();
                disposeView();
                clickedOk = true;
            }

        };
    }

    private void disposeView() {
        dispose();
        setVisible(false);
    }

    public boolean clickedOK() {
        return clickedOk;
    }

    public Sector[] getSelected() {
        return selectedValues;
    }

    private void setSelectedValues() {
        Object[] values = widget.getSelectedValues();
        selectedValues = new Sector[values.length];
        for (int i = 0; i < selectedValues.length; i++) {
            selectedValues[i] = (Sector) values[i];
        }
    }

}

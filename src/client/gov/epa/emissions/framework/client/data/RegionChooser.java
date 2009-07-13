package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.EmfImageTool;
import gov.epa.emissions.framework.services.data.GeoRegion;
import gov.epa.emissions.framework.ui.ListWidget;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class RegionChooser extends JDialog {

    private GeoRegion[] allRegions;

    private ListWidget allGridsListwidget;

    private ListWidget gridsListWidget;

    public RegionChooser(GeoRegion[] allGrids, ListWidget gridsListWidget, EmfConsole parentConsole) {
        super(parentConsole);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
        
        setTitle("Select Regions");
        this.allRegions = allGrids;
        this.gridsListWidget = gridsListWidget;
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
        setSize(300, 300);
        setLocation(ScreenUtils.getPointToCenter(this));
        setModal(true);
        setVisible(true);
    }

    private JScrollPane listWidget() {
        allGridsListwidget = new ListWidget(allRegions);
        JScrollPane pane = new JScrollPane(allGridsListwidget);
        return pane;
    }

    private JPanel buttonPanel() {
        Button newRegionButton = new Button("New", addNewRegionAction());
        newRegionButton.setEnabled(false);
        OKButton okButton = new OKButton(okAction());
        CancelButton cancelButton = new CancelButton(cancelAction());
        
        JPanel buttonPanel = new JPanel();
        
        buttonPanel.add(newRegionButton);
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        return buttonPanel;
    }

    private Action cancelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                disposeView();
            }
        };
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setSelectedValues();
                disposeView();
            }
        };
    }
    
    private Action addNewRegionAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                disposeView();
            }
        };
    }

    private void disposeView() {
        dispose();
        setVisible(false);
    }

    private void setSelectedValues() {
        Object[] values = allGridsListwidget.getSelectedValues();
        GeoRegion[] selectedValues = Arrays.asList(values).toArray(new GeoRegion[0]);
        addNewGrids(selectedValues);
    }

    private void addNewGrids(GeoRegion[] selected) {
        for (int i = 0; i < selected.length; i++) {
            if (!gridsListWidget.contains(selected[i]))
                gridsListWidget.addElement(selected[i]);
        }
        sort(); 
    }
    
    private void sort() {
        GeoRegion[] grids = Arrays.asList(gridsListWidget.getAllElements()).toArray(new GeoRegion[0]);
        Arrays.sort(grids);
        gridsListWidget.removeAllElements();
        for (int i = 0; i < grids.length; i++) {
            gridsListWidget.addElement(grids[i]);
        }
    }
   
}

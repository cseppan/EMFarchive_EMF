package gov.epa.emissions.framework.client.data.region;

import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class NewRegionWindow extends DisposableInteralFrame implements NewRegionView {

    //private ListWidget allGridsListwidget;
    
    private EmfConsole parent;

    private EmfSession session;
    
    private NewRegionPresenterImp presenter;
    
    private MessagePanel messagePanel;

    private NewRegionPanel newRegionPanel;

    public NewRegionWindow(DesktopManager desktopManager,
            EmfConsole parentConsole, EmfSession session ) {
        super("New Region Window", new Dimension(600, 640), desktopManager);
        
        this.parent = parentConsole;
        this.session = session;
    }

    public void display() {
        JPanel layout = null;
        try {
            layout = createLayout();
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        super.getContentPane().add(layout);
        super.display();
        super.resetChanges();
    }

    private JPanel createLayout() throws EmfException{
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);
        this.newRegionPanel = new NewRegionPanel(messagePanel, this, parent, session);
        presenter.doAddRegionFields(panel, newRegionPanel);
        panel.add(buttonPanel());
        return panel;
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
            }
        };
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //setSelectedValues();
                disposeView();
            }
        };
    }
    
//    private Action addNewRegionAction() {
//        return new AbstractAction() {
//            public void actionPerformed(ActionEvent e) {
//                disposeView();
//            }
//        };
//    }

//    private void setSelectedValues() {
//        Object[] values = allGridsListwidget.getSelectedValues();
//        GeoRegion[] selectedValues = Arrays.asList(values).toArray(new GeoRegion[0]);
//        addNewGrids(selectedValues);
//    }

//    private void addNewGrids(GeoRegion[] selected) {
//        for (int i = 0; i < selected.length; i++) {
//            if (!gridsListWidget.contains(selected[i]))
//                gridsListWidget.addElement(selected[i]);
//        }
//        sort(); 
//    }
    
//    private void sort() {
//        GeoRegion[] grids = Arrays.asList(gridsListWidget.getAllElements()).toArray(new GeoRegion[0]);
//        Arrays.sort(grids);
//        gridsListWidget.removeAllElements();
//        for (int i = 0; i < grids.length; i++) {
//            gridsListWidget.addElement(grids[i]);
//        }
//    }

    public void observe(NewRegionPresenterImp presenter) {
        this.presenter = presenter;
        
    }

}

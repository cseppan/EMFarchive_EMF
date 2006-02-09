package gov.epa.emissions.framework.client;

import javax.swing.JOptionPane;

import gov.epa.emissions.commons.gui.ChangeablesList;

public class WidgetChangesMonitor {
    private ChangeablesList list;
    
    private EmfInternalFrame window;
    
    public WidgetChangesMonitor(ChangeablesList list, EmfInternalFrame window) {
        this.list = list;
        this.window = window;
    }
    
    public void resetChanges() {
        list.resetChanges();
        list.onChanges();
    }
    
    public int checkChanges() {
        int option = JOptionPane.OK_OPTION;
        String message = "Would you like to discard the changes " + System.getProperty("line.separator") + 
                " and close the current window?";
        String title = "Please choose one";
        if(list.hasChanges())
            option = JOptionPane.showConfirmDialog(window, message, title, JOptionPane.OK_CANCEL_OPTION);
        return option;
    }

}

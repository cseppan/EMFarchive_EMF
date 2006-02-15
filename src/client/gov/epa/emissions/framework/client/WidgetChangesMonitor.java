package gov.epa.emissions.framework.client;

import gov.epa.emissions.commons.gui.ChangeablesList;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.ui.EmfDialog;

import javax.swing.JOptionPane;

public class WidgetChangesMonitor {
    private ChangeablesList list;
    
   private EmfConsole window;
    
    public WidgetChangesMonitor(ChangeablesList list, EmfConsole window) {
        this.list = list;
        this.window = window;
    }
    
    public void resetChanges() {
        list.resetChanges();
        list.onChanges();
    }
    
    public boolean checkChanges() {
        String message = "Would you like to discard the changes " + System.getProperty("line.separator") + 
                " and close the current window?";
        String title = "Please choose one";
        if(list.hasChanges()) {
            EmfDialog dialog = new EmfDialog(window, title, JOptionPane.QUESTION_MESSAGE,
                    message, JOptionPane.OK_CANCEL_OPTION);
            return dialog.confirm();
        }
        
        return false;
    }

}

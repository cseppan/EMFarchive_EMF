package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class EditControlStrategyOutputTab extends JPanel implements EditControlStrategyTabView {
    
    private TextField folderName;

    public EditControlStrategyOutputTab(ControlStrategy controlStrategy, EmfSession session, SingleLineMessagePanel messagePanel, EmfConsole parentConsole) {
        super.setName("output");
        setLayout();
    }

    private void setLayout() {
        setLayout(new BorderLayout());
        add(folderPanel(),BorderLayout.CENTER);
    }
    
    private JPanel folderPanel(){
        JLabel folderLabel = new JLabel("Folder: ");
        folderName = new TextField("folderName",30);
        Button browseButton = new Button("Browse",browseAction());
        JPanel panel = new JPanel();
        panel.add(folderLabel);
        panel.add(folderName);
        panel.add(browseButton);
        
        return panel;
    }

    private Action browseAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //
            }
        };
    }

    public void save(ControlStrategy controlStrategy) {
        // NOTE Auto-generated method stub

    }

}

package gov.epa.emissions.framework.ui;

import gov.epa.emissions.framework.client.console.EmfConsole;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;

public class EmfDialog extends JDialog {
    private EmfConsole parent;
    
    private int messageType;
    
    private Object message;
    
    private int optionType;

    public EmfDialog(EmfConsole parent, String title, int messageType,
            Object message, int optionType) {
        super(parent, title, true);
        super.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.parent = parent;
        this.messageType = messageType;
        this.message = message;
        this.optionType = optionType;
    }
    
    public int showDialog() {
        JOptionPane pane = new JOptionPane(message, messageType, optionType, null, null, null);
        pane.setInitialValue(null);
        pane.setComponentOrientation(parent.getComponentOrientation());
        pane.selectInitialValue();

        composeDialog(pane);
        
        show();
        dispose();
        
        Object selectedValue = pane.getValue();
        
        if(selectedValue == null)
            return JOptionPane.CLOSED_OPTION;
        
        if(selectedValue instanceof Integer)
            return ((Integer)selectedValue).intValue();
        
        return JOptionPane.CLOSED_OPTION;
    }
    
    private void setLookAndFeel() {
        dispose();
        setUndecorated(true);
        getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
        setResizable(false);
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void composeDialog(final JOptionPane pane) {
        Container contentPane = this.getContentPane();

        contentPane.setLayout(new BorderLayout());
        contentPane.add(pane, BorderLayout.CENTER);
        setLookAndFeel();
        
        addWindowListener(pane);
        addComponentListener(pane);
        addPropertyChangeListener(pane);
    }
    
    private void addWindowListener(final JOptionPane pane) {
        addWindowListener(new WindowAdapter() {
            private boolean gotFocus = false;
            public void windowClosing(WindowEvent we) {
                pane.setValue(null);
            }
            public void windowGainedFocus(WindowEvent we) {
                // Once window gets focus, set initial focus
                if (!gotFocus) {
                    pane.selectInitialValue();
                    gotFocus = true;
                }
            }
        });
    }
    
    private void addComponentListener(final JOptionPane pane) {
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent ce) {
            // reset value to ensure closing works properly
                pane.setValue(JOptionPane.UNINITIALIZED_VALUE);
            }
        });
    }
    
    private void addPropertyChangeListener(final JOptionPane pane) {
        pane.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                // Let the defaultCloseOperation handle the closing
                // if the user closed the window without selecting a button
                // (newValue = null in that case).  Otherwise, close the dialog.
                if(isVisible() && event.getSource() == pane &&
                        (event.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)) &&
                        event.getNewValue() != null &&
                        event.getNewValue() != JOptionPane.UNINITIALIZED_VALUE) {
                    setVisible(false);               
                }
            }
        });
    }

}

package gov.epa.emissions.framework.ui;

import gov.epa.emissions.commons.gui.Confirm;

import java.awt.Component;
import javax.swing.JOptionPane;

public class EmfDialog implements Confirm {

    private Component parent;

    private int messageType;

    private Object message;

    private int optionType;

    private String title;

    public EmfDialog(Component component, String title, int messageType, Object message, int optionType) {
        this.title = title;
        this.parent = component;
        this.messageType = messageType;
        this.message = message;
        this.optionType = optionType;
    }

    public boolean confirm() {
        int option = showDialogNew();
        return option == JOptionPane.YES_OPTION || option == JOptionPane.OK_OPTION;
    }

    private int showDialogNew() {
        return JOptionPane.showConfirmDialog(parent, message, title, optionType, messageType);
    }

    /*
     * private int showDialog() { JOptionPane pane = new JOptionPane(message, messageType, optionType, null, null,
     * null); pane.setInitialValue(null); if (parent != null)
     * pane.setComponentOrientation(parent.getComponentOrientation()); pane.selectInitialValue();
     * 
     * composeDialog(pane);
     * 
     * show(); dispose();
     * 
     * Object selectedValue = pane.getValue();
     * 
     * if (selectedValue == null) return JOptionPane.CLOSED_OPTION;
     * 
     * if (selectedValue instanceof Integer) return ((Integer) selectedValue).intValue();
     * 
     * return JOptionPane.CLOSED_OPTION; }
     * 
     * private void setLookAndFeel() { dispose(); setUndecorated(true);
     * getRootPane().setWindowDecorationStyle(JRootPane.FRAME); setResizable(false); pack();
     * setLocationRelativeTo(parent); }
     * 
     * private void composeDialog(final JOptionPane pane) { Container contentPane = this.getContentPane();
     * 
     * contentPane.setLayout(new BorderLayout()); contentPane.add(pane, BorderLayout.CENTER); setLookAndFeel();
     * 
     * addWindowListener(pane); addComponentListener(pane); addPropertyChangeListener(pane); }
     * 
     * private void addWindowListener(final JOptionPane pane) { addWindowListener(new WindowAdapter() { private boolean
     * gotFocus = false;
     * 
     * public void windowClosing(WindowEvent we) { pane.setValue(null); }
     * 
     * public void windowGainedFocus(WindowEvent we) { // Once window gets focus, set initial focus if (!gotFocus) {
     * pane.selectInitialValue(); gotFocus = true; } } }); }
     * 
     * private void addComponentListener(final JOptionPane pane) { addComponentListener(new ComponentAdapter() { public
     * void componentShown(ComponentEvent ce) { // reset value to ensure closing works properly
     * pane.setValue(JOptionPane.UNINITIALIZED_VALUE); } }); }
     * 
     * private void addPropertyChangeListener(final JOptionPane pane) { pane.addPropertyChangeListener(new
     * PropertyChangeListener() { public void propertyChange(PropertyChangeEvent event) { // Let the
     * defaultCloseOperation handle the closing // if the user closed the window without selecting a button // (newValue =
     * null in that case). Otherwise, close the dialog. if (isVisible() && event.getSource() == pane &&
     * (event.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)) && event.getNewValue() != null &&
     * event.getNewValue() != JOptionPane.UNINITIALIZED_VALUE) { setVisible(false); } } }); }
     */
}

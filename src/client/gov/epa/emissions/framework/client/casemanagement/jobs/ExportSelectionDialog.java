package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.ExportButton;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.Dialog;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class ExportSelectionDialog extends Dialog {

    private boolean shouldCreateCSV = false;
    
    private boolean shouldCreateShapeFile = false;

    private MessagePanel messagePanel;
    
    private JCheckBox csvFormat;

    private JCheckBox shapeFileFormat;
    
    private TextField pollutantName;
    
    private TextField shapeFileName;
    
//    private EmfConsole parent;
//    
//    private EmfSession session;
    
    
    public ExportSelectionDialog(EmfConsole parent) {
        super("Export QA Step Results " , parent);
        super.setSize(new Dimension(450, 260));
        super.center();
        setModal(true);
//        this.parent = parent;
//        this.session = session;
    }

    public void display() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(mainPanel());
        panel.add(buttonsPanel());

        super.getContentPane().add(panel);
        super.display();
    }

    private JPanel mainPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(1, 1));
        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.NORTH);
        panel.add(new Label("empty", "  "), BorderLayout.LINE_START);
        panel.add(formatBox(),BorderLayout.CENTER);
        
        panel.add(pollAndShape(), BorderLayout.SOUTH);
        return panel;
    }
    
    private JPanel formatBox(){
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new Label("Export Format: "), BorderLayout.NORTH);
        csvFormat = new JCheckBox("CSV");
        csvFormat.setSelected(false);
        panel.add(csvFormat,BorderLayout.CENTER);

        shapeFileFormat = new JCheckBox("ShapeFile");
        shapeFileFormat.setSelected(false);
        shapeFileFormat.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (shapeFileFormat.isSelected()){
                    pollutantName.setEnabled(true);
                    shapeFileName.setEnabled(true);
                }
                if (!shapeFileFormat.isSelected()){
                    pollutantName.setEnabled(false);
                    shapeFileName.setEnabled(false);
                }
            }
        });
        panel.add(shapeFileFormat, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel pollAndShape() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        pollutantName = new TextField("Pollutant", "", 20);
        pollutantName.setEnabled(false);
        layoutGenerator.addLabelWidgetPair("Pollutant to Include: ", pollutantName, panel);
        shapeFileName = new TextField("Output Shapefile Name ", "", 20);
        shapeFileName.setEnabled(false);
        layoutGenerator.addLabelWidgetPair("Output Shapefile Name:", shapeFileName, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 2, 2, // rows, cols
                25, 5, // initialX, initialY
                5, 5);// xPad, yPad

        return panel;
    }

    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        Button ok = new ExportButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                cleareMsg();
                exportFile();
                setVisible(false);
                dispose();
            }
        });
        getRootPane().setDefaultButton(ok);
        panel.add(ok);

        Button cancel = new CancelButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });
        panel.add(cancel);
        
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));

        return panel;
    }

    private void exportFile() {
        try {
            checkShapeFileName();
            if (csvFormat.isSelected())
                shouldCreateCSV = true; 
            if (shapeFileFormat.isSelected())
                shouldCreateShapeFile = true; 
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private void checkShapeFileName() throws EmfException{
        if (shapeFileName.getText().contains("/"))
            throw new EmfException("Shape file name can't contain /");
    }

    private void cleareMsg() {
        this.messagePanel.clear();
    }

   public boolean shouldCreateCSV(){
       return shouldCreateCSV;
   }
   
   public boolean shouldCreateShapeFile(){
       return shouldCreateShapeFile;
   }

}

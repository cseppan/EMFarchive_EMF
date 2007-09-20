package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;


public class EditQAArgumentsWindow extends DisposableInteralFrame implements EditQAArgumentsView{
    
    private JTextArea arguments;
    
    private JPanel layout;
    
    private String textAreaArguments;

    private EditQAArgumentsPresenter presenter;
    
    public EditQAArgumentsWindow(DesktopManager desktopManager, String textAreaArguments) {
        
        super("Argument Editor", new Dimension(750, 350), desktopManager);
        this.textAreaArguments = textAreaArguments;
        this.getContentPane().add(createLayout());
        //System.out.println("Args: " + textAreaArguments);
        
        //System.out.println("Args: " + this.textAreaArguments);
    }

  public void display(EmfDataset dataset, QAStep qaStep) {
      super.setTitle("Edit QA Step Arguments: " + qaStep.getName() + "_" + qaStep.getId()+" ("+dataset.getName()+")");
      super.display();
  }
  
  public void observe(EditQAArgumentsPresenter presenter) {
      this.presenter = presenter;
  }
  
    public JPanel createLayout() {
        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        JPanel content = new JPanel(new SpringLayout());
        //layout = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
       
        //Create the arguments text area and set its text to the text in the 
        // QAStepWindow arguments text area.
        
        arguments = new JTextArea();
        arguments.setWrapStyleWord(true);
        arguments.setLineWrap(true);
        arguments.setText(textAreaArguments);
        
        //Put the text area in a scroll pane and set its size.
       
        ScrollableComponent viewableTextArea = new ScrollableComponent(arguments);
        viewableTextArea.setPreferredSize(new Dimension(650,250));
        
        // Add the text area and button panel to the main window panel.
        layoutGenerator.addLabelWidgetPair("Arguments:", viewableTextArea, content);
        //layoutGenerator.addLabelWidgetPair("", buttonPanel(), layout);
        layoutGenerator.makeCompactGrid(content, 1, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad
        layout.add(content);
        layout.add(buttonPanel());
        
        return layout;
    }

    private JPanel buttonPanel() {
        JPanel panel = new JPanel();
        panel.add(new OKButton(okAction()));
        panel.add(new CancelButton(cancelAction()));
        return panel;
    }

    private Action cancelAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                dispose();
                disposeView();
            }

        };
    }

    private Action okAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                String argumentsText = arguments.getText();
                // Send the modified text from the arguments text area to the one in
                // the QAStepWindow arguments text area.
                presenter.refreshArgs(argumentsText);
                dispose();
                disposeView();
            }
        };
    }
}

package gov.epa.emissions.framework.client.casemanagement.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.ui.NumberFieldVerifier;

import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class EditableCaseParameterTab extends JPanel implements EditableCaseParameterTabView {

    private Case caseObj;
    
    private ManageChangeables changeablesList;
    
    private NumberFieldVerifier verifier;
    
    private TextField numEmisLayer;

    private TextField numMetLayer;
    
    private TextArea gridDescription;
    
    private Dimension defaultDimension = new Dimension(100, 100);
    
    public EditableCaseParameterTab(Case caseObj, ManageChangeables changeablesList) {
        super.setName("summary");
        this.caseObj = caseObj;
        this.changeablesList = changeablesList;
        this.verifier = new NumberFieldVerifier("Parameter Tab:");
    }
    
    
    
    void display() {
        this.setLayout(new BorderLayout());
        this.add(topPanel(), BorderLayout.NORTH);
        this.add(new JPanel());
    }
    
    private JPanel topPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        
        layoutGenerator.addLabelWidgetPair("# of Met. Layers:", numMetLayer(), panel);
        layoutGenerator.addLabelWidgetPair("# of Emissions Layers:", numEmisLayer(), panel);
        layoutGenerator.addLabelWidgetPair("Grid Description:", gridDescription(), panel);

        layoutGenerator.makeCompactGrid(panel, 3, 2, 10, 10, 10, 10);

        return panel;
    }
    
    private TextField numEmisLayer() {
        numEmisLayer = new TextField("Number of Emissions Layers", 10);
        numEmisLayer.setText(caseObj.getNumEmissionsLayers() + "");
        changeablesList.addChangeable(numEmisLayer);
        numEmisLayer.setMaximumSize(defaultDimension);

        return numEmisLayer;
    }

    private TextField numMetLayer() {
        numMetLayer = new TextField("Number of Met Layers", 10);
        numMetLayer.setText(caseObj.getNumMetLayers() + "");
        changeablesList.addChangeable(numMetLayer);
        numMetLayer.setMaximumSize(defaultDimension);

        return numMetLayer;
    }
    
    private ScrollableComponent gridDescription() {
        gridDescription = new TextArea("griddescription", "", 20, 6);
        gridDescription.setText(caseObj.getGridDescription());
        changeablesList.addChangeable(gridDescription);

        ScrollableComponent descScrollableTextArea = new ScrollableComponent(gridDescription);
        descScrollableTextArea.setMinimumSize(defaultDimension);
        return descScrollableTextArea;
    }
    
    public void save(Case caseObj) throws EmfException {
        caseObj.setNumEmissionsLayers(verifier.parseInteger(numEmisLayer));
        caseObj.setNumMetLayers(verifier.parseInteger(numMetLayer));
        caseObj.setGridDescription(gridDescription.getText());
    }

}
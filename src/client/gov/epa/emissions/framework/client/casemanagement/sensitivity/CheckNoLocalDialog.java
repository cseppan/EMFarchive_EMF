package gov.epa.emissions.framework.client.casemanagement.sensitivity;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.ui.Dialog;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.Dimension;

public class CheckNoLocalDialog  extends Dialog{
    
    private SetCasePresenter presenter;
    
    private EmfConsole parentConsole;
    
    private Case caseObj; 
    
    private boolean hasValues = false; 
    
    public CheckNoLocalDialog(EmfConsole parentConsole, SetCasePresenter presenter, Case caseObj){
        super("Jobs in the case may not run until the following items are corrected: ", parentConsole);
        super.setSize(new Dimension(400, 200));
        this.presenter = presenter; 
        this.parentConsole =parentConsole;
        this.caseObj = caseObj; 
    }
    
    
    public void display(String msg) {
        //String validationMsg = validateValues();
        int width = 50;
        int height = (msg.length() / 50)+3;
        getContentPane().add(createMsgScrollPane(msg, width, height));
        setLocation(ScreenUtils.getPointToCenter(parentConsole));
        pack();
        setModal(false);
        setVisible(true);
    }
    
    private ScrollableComponent createMsgScrollPane(String msg, int width, int height) {
        TextArea message = new TextArea("msgArea", msg, width, height);
        message.setEditable(false);
        ScrollableComponent descScrollableTextArea = new ScrollableComponent(message);
        return descScrollableTextArea;
    }
    
    public String validateValues() throws EmfException{
        String noLocalValues = "";
        CaseInput[] inputList = presenter.getCaseInput(caseObj.getId(), new Sector("All", "All"), true);
        noLocalValues += "The following non-local inputs do not have datasets specified: \n";
        for (CaseInput input :inputList){
            if ( !input.isLocal() && input.getDataset()==null){
                hasValues = true; 
                noLocalValues += getInputValues(input) +"\n";
            }
        }
        CaseParameter[] paraList = presenter.getCaseParameters(caseObj.getId(), new Sector("All", "All"), true);
        noLocalValues += "\nThe following non-local parameters do not have values: \n"; 
        for (CaseParameter par :paraList){
            if ( !par.isLocal() && par.getValue().trim().isEmpty()){
                noLocalValues += getParamValues(par) + "\n";
                hasValues = true; 
            }
        }
        return noLocalValues;
    }
    
    private String getInputValues(CaseInput input) throws EmfException{
        String Value = (input.getEnvtVars() == null ? "" : input.getEnvtVars().getName()) + "; " 
                     + (input.getSector() == null ? "All sectors" : input.getSector().getName())+ "; "
                     + presenter.getJobName(input.getCaseJobID()) + "; "
                     + input.getName();
        return Value; 
    }
    
    private String getParamValues(CaseParameter parameter) throws EmfException{
        String Value = (parameter.getEnvVar() == null ? "" : parameter.getEnvVar().getName()) + "; " 
                     + (parameter.getSector() == null ? "All sectors" : parameter.getSector().getName())+ "; " 
                     + presenter.getJobName(parameter.getJobId()) + "; "
                     + parameter.getName();
        return Value; 
    }

    public boolean getHasValues(){
        return hasValues;
    }
}

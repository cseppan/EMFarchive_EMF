package gov.epa.emissions.framework.client.casemanagement;


public class SetCaseObject{
    private Object object; 
    private String wizardType;
    public static final String WIZARD_1 = "PATH";
    public static final String WIZARD_2 = "INPUT";
    public static final String WIZARD_3 = "PARAMETER";
    public SetCaseObject(Object object, String wizardType){
        this.object=object;
        this.wizardType=wizardType;
    }
    
    public void setObject(Object object){
        this.object = object;
    }
    
    public void setIsInput(String wizardType){
        this.wizardType = wizardType;
    }
    
    public Object getObject(){
        return object;
    }
    
    public String getWizardType(){
        return wizardType;
    }
    
}
package gov.epa.emissions.framework.client.casemanagement;


public class SetCaseObject{
    private Object object; 
    private boolean isInput;
    public SetCaseObject(Object object, boolean isInput){
        this.object=object;
        this.isInput=isInput;
    }
    
    public void setObject(Object object){
        this.object = object;
    }
    
    public void setIsInput(boolean isInput){
        this.isInput = isInput;
    }
    
    public Object getObject(){
        return object;
    }
    
    public boolean isInput(){
        return isInput;
    }
    
}
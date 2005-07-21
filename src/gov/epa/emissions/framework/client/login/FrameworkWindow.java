// The FrameworkWindow interface

package gov.epa.emissions.framework.client.login;


public interface FrameworkWindow
{
    public void setupComponents();
    public void setModel(FrameworkObservable fro);
    public void loadDataFromModel();
    public void saveDataToModel();
}

// The FrameworkWindow interface

package gov.epa.emissions.framework.client.gui;


public interface FrameworkWindow
{
    public void setupComponents();
    public void setModel(FrameworkObservable fro);
    public void loadDataFromModel();
    public void saveDataToModel();
}

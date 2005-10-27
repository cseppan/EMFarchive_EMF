package gov.epa.emissions.framework.ui;

public interface Row {

    Object getValueAt(int column);

    Object source();

    void setValueAt(Object value, int column);

}
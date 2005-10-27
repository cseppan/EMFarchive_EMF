package gov.epa.emissions.framework.ui;

public interface Row {

    Object getValueAt(int column);

    Object source();

}
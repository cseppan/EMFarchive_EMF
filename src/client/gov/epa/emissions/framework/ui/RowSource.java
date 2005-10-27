package gov.epa.emissions.framework.ui;

public interface RowSource {

    Object[] values();

    void setValueAt(int column, Object val);

    Object source();

}

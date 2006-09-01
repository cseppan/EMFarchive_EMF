package gov.epa.emissions.framework.services.cost.controlStrategy;

import org.apache.commons.collections.primitives.ArrayDoubleList;
import org.apache.commons.collections.primitives.DoubleList;

import gov.epa.emissions.framework.services.EmfException;

public class CostYearTable {

    private int targetYear;

    private DoubleList gdpValues;

    private int startYear;

    public CostYearTable() {
        gdpValues = new ArrayDoubleList();
    }

    public CostYearTable(int targetYear) {
        this();
        this.targetYear = targetYear;
    }

    public int size() {
        return gdpValues.size();
    }

    public void addFirst(int startYear, double gdp) {
        this.startYear = startYear;
        gdpValues.add(gdp);
    }

    public void add(double gdp) {
        gdpValues.add(gdp);
    }

    public double factor(int year) throws EmfException {
        double yearGdp = gdpValue(year);
        double targetYearGdp = gdpValue(targetYear);
        return targetYearGdp / yearGdp;

    }

    private double gdpValue(int year) throws EmfException {
        int index = year - startYear;
        if (index > size() - 1) {
            throw new EmfException("The cost year conversion is available between 1929 to 2005");
        }
        return gdpValues.get(index);
    }

    public void setTargetYear(int targetYear) {
        this.targetYear = targetYear;
    }

    public int getTargetYear() {
        return targetYear;
    }

    public DoubleValue[] getGdpValues() {
        DoubleValue[] doubles = new DoubleValue[gdpValues.size()];
        for (int i = 0; i < doubles.length; i++) {
            doubles[i] = new DoubleValue();
            doubles[i].setValue(gdpValues.get(i));
        }
        return doubles;
    }

    public void setGdpValues(DoubleValue[] values) {
        this.gdpValues = new ArrayDoubleList();
        for (int i = 0; i < values.length; i++) {
            gdpValues.add(values[i].getValue());
        }
    }

    public int getStartYear() {
        return startYear;
    }

    public void setStartYear(int startYear) {
        this.startYear = startYear;
    }

}

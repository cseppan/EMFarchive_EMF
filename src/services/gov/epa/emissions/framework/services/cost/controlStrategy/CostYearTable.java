package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.framework.services.EmfException;

import org.apache.commons.collections.primitives.ArrayDoubleList;
import org.apache.commons.collections.primitives.DoubleList;

public class CostYearTable {

    private int targetYear;

    private DoubleList list;

    private int startYear;

    public CostYearTable(int targetYear) {
        this.targetYear = targetYear;
        list = new ArrayDoubleList();
    }

    public int size() {
        return list.size();
    }

    public void addFirst(int startYear, double gdp) {
        this.startYear = startYear;
        list.add(gdp);
    }

    public void add(double gdp) {
        list.add(gdp);
    }

    public double factor(int year) throws EmfException {
        double yearGdp = gdpValue(year);
        double targetYearGdp = gdpValue(targetYear);
        return targetYearGdp / yearGdp;

    }

    private double gdpValue(int year) throws EmfException {
        int index = year - startYear;
        if (index > size()-1) {
            throw new EmfException("Could not find cost year conversion factor for year " + year);
        }
        return list.get(index);
    }

}

package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

import gov.epa.emissions.framework.client.cost.controlstrategy.LocaleFilter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.controlStrategy.SccControlMeasuresMap;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

import java.util.ArrayList;
import java.util.List;

public class CalMaxEmsRedControlMeasure {

    private SccControlMeasuresMap map;

    private ControlStrategy controlStrategy;

    private LocaleFilter localeFilter;

    private CostYearTable costYearTable;

    public CalMaxEmsRedControlMeasure(SccControlMeasuresMap map, CostYearTable costYearTable,
            ControlStrategy controlStrategy) {
        this.map = map;
        this.costYearTable = costYearTable;
        this.controlStrategy = controlStrategy;
        this.localeFilter = new LocaleFilter();
    }

    public MaxControlEffControlMeasure getControlMeasure(String scc, String fips) throws EmfException {
        ControlMeasure[] controlMeasures = map.getControlMeasures(scc);
        // FIXME: if no control measure found for an scc log add warning msg
        CalMaxEmsRedEfficiencyRecord reduction = new CalMaxEmsRedEfficiencyRecord(costYearTable);
        for (int i = 0; i < controlMeasures.length; i++) {
            EfficiencyRecord record = findRecord(controlMeasures[i], fips, controlStrategy.getInventoryYear());
            if (record != null) {
                reduction.add(controlMeasures[i], record);
            }
        }

        MaxControlEffControlMeasure maxMeasure = reduction.maxEmsReductionMeasure();
        // FIXME: warn or error ?? if(maxEmsReductionMeasure==null)
        return maxMeasure;
    }

    private EfficiencyRecord findRecord(ControlMeasure measure, String fips, int inventoryYear) {
        EfficiencyRecord[] efficiencyRecords = pollutantFilter(measure);
        efficiencyRecords = localeFilter(efficiencyRecords, fips);
        efficiencyRecords = effectiveDateFilter(efficiencyRecords, inventoryYear);
        //TODO: add existing measure filter
        if (efficiencyRecords.length == 0)
            return null;

        return efficiencyRecords[0];
        // FIXME: throw an error ? probably no
        // FIXME: what if we get more than one record
    }

    private EfficiencyRecord[] effectiveDateFilter(EfficiencyRecord[] efficiencyRecords, int inventoryYear) {
        return new EffectiveDateFilter(efficiencyRecords, inventoryYear).filter();
    }

    private EfficiencyRecord[] pollutantFilter(ControlMeasure measure) {
        List records = new ArrayList();
        EfficiencyRecord[] efficiencyRecords = measure.getEfficiencyRecords();
        String targetPollutant = controlStrategy.getTargetPollutant();
        for (int i = 0; i < efficiencyRecords.length; i++) {
            if (efficiencyRecords[i].getPollutant().getName().equals(targetPollutant))
                records.add(efficiencyRecords[i]);
        }
        return (EfficiencyRecord[]) records.toArray(new EfficiencyRecord[0]);
    }

    private EfficiencyRecord[] localeFilter(EfficiencyRecord[] efficiencyRecords, String fips) {
        List records = new ArrayList();
        for (int i = 0; i < efficiencyRecords.length; i++) {
            String locale = efficiencyRecords[i].getLocale();
            if (localeFilter.acceptLocale(locale, fips))
                records.add(efficiencyRecords[i]);
        }
        return localeFilter.closestRecords(records);
    }

}

package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;

public class OnroadRecordGenerator implements RecordGenerator {
    private NonpointRecordGenerator delegate;

    public OnroadRecordGenerator(ControlStrategyResult result, DecimalFormat decFormat) {
        this.delegate = new NonpointRecordGenerator(result, decFormat);
    }

    public Record getRecord(ResultSet resultSet, BestMeasureEffRecord maxCM, double originalEmissions, boolean displayOriginalEmissions, boolean displayFinalEmissions) throws SQLException, EmfException {
        return delegate.getRecord(resultSet, maxCM, originalEmissions, displayOriginalEmissions, displayFinalEmissions);
    }

    public double reducedEmission() {
        return delegate.reducedEmission();
    }

    public void calculateEmissionReduction(ResultSet resultSet, BestMeasureEffRecord maxMeasure) throws SQLException {
        delegate.calculateEmissionReduction(resultSet, maxMeasure);
    }

    public List tokens(ResultSet resultSet, BestMeasureEffRecord maxCM, double originalEmissions, boolean displayOriginalEmissions, boolean displayFinalEmissions) throws SQLException, EmfException {
        return delegate.tokens(resultSet, maxCM, originalEmissions, displayOriginalEmissions, displayFinalEmissions);
    }
}
package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;

public class NonroadRecordGenerator implements RecordGenerator {
    private NonpointRecordGenerator delegate;

    public NonroadRecordGenerator(ControlStrategyResult result, DecimalFormat decFormat) {
        this.delegate = new NonpointRecordGenerator(result, decFormat);
    }

    public Record getRecord(ResultSet resultSet, MaxEmsRedControlMeasure maxCM) throws SQLException, EmfException {
        return delegate.getRecord(resultSet, maxCM);
    }

    public double reducedEmission() {
        return delegate.reducedEmission();
    }

    public void calculateEmissionReduction(ResultSet resultSet, MaxEmsRedControlMeasure maxMeasure) throws SQLException {
        delegate.calculateEmissionReduction(resultSet, maxMeasure);
    }

    public List tokens(ResultSet resultSet, MaxEmsRedControlMeasure maxCM) throws SQLException, EmfException {
        return delegate.tokens(resultSet, maxCM);
    }

}

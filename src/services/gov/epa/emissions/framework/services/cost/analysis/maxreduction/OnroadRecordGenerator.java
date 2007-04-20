package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class OnroadRecordGenerator implements RecordGenerator {
    private NonpointRecordGenerator delegate;

    public OnroadRecordGenerator(ControlStrategyResult result) {
        this.delegate = new NonpointRecordGenerator(result);
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

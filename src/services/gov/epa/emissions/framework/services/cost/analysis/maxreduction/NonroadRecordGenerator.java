package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

import java.sql.ResultSet;
import java.sql.SQLException;

public class NonroadRecordGenerator implements RecordGenerator {
    private NonpointRecordGenerator delegate;

    public NonroadRecordGenerator(ControlStrategyResult result) {
        this.delegate = new NonpointRecordGenerator(result);
    }

    public Record getRecord(ResultSet resultSet, MaxControlEffControlMeasure maxCM) throws SQLException, EmfException {
        return delegate.getRecord(resultSet, maxCM);
    }

    public double reducedEmission() {
        return delegate.reducedEmission();
    }

}

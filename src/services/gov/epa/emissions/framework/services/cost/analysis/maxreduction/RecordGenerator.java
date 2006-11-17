package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.framework.services.EmfException;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface RecordGenerator {

    Record getRecord(ResultSet resultSet, MaxControlEffControlMeasure maxCM) throws SQLException, EmfException;
    
    double reducedEmission();
}

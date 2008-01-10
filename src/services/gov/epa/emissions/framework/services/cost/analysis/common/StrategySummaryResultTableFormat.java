package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.LongFormatter;
import gov.epa.emissions.commons.io.NullFormatter;
import gov.epa.emissions.commons.io.RealFormatter;
import gov.epa.emissions.commons.io.StringFormatter;
import gov.epa.emissions.commons.io.TableFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StrategySummaryResultTableFormat implements TableFormat {
    private SqlDataTypes types;
    
    private Column[] cols;

    public StrategySummaryResultTableFormat(SqlDataTypes types) {
        this.types = types;
        this.cols = createCols();
    }

    public String identify() {
        return "Strategy Summary";
    }
    
    public String key() {
        return "Record_Id";
    }
    
    public Column[] cols() {
        return cols;
    }
    
    private Column[] createCols() {
        List cols = new ArrayList();

        cols.addAll(Arrays.asList(versionCols()));
        cols.addAll(Arrays.asList(baseCols()));

        return (Column[]) cols.toArray(new Column[0]);
    }
    
    private Column[] versionCols() {
        Column recordId = recordID(types);
        Column datasetId = new Column("Dataset_Id", types.longType(), new LongFormatter(), "NOT NULL");
        Column version = new Column("Version", types.intType(), new NullFormatter(), "NULL DEFAULT 0");
        Column deleteVersions = new Column("Delete_Versions", types.text(), new NullFormatter(), "DEFAULT ''::text");

        return new Column[] { recordId, datasetId,  version, deleteVersions};
    }

    private Column recordID(SqlDataTypes types) {
        String key = key();
        String keyType = types.autoIncrement() + ",Primary Key(" + key + ")";
        Column recordId = new Column(key, keyType, new NullFormatter());
        return recordId;
    }

    private Column[] baseCols() {
        Column fips = new Column("FIPS", types.stringType(6), new StringFormatter(6));
        Column scc = new Column("SCC", types.stringType(10), new StringFormatter(10));
        Column pollutant = new Column("Poll", types.stringType(20), new StringFormatter(20));
        Column controlTechnology = new Column("Control_Technology", types.stringType(128), new StringFormatter(128));
        Column annualCost = new Column("Annual_Cost", types.realType(), new RealFormatter());
        Column avgAnnualCostPerTon = new Column("Avg_Ann_Cost_per_Ton", types.realType(), new RealFormatter());
        Column emissionsReduction = new Column("Emis_Reduction", types.realType(), new RealFormatter());

        return new Column[] { fips, scc, 
                pollutant, controlTechnology, 
                annualCost, avgAnnualCostPerTon,
                emissionsReduction };
    }
}
package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.IntegerFormatter;
import gov.epa.emissions.commons.io.LongFormatter;
import gov.epa.emissions.commons.io.NullFormatter;
import gov.epa.emissions.commons.io.RealFormatter;
import gov.epa.emissions.commons.io.StringFormatter;
import gov.epa.emissions.commons.io.TableFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MaxEmsRedTableFormat implements TableFormat {
    private SqlDataTypes types;
    
    private Column[] cols;

    public MaxEmsRedTableFormat(SqlDataTypes types) {
        this.types = types;
        this.cols = createCols();
    }

    public String identify() {
        return "Maximum Emissions Reduction";
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

        return new Column[] { recordId, datasetId, version, deleteVersions};
    }

    private Column recordID(SqlDataTypes types) {
        String key = key();
        String keyType = types.autoIncrement() + ",Primary Key(" + key + ")";
        Column recordId = new Column(key, keyType, new NullFormatter());
        return recordId;
    }

    private Column[] baseCols() {
        Column disable = new Column("Disable", types.booleanType(), new StringFormatter(5));
        Column controlMeasureAbbr = new Column("CM_Abbrev", types.stringType(10), new StringFormatter(10), "DEFAULT ''");
        Column pollutant = new Column("Pollutant", types.stringType(20), new StringFormatter(20));
        Column scc = new Column("SCC", types.stringType(10), new StringFormatter(10));
        Column fips = new Column("FIPS", types.stringType(6), new StringFormatter(6));
        Column totalCost = new Column("Cost", types.realType(), new RealFormatter());
        Column costPerTon = new Column("Cost_per_Ton", types.realType(), new RealFormatter());
        Column controlEfficiency = new Column("Control_Eff", types.realType(), new RealFormatter());
        Column controlledEmission = new Column("CNTRLD_Emissions", types.realType(), new RealFormatter());
        Column totalReduction = new Column("Emis_Reduction", types.realType(), new RealFormatter());
        Column originalEmissions = new Column("Original_Emis", types.realType(), new RealFormatter());
        
        Column sourceId = new Column("Source_Id", types.intType(), new IntegerFormatter(), "NOT NULL");
        Column inputDatasetId = new Column("Input_DS_Id", types.intType(), new IntegerFormatter(), "NOT NULL");
        Column controlStrategyId = new Column("CS_Id", types.intType(), new IntegerFormatter());
        Column controlMeasureId = new Column("CM_Id", types.intType(), new IntegerFormatter());
        Column comment = new Column("Comment", types.stringType(50), new StringFormatter(50));
        
        return new Column[] { disable, controlMeasureAbbr, pollutant, scc, fips, totalCost, costPerTon, controlEfficiency, 
                controlledEmission, totalReduction, originalEmissions, sourceId, inputDatasetId, controlStrategyId,
                controlMeasureId, comment };
    }

}

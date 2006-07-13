package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.IntegerFormatter;
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

        cols.addAll(Arrays.asList(referenceCols()));
        cols.addAll(Arrays.asList(baseCols()));

        return (Column[]) cols.toArray(new Column[0]);
    }
    
    private Column[] referenceCols() {
        Column recordId = recordID(types);
        Column sourceId = new Column("Source_Id", types.intType(), new IntegerFormatter(), "NOT NULL");
        Column datasetId = new Column("Dataset_Id", types.intType(), new IntegerFormatter(), "NOT NULL");
        Column controlStrategyId = new Column("Control_Strategy_Id", types.intType(), new IntegerFormatter());
        Column controlMeasureId = new Column("Control_Measure_Id", types.intType(), new IntegerFormatter());
        Column controlMeasureAbbr = new Column("Control_Measure_Abbr", types.text(), new NullFormatter(), "DEFAULT ''::text");
        
        return new Column[] { recordId, sourceId, datasetId, controlStrategyId, controlMeasureId, controlMeasureAbbr };
    }

    private Column recordID(SqlDataTypes types) {
        String key = key();
        String keyType = types.autoIncrement() + ",Primary Key(" + key + ")";
        Column recordId = new Column(key, keyType, new NullFormatter());
        return recordId;
    }

    private Column[] baseCols() {
        Column pollutant = new Column("pollutant", types.stringType(20), new StringFormatter(20));
        Column scc = new Column("scc", types.stringType(10), new StringFormatter(10));
        Column fips = new Column("FIPS", types.stringType(6), new StringFormatter(6));
        Column totalCost = new Column("total_cost", types.realType(), new RealFormatter());
        Column costPerTon = new Column("cost_per_ton", types.realType(), new RealFormatter());
        Column controlEfficiency = new Column("control_efficiency", types.realType(), new RealFormatter());
        Column controlledEmission = new Column("cntrld_emission", types.realType(), new RealFormatter());
        Column totalReduction = new Column("total_reduction", types.realType(), new RealFormatter());
        Column originalEmissions = new Column("original_emissions", types.realType(), new RealFormatter());
        Column disable = new Column("disable", types.booleanType(), new StringFormatter(5));
        Column comment = new Column("comment", types.stringType(50), new StringFormatter(50));
        
        return new Column[] { pollutant, scc, fips, totalCost, costPerTon, controlEfficiency, 
                controlledEmission, totalReduction, originalEmissions, disable, comment };
    }

}

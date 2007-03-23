package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.IntegerFormatter;
import gov.epa.emissions.commons.io.RealFormatter;
import gov.epa.emissions.commons.io.StringFormatter;
import gov.epa.emissions.commons.io.TableFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EfficiencyRecordTableFormat implements TableFormat {
    private SqlDataTypes types;
    
    private Column[] cols;

    public EfficiencyRecordTableFormat(SqlDataTypes types) {
        this.types = types;
        this.cols = createCols();
    }

    public String identify() {
        return "Efficiency Record";
    }
    
    public String key() {
        return "id";
    }
    
    public Column[] cols() {
        return cols;
    }
    
    private Column[] createCols() {
        List cols = new ArrayList();

        cols.addAll(Arrays.asList(baseCols()));

        return (Column[]) cols.toArray(new Column[0]);
    }

//    private Column recordID(SqlDataTypes types) {
//        String key = key();
//        String keyType = types.autoIncrement() + ",Primary Key(" + key + ")";
//        Column recordId = new Column(key, keyType, new NullFormatter());
//        return recordId;
//    }

//  id serial NOT NULL,
//  control_measures_id int4 NOT NULL,
//  list_index int4,
//  record_id int4,
//  pollutant_id int4 NOT NULL,
//  existing_measure_abbr varchar(10),
//  existing_dev_code int4,
//  locale varchar(10),
//  efficiency float4,
//  percent_reduction float4,
//  cost_year int4,
//  cost_per_ton float4,
//  rule_effectiveness float4,
//  rule_penetration float4,
//  equation_type varchar(128),
//  cap_rec_factor float4,
//  discount_rate float4,
//  detail varchar(128),
//  effective_date timestamp,
//  last_modified_by varchar(255) NOT NULL DEFAULT ''::character varying,
//  last_modified_time timestamp NOT NULL DEFAULT now(),
//  ref_yr_cost_per_ton float4 DEFAULT 0,

    private Column[] baseCols() {
        Column controlMeasureId = new Column("control_measures_id", types.intType(), new IntegerFormatter(), "NOT NULL");
        Column list_index = new Column("list_index", types.intType(), new IntegerFormatter());
        Column record_id = new Column("record_id", types.intType(), new IntegerFormatter());
        Column pollutant_id = new Column("pollutant_id", types.intType(), new IntegerFormatter(), "NOT NULL");
        Column existing_measure_abbr = new Column("existing_measure_abbr", types.stringType(10), new StringFormatter(10), "DEFAULT ''");
        Column existing_dev_code = new Column("existing_dev_code", types.intType(), new IntegerFormatter(), "DEFAULT 0");
        Column locale = new Column("locale", types.stringType(10), new StringFormatter(10));
        Column efficiency = new Column("efficiency", types.realType(), new RealFormatter());
        Column percent_reduction = new Column("percent_reduction", types.realType(), new RealFormatter());
        Column cost_year = new Column("cost_year", types.intType(), new IntegerFormatter());
        Column cost_per_ton = new Column("cost_per_ton", types.realType(), new RealFormatter(), "DEFAULT NULL");
        Column rule_effectiveness = new Column("rule_effectiveness", types.realType(), new RealFormatter());
        Column rule_penetration = new Column("rule_penetration", types.realType(), new RealFormatter());
        Column equation_type = new Column("equation_type", types.stringType(128), new StringFormatter(128));
        Column cap_rec_factor = new Column("cap_rec_factor", types.realType(), new RealFormatter());
        Column discount_rate = new Column("discount_rate", types.realType(), new RealFormatter());
        Column detail = new Column("detail", types.stringType(128), new StringFormatter(128), "DEFAULT ''");
        Column effective_date = new Column("effective_date", types.timestamp());
        Column last_modified_by = new Column("last_modified_by", types.stringType(255), new StringFormatter(255), "DEFAULT ''");
        Column last_modified_time = new Column("last_modified_time", types.timestamp());
        Column ref_yr_cost_per_ton = new Column("ref_yr_cost_per_ton", types.realType(), new RealFormatter(), "DEFAULT NULL");

        
        return new Column[] { controlMeasureId, list_index, record_id, pollutant_id, existing_measure_abbr, existing_dev_code, locale, efficiency, percent_reduction, 
                cost_year, cost_per_ton, rule_effectiveness, rule_penetration, equation_type, cap_rec_factor, 
                discount_rate, detail, effective_date, last_modified_by, 
                last_modified_time, ref_yr_cost_per_ton};
    }

}

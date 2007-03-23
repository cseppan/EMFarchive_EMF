package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

import java.util.ArrayList;
import java.util.List;

public class EfficiencyRecordGenerator {

    public EfficiencyRecordGenerator() {
        //
    }

    public Record getRecord(EfficiencyRecord efficiencyRecord)  {
        Record record = new Record();
        record.add(tokens(efficiencyRecord));

        return record;
    }

    public List tokens(EfficiencyRecord efficiencyRecord) {
        List tokens = new ArrayList();
        
//      id serial NOT NULL,
//      control_measures_id int4 NOT NULL,
//      list_index int4,
//      record_id int4,
//      pollutant_id int4 NOT NULL,
//      existing_measure_abbr varchar(10),
//      existing_dev_code int4,
//      locale varchar(10),
//      efficiency float4,
//      percent_reduction float4,
//      cost_year int4,
//      cost_per_ton float4,
//      rule_effectiveness float4,
//      rule_penetration float4,
//      equation_type varchar(128),
//      cap_rec_factor float4,
//      discount_rate float4,
//      detail varchar(128),
//      effective_date timestamp,
//      last_modified_by varchar(255) NOT NULL DEFAULT ''::character varying,
//      last_modified_time timestamp NOT NULL DEFAULT now(),
//      ref_yr_cost_per_ton float4 DEFAULT 0,

        tokens.add(""); // record id
        tokens.add("" + efficiencyRecord.getControlMeasureId());
        tokens.add("" + 0);
        tokens.add("" + 0);
        tokens.add("" + efficiencyRecord.getPollutant().getId());
        tokens.add("" + efficiencyRecord.getExistingMeasureAbbr());
        tokens.add("" + efficiencyRecord.getExistingDevCode());
        tokens.add("" + efficiencyRecord.getLocale());
        tokens.add("" + efficiencyRecord.getEfficiency());
        tokens.add("" + 0);
        tokens.add("" + efficiencyRecord.getCostYear());
        tokens.add("" + (efficiencyRecord.getCostPerTon() != Float.MIN_VALUE ? efficiencyRecord.getCostPerTon() : ""));
        tokens.add("" + efficiencyRecord.getRuleEffectiveness());
        tokens.add("" + efficiencyRecord.getRulePenetration());
        tokens.add("" + efficiencyRecord.getEquationType());
        tokens.add("" + efficiencyRecord.getCapRecFactor());
        tokens.add("" + efficiencyRecord.getDiscountRate());
        tokens.add("" + efficiencyRecord.getDetail());
        tokens.add("" + (efficiencyRecord.getEffectiveDate() == null ? "" : efficiencyRecord.getEffectiveDate()));
        tokens.add("" + efficiencyRecord.getLastModifiedBy());
        tokens.add("" + efficiencyRecord.getLastModifiedTime());
        tokens.add("" + (efficiencyRecord.getRefYrCostPerTon() != Float.MIN_VALUE ? efficiencyRecord.getRefYrCostPerTon() : ""));


        return tokens;
    }
}

package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;

import java.util.Map;

public class CMSCCRecordReader {

    private CMSCCsFileFormat fileFormat;

    public CMSCCRecordReader(CMSCCsFileFormat fileFormat) {
        this.fileFormat = fileFormat;
    }

    public void parse(Map controlMeasures, Record record, int lineNo) throws CMImporterException {
        String[] tokens = modify(record);
        ControlMeasure cm = (ControlMeasure) controlMeasures.get(tokens[0]);
        if (cm == null)
            throw new CMImporterException("The abbreviation '" + tokens[0]
                    + "' is not in the control measure summary file. line no: " + lineNo);

        Scc scc = new Scc();
        scc.setCode(tokens[0]);
        scc.setStatus(tokens[1]);
        
        cm.addScc(scc);

    }

    private String[] modify(Record record) throws CMImporterException {
        int sizeDiff = fileFormat.cols().length - record.getTokens().length;
        if (sizeDiff == 0)
            return record.getTokens();

        if (sizeDiff > 0) {
            for (int i = 0; i < sizeDiff; i++) {
                record.add("");
            }
            return record.getTokens();
        }

        throw new CMImporterException("This record has more tokens");
    }

}

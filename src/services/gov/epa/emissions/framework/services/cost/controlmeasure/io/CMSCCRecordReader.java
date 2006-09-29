package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.Map;

public class CMSCCRecordReader {

    private CMSCCsFileFormat fileFormat;

    private CMAddImportStatus status;

    public CMSCCRecordReader(CMSCCsFileFormat fileFormat, User user, HibernateSessionFactory sessionFactory) {
        this.fileFormat = fileFormat;
        this.status = new CMAddImportStatus(user, sessionFactory);
    }

    public void parse(Map controlMeasures, Record record, int lineNo) {
        StringBuffer sb = new StringBuffer();
        String[] tokens = modify(record, sb, lineNo);
        ControlMeasure cm = controlMeasure(tokens[0], controlMeasures, sb, lineNo);
        if (tokens == null || cm == null)
            return;

        Scc scc = new Scc();
        scc.setCode(tokens[1]);
        scc.setStatus(tokens[2]);

        cm.addScc(scc);

    }

    private ControlMeasure controlMeasure(String token, Map controlMeasures, StringBuffer sb, int lineNo) {
        ControlMeasure cm = (ControlMeasure) controlMeasures.get(token);
        if (cm == null) {
            sb.append(format("abbreviation '" + token + "' is not in the control measure summary file"));
            status.addStatus(lineNo, sb);
        }
        return cm;
    }

    private String[] modify(Record record, StringBuffer sb, int lineNo) {
        int sizeDiff = fileFormat.cols().length - record.getTokens().length;
        if (sizeDiff == 0)
            return record.getTokens();

        if (sizeDiff > 0) {
            for (int i = 0; i < sizeDiff; i++) {
                record.add("");
            }
            return record.getTokens();
        }

        sb.append(format("The new record has extra tokens"));
        status.addStatus(lineNo, sb);
        return null;
    }

    private String format(String text) {
        return status.format(text);
    }

}

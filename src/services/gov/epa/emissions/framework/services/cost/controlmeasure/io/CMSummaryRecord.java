package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

public class CMSummaryRecord {

    private CMSummaryFileFormat fileFormat;

    private Pollutants pollutants;

    public CMSummaryRecord(CMSummaryFileFormat fileFormat, HibernateSessionFactory sessionFactory) {
        this.fileFormat = fileFormat;
        pollutants = new Pollutants(sessionFactory);
    }

    // FIXME: duplicate name,abbrev handling?????
    public ControlMeasure parse(Record record, int lineNo) throws ImporterException {
        String[] tokens = modify(record);
        return measure(tokens, lineNo);
    }

    // FIXME: throw an different kind of exceptions
    private ControlMeasure measure(String[] tokens, int lineNo) throws ImporterException {
        ControlMeasure cm = new ControlMeasure();
        name(cm, tokens[0], lineNo);
        abbrev(cm, tokens[1], lineNo);
        majorPollutant(cm, tokens[3], lineNo);
        return cm;
    }

    private void name(ControlMeasure cm, String token, int lineNo) throws ImporterException {
        if (token.length() == 0) {
            throw new ImporterException("The name should not be empty. line no: " + lineNo);
        }
        cm.setName(token);
    }

    private void abbrev(ControlMeasure cm, String token, int lineNo) throws ImporterException {
        if (token.length() == 0) {
            throw new ImporterException("The Abbreviation should not be empty. line no: " + lineNo);
        }
        cm.setAbbreviation(token);

    }

    private void majorPollutant(ControlMeasure cm, String name, int lineNo) throws ImporterException {
        try {
            Pollutant pollutant = pollutants.getPollutant(name);
            cm.setMajorPollutant(pollutant);
        } catch (ImporterException e) {
            throw new ImporterException(e.getMessage()+". line no: " + lineNo);
        }

    }

    private String[] modify(Record record) throws ImporterException {
        int sizeDiff = fileFormat.cols().length - record.getTokens().length;
        if (sizeDiff == 0)
            return record.getTokens();

        if (sizeDiff > 0) {
            for (int i = 0; i < sizeDiff; i++) {
                record.add("");
            }
            return record.getTokens();
        }

        throw new ImporterException("This record has more tokens");
    }

}

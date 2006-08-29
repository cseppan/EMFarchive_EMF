package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.data.SourceGroup;
import gov.epa.emissions.framework.client.data.EmfDateFormat;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.data.ControlTechnology;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.text.ParseException;
import java.util.Date;

public class CMSummaryRecordReader {

    private CMSummaryFileFormat fileFormat;

    private Pollutants pollutants;

    private ControlTechnologies controlTechnologies;

    private SourceGroups sourceGroups;

    private Sectors sectors;

    public CMSummaryRecordReader(CMSummaryFileFormat fileFormat, HibernateSessionFactory sessionFactory) {
        this.fileFormat = fileFormat;
        pollutants = new Pollutants(sessionFactory);
        controlTechnologies = new ControlTechnologies(sessionFactory);
        sourceGroups = new SourceGroups(sessionFactory);
        sectors = new Sectors(sessionFactory);
    }

    // FIXME: duplicate name,abbrev handling?????
    public ControlMeasure parse(Record record, int lineNo) throws CMImporterException {
        String[] tokens = modify(record);
        return measure(tokens, lineNo);
    }

    // FIXME: throw an different kind of exceptions
    private ControlMeasure measure(String[] tokens, int lineNo) throws CMImporterException {
        ControlMeasure cm = new ControlMeasure();
        name(cm, tokens[0], lineNo);
        abbrev(cm, tokens[1], lineNo);
        majorPollutant(cm, tokens[2], lineNo);
        controlTechnology(cm, tokens[3], lineNo);
        sourceGroup(cm, tokens[4], lineNo);
        sector(cm, tokens[5], lineNo);
        cmClass(cm, tokens[6], lineNo);
        equipLife(cm, tokens[7], lineNo);
        deviceCode(cm, tokens[8], lineNo);
        dateReviewed(cm, tokens[9], lineNo);
        datasource(cm, tokens[10]);
        description(cm, tokens[11]);
        return cm;
    }

    private void name(ControlMeasure cm, String token, int lineNo) throws CMImporterException {
        if (token.length() == 0) {
            throw new CMImporterException("The name should not be empty. line no: " + lineNo);
        }
        cm.setName(token);
    }

    private void abbrev(ControlMeasure cm, String token, int lineNo) throws CMImporterException {
        if (token.length() == 0) {
            throw new CMImporterException("The Abbreviation should not be empty. line no: " + lineNo);
        }
        cm.setAbbreviation(token);

    }

    private void majorPollutant(ControlMeasure cm, String name, int lineNo) throws CMImporterException {
        if (name.length() == 0)
            throw new CMImporterException("The Major Pollutant should not be empty. line no:" + lineNo);

        try {
            Pollutant pollutant = pollutants.getPollutant(name);
            cm.setMajorPollutant(pollutant);
        } catch (CMImporterException e) {
            throw new CMImporterException(e.getMessage() + ". line no: " + lineNo);
        }
    }

    private void controlTechnology(ControlMeasure cm, String name, int lineNo) throws CMImporterException {
        try {
            ControlTechnology ct = controlTechnologies.getControlTechnology(name);
            cm.setControlTechnology(ct);
        } catch (CMImporterException e) {
            throw new CMImporterException(e.getMessage() + ". line no: " + lineNo);
        }
    }

    private void sourceGroup(ControlMeasure cm, String name, int lineNo) throws CMImporterException {
        try {
            SourceGroup sourceGroup = sourceGroups.getSourceGroup(name);
            cm.setSourceGroup(sourceGroup);
        } catch (CMImporterException e) {
            throw new CMImporterException(e.getMessage() + ". line no: " + lineNo);
        }

    }

    private void sector(ControlMeasure cm, String name, int lineNo) throws CMImporterException {
        try {
            Sector sector = sectors.getSector(name);
            cm.setSectors(new Sector[] { sector });
        } catch (CMImporterException e) {
            throw new CMImporterException(e.getMessage() + ". line no: " + lineNo);
        }
    }

    private void equipLife(ControlMeasure cm, String equipLife, int lineNo) throws CMImporterException {
        try {
            float noOfYears = 0;
            if (equipLife.length() != 0)
                noOfYears = Float.parseFloat(equipLife);
            
            cm.setEquipmentLife(noOfYears);
        } catch (NumberFormatException e) {
            throw new CMImporterException("Could not convert equip life into a floating point value. line no: "
                    + lineNo);
        }

    }

    private void cmClass(ControlMeasure cm, String clazz, int lineNo) {
        // TODO: do we throw an exception when there is no class specified
        cm.setCmClass(clazz);
    }

    private void deviceCode(ControlMeasure cm, String code, int lineNo) throws CMImporterException {
        try {
            int deviceCode = 0;
            if (code.length() != 0)
                deviceCode = Integer.parseInt(code);

            cm.setDeviceCode(deviceCode);
        } catch (NumberFormatException e) {
            throw new CMImporterException("Could not convert device code into a int value. line no: " + lineNo);
        }
    }

    private void dateReviewed(ControlMeasure cm, String date, int lineNo) throws CMImporterException {
        try {
            Date dateReviewed = EmfDateFormat.parse_YYYY(date);
            cm.setDateReviewed(dateReviewed);
        } catch (ParseException e) {
            throw new CMImporterException("Could not convert review date into a date value. line no: " + lineNo);
        }
    }

    private void datasource(ControlMeasure cm, String dataSouce) {
        cm.setDataSouce(dataSouce);
    }

    private void description(ControlMeasure cm, String description) {
        cm.setDescription(description);
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

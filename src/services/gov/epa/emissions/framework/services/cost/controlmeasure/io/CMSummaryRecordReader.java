package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.data.SourceGroup;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.data.ControlTechnology;
import gov.epa.emissions.framework.services.data.EmfDateFormat;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CMSummaryRecordReader {

    private CMSummaryFileFormat fileFormat;

    private Pollutants pollutants;

    private ControlTechnologies controlTechnologies;

    private SourceGroups sourceGroups;

    private Sectors sectors;

    private List namesList;

    private List abbrevList;

    private CMAddImportStatus cmAddImportStatus;

    public CMSummaryRecordReader(CMSummaryFileFormat fileFormat, User user, HibernateSessionFactory sessionFactory) {
        this.fileFormat = fileFormat;
        this.cmAddImportStatus = new CMAddImportStatus(user, sessionFactory);
        pollutants = new Pollutants(sessionFactory);
        controlTechnologies = new ControlTechnologies(sessionFactory);
        sourceGroups = new SourceGroups(sessionFactory);
        sectors = new Sectors(sessionFactory);
        this.namesList = new ArrayList();
        this.abbrevList = new ArrayList();
    }

    public ControlMeasure parse(Record record, int lineNo) {
        String[] tokens = null;
        try {
            tokens = modify(record);
            return measure(tokens, lineNo);
        } catch (CMImporterException e) {
            cmAddImportStatus.addStatus(lineNo, new StringBuffer(format(e.getMessage())));
            return null;
        }
    }

    private ControlMeasure measure(String[] tokens, int lineNo) {
        StringBuffer sb = new StringBuffer();

        ControlMeasure cm = null;
        if (constraintCheck(tokens, sb)) {
            cm = new ControlMeasure();
            name(cm, tokens[0]);
            abbrev(cm, tokens[1]);
            majorPollutant(cm, tokens[2], sb);
            controlTechnology(cm, tokens[3], sb);
            sourceGroup(cm, tokens[4], sb);
            sector(cm, tokens[5], sb);
            cmClass(cm, tokens[6], sb);
            equipLife(cm, tokens[7], sb);
            deviceCode(cm, tokens[8], sb);
            dateReviewed(cm, tokens[9], sb);
            datasource(cm, tokens[10]);
            description(cm, tokens[11]);
        }
        cmAddImportStatus.addStatus(lineNo, sb);
        return cm;
    }

    private boolean constraintCheck(String[] tokens, StringBuffer sb) {
        if (tokens[0].length() == 0) {
            sb.append(format("name should not be empty"));
            return false;
        }

        if (tokens[1].length() == 0) {
            sb.append(format("abbreviation should not be empty"));
            return false;
        }
        if (namesList.contains(tokens[0])) {
            sb.append(format("name alerady in the file-" + tokens[0]));
            return false;
        }
        namesList.add(tokens[0]);

        if (abbrevList.contains(tokens[1])) {
            sb.append(format("abbreviation alerady in the file-" + tokens[1]));
            return false;
        }
        namesList.add(tokens[1]);
        return true;

    }

    private void name(ControlMeasure cm, String token) {
        cm.setName(token);
    }

    private void abbrev(ControlMeasure cm, String token) {
        cm.setAbbreviation(token);
    }

    private void majorPollutant(ControlMeasure cm, String name, StringBuffer sb) {
        if (name.length() == 0) {
            sb.append(format("major pollutant should not be empty"));
            return;
        }

        try {
            Pollutant pollutant = pollutants.getPollutant(name);
            cm.setMajorPollutant(pollutant);
        } catch (CMImporterException e) {
            sb.append(format(e.getMessage()));
        }
    }

    private void controlTechnology(ControlMeasure cm, String name, StringBuffer sb) {
        if (name.length() == 0)
            return;

        try {
            ControlTechnology ct = controlTechnologies.getControlTechnology(name);
            cm.setControlTechnology(ct);
        } catch (CMImporterException e) {
            sb.append(format(e.getMessage()));
        }
    }

    private void sourceGroup(ControlMeasure cm, String name, StringBuffer sb) {
        if (name.length() == 0)
            return;

        try {
            SourceGroup sourceGroup = sourceGroups.getSourceGroup(name);
            cm.setSourceGroup(sourceGroup);
        } catch (CMImporterException e) {
            sb.append(format(e.getMessage()));
        }
    }

    private void sector(ControlMeasure cm, String name, StringBuffer sb) {
        if (name.length() == 0)
            return;

        try {
            Sector sector = sectors.getSector(name);
            cm.setSectors(new Sector[] { sector });
        } catch (CMImporterException e) {
            sb.append(format(e.getMessage()));
        }
    }

    private void equipLife(ControlMeasure cm, String equipLife, StringBuffer sb) {
        try {
            float noOfYears = 0;
            if (equipLife.length() != 0)
                noOfYears = Float.parseFloat(equipLife);

            cm.setEquipmentLife(noOfYears);
        } catch (NumberFormatException e) {
            sb.append(format("equip life is a floating point value-" + equipLife));
        }
    }

    private void cmClass(ControlMeasure cm, String clazz, StringBuffer sb) {
        if (clazz.length() == 0) {
            sb.append(format("class shoule not be empty"));
            return;
        }
        cm.setCmClass(clazz);
    }

    private void deviceCode(ControlMeasure cm, String code, StringBuffer sb) {
        try {
            int deviceCode = 0;
            if (code.length() != 0)
                deviceCode = Integer.parseInt(code);

            cm.setDeviceCode(deviceCode);
        } catch (NumberFormatException e) {
            sb.append(format("device code is an int value-" + code));
        }
    }

    private void dateReviewed(ControlMeasure cm, String date, StringBuffer sb) {
        try {
            Date dateReviewed = EmfDateFormat.parse_YYYY(date);
            cm.setDateReviewed(dateReviewed);
        } catch (ParseException e) {
            sb.append(format("expected date format YYYY-" + date));
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

    private String format(String text) {
        return cmAddImportStatus.format(text);
    }

}

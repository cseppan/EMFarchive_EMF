package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.util.Date;
import java.util.Map;

public class CMSummaryImporter {

    private File file;

    private CMSummaryRecordReader cmSummaryRecord;

    private HibernateSessionFactory sessionFactory;

    private User user;

    public CMSummaryImporter(File file, CMSummaryFileFormat fileFormat, User user,
            HibernateSessionFactory sessionFactory) {
        this.file = file;
        this.user = user;
        this.sessionFactory = sessionFactory;
        cmSummaryRecord = new CMSummaryRecordReader(fileFormat, user, sessionFactory);
    }

    public void run(Map controlMeasures) throws ImporterException {
        addStatus("Started reading summary file");
        CMCSVFileReader reader = new CMCSVFileReader(file);
        for (Record record = reader.read(); !record.isEnd(); record = reader.read()) {
            ControlMeasure cm = cmSummaryRecord.parse(record, reader.lineNumber());
            if (cm != null)
                controlMeasures.put(cm.getAbbreviation(), cm);
        }
        addStatus("Finished reading summary file");
    }

    private void addStatus(String message) {
        setStatus(message);
    }

    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("CMImportDetailMsg");
        endStatus.setMessage(message + "\n");
        endStatus.setTimestamp(new Date());

        new StatusDAO(sessionFactory).add(endStatus);
    }

}

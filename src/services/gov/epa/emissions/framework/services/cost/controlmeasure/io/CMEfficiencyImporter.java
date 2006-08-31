package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.util.Date;
import java.util.Map;

public class CMEfficiencyImporter {

    private File file;

    private CMEfficiencyRecordReader cmEfficiencyReader;

    private User user;

    private HibernateSessionFactory sessionFactory;

    public CMEfficiencyImporter(File file, CMEfficiencyFileFormat fileFormat, User user,
            HibernateSessionFactory sessionFactory) {
        this.file = file;
        this.user = user;
        this.sessionFactory = sessionFactory;
        this.cmEfficiencyReader = new CMEfficiencyRecordReader(fileFormat, user, sessionFactory);
    }

    public void run(Map controlMeasures) throws ImporterException {
        addStatus("Finished reading Efficiency file");
        CMCSVFileReader reader = new CMCSVFileReader(file);
        for (Record record = reader.read(); !record.isEnd(); record = reader.read()) {
            cmEfficiencyReader.parse(controlMeasures, record, reader.lineNumber());
        }
        addStatus("Finished reading Efficiency file");
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

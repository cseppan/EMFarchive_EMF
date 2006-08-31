package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.util.Map;

public class CMEfficiencyImporter {

    private File file;
    
    private CMEfficiencyRecordReader cmEfficiencyReader;

    public CMEfficiencyImporter(File file, CMEfficiencyFileFormat fileFormat, User user, HibernateSessionFactory sessionFactory) {
        this.file = file;
        this.cmEfficiencyReader = new CMEfficiencyRecordReader(fileFormat,user, sessionFactory);
    }

    public void run(Map controlMeasures) throws ImporterException {
        CMCSVFileReader reader = new CMCSVFileReader(file);
        try {
            for (Record record = reader.read(); !record.isEnd(); record = reader.read()) {
                cmEfficiencyReader.parse(controlMeasures,record, reader.lineNumber());
            }
        } catch (CMImporterException e) {
            throw new ImporterException(e.getMessage());// FIXME:quit import or not
        }

    }

}

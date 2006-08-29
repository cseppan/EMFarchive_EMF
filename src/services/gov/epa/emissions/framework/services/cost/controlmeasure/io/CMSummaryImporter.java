package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.util.Map;

public class CMSummaryImporter {

    private File file;

    private CMSummaryRecordReader cmSummaryRecord;

    public CMSummaryImporter(File file, CMSummaryFileFormat fileFormat, HibernateSessionFactory sessionFactory) {
        this.file = file;
        cmSummaryRecord = new CMSummaryRecordReader(fileFormat, sessionFactory);
    }

    public void run(Map controlMeasures) throws ImporterException {
        CMCSVFileReader reader = new CMCSVFileReader(file);
        try {
            for (Record record = reader.read(); !record.isEnd(); record = reader.read()) {
                ControlMeasure cm = cmSummaryRecord.parse(record, reader.lineNumber());
                controlMeasures.put(cm.getAbbreviation(), cm);
            }
        } catch (CMImporterException e) {
            throw new ImporterException(e.getMessage());// FIXME:quit import or not
        }

    }

}

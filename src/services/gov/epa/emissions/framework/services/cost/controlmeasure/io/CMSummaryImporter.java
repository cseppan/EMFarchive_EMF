package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CMSummaryImporter implements Importer {

    private File file;
    
    private List controlMeasures;

    private CMSummaryRecord cmSummaryRecord;
    
    public CMSummaryImporter(File file, CMSummaryFileFormat fileFormat, HibernateSessionFactory sessionFactory) {
        this.file = file;
        controlMeasures = new ArrayList();
        cmSummaryRecord = new CMSummaryRecord(fileFormat,sessionFactory);
    }

    
    public void run() throws ImporterException {
        CMCSVFileReader reader = new CMCSVFileReader(file);
        try {
            for (Record record = reader.read(); !record.isEnd(); record = reader.read()) {
                ControlMeasure cm = cmSummaryRecord.parse(record,reader.lineNumber());
                controlMeasures.add(cm);
            }
        } catch (CMImporterException e) {
            throw new ImporterException(e.getMessage());//FIXME:quit import or not
        }

    }

}

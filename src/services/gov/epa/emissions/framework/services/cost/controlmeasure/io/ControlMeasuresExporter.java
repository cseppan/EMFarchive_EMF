package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.data.SourceGroup;
import gov.epa.emissions.commons.io.CustomCharSetOutputStreamWriter;
import gov.epa.emissions.commons.io.Exporter;
import gov.epa.emissions.commons.io.ExporterException;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureDAO;
import gov.epa.emissions.framework.services.cost.ControlMeasureEquation;
import gov.epa.emissions.framework.services.cost.EquationType;
import gov.epa.emissions.framework.services.cost.EquationTypeVariable;
import gov.epa.emissions.framework.services.cost.data.ControlTechnology;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;

public class ControlMeasuresExporter implements Exporter {

    private ControlMeasure[] controlMeasures;

    private File folder;

    private String prefix;
    
    private User user;

    private HibernateSessionFactory factory;
    
    private String delimiter;
    
    private String[] cmAbbrevSccs;

    private long exportedLinesCount = 0;
    
//    private EquationTypeMap equationTypeMap;

    public ControlMeasuresExporter(File folder, String prefix, ControlMeasure[] controlMeasures, String[] sccs,
            User user, HibernateSessionFactory factory) {
        this.controlMeasures = controlMeasures;
        this.folder = folder;
        this.prefix = prefix;
        this.user = user;
        this.factory = factory;
        this.cmAbbrevSccs = sccs;
        this.delimiter = ",";
 //       this.equationTypeMap = new EquationTypeMap(getEquationTypes());
    }

    public void run() throws ExporterException {
        try {
            addStatus("Start exporting control measures to folder: " + folder.getAbsolutePath() +  ".");
            writeExportFiles();
            addStatus("Export control measures finished.");
            exportedLinesCount = controlMeasures.length;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExporterException("Export control measures failed. Reason: " + e.getMessage());
        }
    }

    private PrintWriter openExportFile(String fileName) throws IOException {
        File file = new File(folder, prefix + fileName);
        
//        return new PrintWriter(new BufferedWriter(new FileWriter(file)));
        return new PrintWriter(new CustomCharSetOutputStreamWriter(new FileOutputStream(file)));
    }
    
    private void writeExportFiles() throws IOException {
        writeSummaryFile();
        writeEfficiencyFile();
        writeSccFile();
        writeEquationFile();
    }

    private void writeEquationFile() throws IOException {
        
        PrintWriter equationWriter = openExportFile("_equation.csv");
        CMEquationFileFormat fileFormat = new CMEquationFileFormat();
        String[] colNames = fileFormat.cols();
        
        for (int i = 0; i < colNames.length; i++) {
            if (i == colNames.length - 1) {
                equationWriter.write(colNames[i]);
                break;
            }
            
            equationWriter.write(colNames[i] + delimiter);
        }
        
        equationWriter.write(System.getProperty("line.separator"));
        
        for (int j = 0; j < controlMeasures.length; j++) {
            if (controlMeasures[j].getEquations().length != 0) {
                equationWriter.write(equationRecord(controlMeasures[j], fileFormat.cols().length));
                equationWriter.write(System.getProperty("line.separator"));
            }
        }

        equationWriter.close();
    }
    
    private String equationRecord(ControlMeasure measure, int size) {
        List list=new ArrayList(size);
         
        String name = measure.getName();
        list.add(0,name);
        
        ControlMeasureEquation cMequation[]=measure.getEquations();
        EquationType equationType=cMequation[0].getEquationType();
        list.add(1,equationType.getName());
        
        for (int k=0; k< cMequation.length; k++){
            EquationTypeVariable typeVariable=cMequation[k].getEquationTypeVariable();
            if (typeVariable!=null)
                list.add(typeVariable.getFileColPosition()+1, cMequation[k].getValue());         
        }       
        
        String equationRecord = list.get(0) + delimiter;
        for (int i=1; i<size-1; i++)
            equationRecord += list.get(i) + delimiter; 
        equationRecord += list.get(size-1) + delimiter; 
        return equationRecord; 
    }
    
        
    private void writeSummaryFile() throws IOException {
        PrintWriter summaryWriter = openExportFile("_summary.csv");
        CMSummaryFileFormat fileFormat = new CMSummaryFileFormat();
        String[] colNames = fileFormat.cols();
        
        for (int i = 0; i < colNames.length; i++) {
            if (i == colNames.length - 1) {
                summaryWriter.write(colNames[i]);
                break;
            }
            
            summaryWriter.write(colNames[i] + delimiter);
        }
        
        summaryWriter.write(System.getProperty("line.separator"));
        
        for (int j = 0; j < controlMeasures.length; j++) {
            summaryWriter.write(summaryRecord(controlMeasures[j]));
            summaryWriter.write(System.getProperty("line.separator"));
        }
        
        summaryWriter.close();
    }
    
    private String summaryRecord(ControlMeasure measure) {
        String name = measure.getName();
        String summaryRecord = (name.indexOf(delimiter) < 0 ? name : ("\"" + name + "\"")) + delimiter;
        summaryRecord += measure.getAbbreviation() + delimiter;
        Pollutant majPollutant = measure.getMajorPollutant();
        summaryRecord += (majPollutant == null ? "" : majPollutant.getName()) + delimiter;
        ControlTechnology ct = measure.getControlTechnology();
        summaryRecord += (ct == null ? "" : ct.getName()) + delimiter;
        SourceGroup sg = measure.getSourceGroup();
        summaryRecord += (sg == null ? "" : sg.getName()) + delimiter;
        summaryRecord += formatSectors(measure.getSectors()) + delimiter;
        summaryRecord += measure.getCmClass() + delimiter;
        summaryRecord += measure.getEquipmentLife() + delimiter;
        summaryRecord += measure.getDeviceCode() + delimiter;
        Date dateRev = measure.getDateReviewed();
        summaryRecord += (dateRev == null ? "" : dateRev.toString()) + delimiter;
        summaryRecord += measure.getDataSouce() + delimiter;
        summaryRecord += measure.getDescription();
        
        return summaryRecord;
    }

    private String formatSectors(Sector[] sectors) {
        String sectorString = "";
        
        for (int i = 0; i < sectors.length; i++) {
            if (i == sectors.length - 1) {
                sectorString += sectors[i].getName();
                break;
            }
            
            sectorString += sectors[i].getName() + "|";
        }
        
        return sectorString;
    }

    private void writeEfficiencyFile() throws IOException {
        PrintWriter efficienciesWriter = openExportFile("_efficiencies.csv");
        CMEfficiencyFileFormatv3 fileFormat = new CMEfficiencyFileFormatv3();
        String[] colNames = fileFormat.cols();
        ControlMeasureDAO dao = new ControlMeasureDAO();
        Session session = factory.getSession();
        
        for (int i = 0; i < colNames.length; i++) {
            if (i == colNames.length - 1) {
                efficienciesWriter.write(colNames[i]);
                break;
            }
            
            efficienciesWriter.write(colNames[i] + delimiter);
        }
        
        efficienciesWriter.write(System.getProperty("line.separator"));
        try {
            for (int j = 0; j < controlMeasures.length; j++) {
                EfficiencyRecord[] records = (EfficiencyRecord[]) dao.getEfficiencyRecords(controlMeasures[j].getId(), session).toArray(new EfficiencyRecord[0]);
                writeEfficiencyRecords(efficienciesWriter, controlMeasures[j].getAbbreviation(), records);
            }
            session.clear();
        } finally {
            session.close();
        }
        efficienciesWriter.close();
    }
    
    private void writeEfficiencyRecords(PrintWriter writer, String abbreviation, EfficiencyRecord[] records) {
        for (int i = 0; i < records.length; i++) {
            writer.write(efficiencyRecord(abbreviation, records[i]));
            writer.write(System.getProperty("line.separator"));
        }
    }

    private String efficiencyRecord(String abbreviation, EfficiencyRecord record) {
        String efficiencyRecord = abbreviation + delimiter;
        Pollutant pollutant = record.getPollutant();
        efficiencyRecord += (pollutant == null ? "" : pollutant.getName()) + delimiter;
        efficiencyRecord += record.getLocale() + delimiter;
        Date effectiveDate = record.getEffectiveDate();
        efficiencyRecord += (effectiveDate == null ? "" : effectiveDate.toString()) + delimiter;
        efficiencyRecord += record.getExistingMeasureAbbr() + delimiter;
        efficiencyRecord += record.getExistingDevCode() + delimiter;
        Double minEmis = record.getMinEmis();
        efficiencyRecord += (minEmis == null ? "" : minEmis.toString()) + delimiter;
        Double maxEmis = record.getMaxEmis();
        efficiencyRecord += (maxEmis == null ? "" : maxEmis.toString()) + delimiter;
        efficiencyRecord += record.getEfficiency() + delimiter;
        efficiencyRecord += record.getCostYear() + delimiter;
        efficiencyRecord += (record.getCostPerTon() == null ? "" : record.getCostPerTon()) + delimiter;
        efficiencyRecord += record.getRuleEffectiveness() + delimiter;
        efficiencyRecord += record.getRulePenetration() + delimiter;
        efficiencyRecord += record.getEquationType() + delimiter;
        efficiencyRecord += record.getCapRecFactor() + delimiter;
        efficiencyRecord += record.getDiscountRate() + delimiter;
        efficiencyRecord += record.getCapitalAnnualizedRatio() + delimiter;
        efficiencyRecord += record.getIncrementalCostPerTon() + delimiter;
        
        efficiencyRecord += record.getDetail();
        
        return efficiencyRecord;
    }

    private void writeSccFile() throws IOException {
        PrintWriter sccsWriter = openExportFile("_SCCs.csv");
        CMSCCsFileFormat fileFormat = new CMSCCsFileFormat();
        String[] colNames = fileFormat.cols();
        
        for (int i = 0; i < colNames.length; i++) {
            if (i == colNames.length - 1) {
                sccsWriter.write(colNames[i]);
                break;
            }
            
            sccsWriter.write(colNames[i] + delimiter);
        }
        
        sccsWriter.write(System.getProperty("line.separator"));
        
        for (int j = 0; j < cmAbbrevSccs.length; j++) {
            sccsWriter.write(cmAbbrevSccs[j] + delimiter);
            sccsWriter.write(System.getProperty("line.separator"));
        }
        
        sccsWriter.close();
    }

    private void addStatus(String message) {
        setStatus(message);
    }

    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("CMExport");
        endStatus.setMessage(message + "\n");
        endStatus.setTimestamp(new Date());

        new StatusDAO(factory).add(endStatus);
    }
    
    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }
    
    public void export(File file) throws ExporterException {
        // NOTE Auto-generated method stub
        throw new ExporterException("Not used method...");
    }

    public long getExportedLinesCount() {
        return this.exportedLinesCount;
    }
    
//    private EquationType[] getEquationTypes() throws EmfException {
//        Session session = factory.getSession();
//        try {
//            List<EquationType> all = new ControlMeasureDAO().getEquationTypes(session);
//            return all.toArray(new EquationType[0]);
//        } catch (RuntimeException e) {
//            throw new EmfException("Could not retrieve control measures Equation Types.");
//        } finally {
//            session.close();
//        }
//    }

}

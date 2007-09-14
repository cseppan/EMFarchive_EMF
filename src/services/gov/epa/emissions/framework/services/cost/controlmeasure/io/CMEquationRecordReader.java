package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureDAO;
import gov.epa.emissions.framework.services.cost.ControlMeasureEquation;
import gov.epa.emissions.framework.services.cost.EquationType;
import gov.epa.emissions.framework.services.cost.EquationTypeVariable;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;

public class CMEquationRecordReader {

    private CMEquationFileFormat fileFormat;

    private CMAddImportStatus status;
    
    //Add EqTypVar map -- contains a map between eq type no to list of variables. 
    private EquationTypeMap equationTypeMap;

    private int errorCount = 0;
    
    private List equList;

    private int errorLimit = 100;
    
    private HibernateSessionFactory sessionFactory;

    public CMEquationRecordReader(CMEquationFileFormat fileFormat, User user, HibernateSessionFactory sessionFactory) throws EmfException {
        this.fileFormat = fileFormat;
        this.status = new CMAddImportStatus(user, sessionFactory);
        this.sessionFactory = sessionFactory;
        this.equationTypeMap = new EquationTypeMap(getEquationTypes());
        this.equList= new ArrayList();
    }

    public void parse(Map controlMeasures, Record record, int lineNo) throws ImporterException {
        StringBuffer sb = new StringBuffer();
        String[] tokens = modify(record, sb, lineNo);
        ControlMeasure cm = controlMeasure(tokens[0], controlMeasures, sb, lineNo);
        if (tokens == null || cm == null)
            return;

        //first lets get the equation type using the map, the seconds columns should
        //contain the equation type name.
        EquationType equationType = equationTypeMap.getEquationType(tokens[1]);
        if (equationType != null) {
            EquationTypeVariable[] equationTypeVariables = equationType.getEquationTypeVariables();
            //get rid of original settings
            cm.setEquations(new ControlMeasureEquation[] {});
            //now add equation settings...
            if (equationTypeVariables.length > 0) {
                if (constraintCheck(tokens[0], equationType, sb)){
                    for (int i = 0; i < equationTypeVariables.length; i++) {

                        EquationTypeVariable equationTypeVariable = equationTypeVariables[i];
                        ControlMeasureEquation equation = new ControlMeasureEquation(equationType);
                        equation.setEquationTypeVariable(equationTypeVariable);
                        try {
                            Double value = Double.valueOf(tokens[equationTypeVariable.getFileColPosition() + 1]);
                            equation.setValue(value);
                        } catch (NumberFormatException e) {
                            sb.append(format("variable value must be a number, column position = " + (equationTypeVariable.getFileColPosition() + 1) + ", value = " + tokens[equationTypeVariable.getFileColPosition() + 1]));
                            break;
                        }
                        cm.addEquation(equation);
                    }
                }
            } else {
                ControlMeasureEquation equation = new ControlMeasureEquation(equationType);
                cm.addEquation(equation);
            }

        } else {
            sb.append(format("unknown equation type '" + tokens[1] + "'"));
        }
        if (sb.length() > 0) {
            errorCount++;
            status.addStatus(lineNo, sb);
        }
        if (errorCount >= errorLimit) throw new ImporterException("The maximum allowable error limit (" + errorLimit + ") has been reached while parsing the control measure equation records.");
    }

    private boolean constraintCheck(String abbre, EquationType equationType, StringBuffer sb) {
        String equString=abbre + equationType.getName();
        if (equList.contains(equString)){ 
            sb.append(format("Equation already in the file: "+ equString));
            return false;
        }
        equList.add(equString);
        return true;
    }

    private ControlMeasure controlMeasure(String token, Map controlMeasures, StringBuffer sb, int lineNo) {
        ControlMeasure cm = (ControlMeasure) controlMeasures.get(token);
        if (cm == null) {
            sb.append(format("abbreviation '" + token + "' is not in the control measure summary file"));
        }
        return cm;
    }

    private String[] modify(Record record, StringBuffer sb, int lineNo) throws ImporterException {
        int sizeDiff = fileFormat.cols().length - record.getTokens().length;
        if (sizeDiff == 0)
            return record.getTokens();

        if (sizeDiff > 0) {
            for (int i = 0; i < sizeDiff; i++) {
                record.add("");
            }
            return record.getTokens();
        }

        throw new ImporterException("The new record has extra tokens");
    }

    private String format(String text) {
        return status.format(text);
    }

    public int getErrorCount() {
        return errorCount;
    }
    
    private EquationType[] getEquationTypes() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List<EquationType> all = new ControlMeasureDAO().getEquationTypes(session);
            return all.toArray(new EquationType[0]);
        } catch (RuntimeException e) {
            throw new EmfException("Could not retrieve control measures Equation Types.");
        } finally {
            session.close();
        }
    }
}

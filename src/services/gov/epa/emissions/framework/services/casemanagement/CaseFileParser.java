package gov.epa.emissions.framework.services.casemanagement;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.importer.CommaDelimitedTokenizer;
import gov.epa.emissions.commons.io.importer.DelimitedFileReader;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.Executable;
import gov.epa.emissions.framework.services.casemanagement.jobs.Host;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobRunStatus;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterEnvVar;
import gov.epa.emissions.framework.services.casemanagement.parameters.ParameterName;
import gov.epa.emissions.framework.services.casemanagement.parameters.ValueType;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class CaseFileParser {
    private File sum_file;

    private File inputs_file;

    private File jobs_file;

    private String[] paramColNames = new String[0];

    private String[] inputColNames = new String[0];

    private String[] jobColNames = new String[0];

    private List<CaseParameter> params = new ArrayList<CaseParameter>();

    private List<ParameterEnvVar> pEnvVars = new ArrayList<ParameterEnvVar>();

    private List<CaseInput> inputs = new ArrayList<CaseInput>();

    private List<InputEnvtVar> inEnvVars = new ArrayList<InputEnvtVar>();

    private List<CaseJob> jobs = new ArrayList<CaseJob>();

    private Case caseObj = new Case();

    public CaseFileParser(String sumFile, String inputsFile, String jobsFile) throws Exception {
        sum_file = new File(sumFile);
        inputs_file = new File(inputsFile);
        jobs_file = new File(jobsFile);
        readProcessAllSummaryLines(sum_file);
        readProcessAllInputsLines(inputs_file);
        readProcessAllJobsLines(jobs_file);
    }

    public Case getCase() {
        return this.caseObj;
    }

    public Abbreviation getCaseAbbreviation() {
        return this.caseObj.getAbbreviation();
    }

    public String[] getParamColNames() {
        return this.paramColNames;
    }

    public String[] getJobColNames() {
        return this.jobColNames;
    }

    public String[] getInputColNames() {
        return this.inputColNames;
    }

    public List<CaseParameter> getParameters() {
        return this.params;
    }

    public List<CaseInput> getInputs() {
        return this.inputs;
    }

    public List<ParameterEnvVar> getParamEnvVars() {
        return this.pEnvVars;
    }

    public String[] getRecordFields(String line, String delimiter) {
        List<String> fields = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(line, delimiter);

        while (st.hasMoreTokens())
            fields.add(st.nextToken().trim());

        return fields.toArray(new String[0]);
    }

    public String[] getNonEmptyFeilds(String[] fields) {
        List<String> realFields = new ArrayList<String>();

        for (int i = 0; i < fields.length; i++)
            if (fields[i].length() > 0)
                realFields.add(fields[i]);

        return realFields.toArray(new String[0]);
    }

    private void readProcessAllSummaryLines(File file) throws Exception {
        try {
            DelimitedFileReader reader = new DelimitedFileReader(file, new CommaDelimitedTokenizer());
            Record record = null;

            for (record = reader.read(); !record.isEnd(); record = reader.read())
                processSummary(record.getTokens());
        } catch (ParseException e) {
            throw new Exception("Summary field not in correct format: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("Could not read summary info: " + e.getMessage());
        }
    }

    private void readProcessAllInputsLines(File file) throws Exception {
        try {
            DelimitedFileReader reader = new DelimitedFileReader(file, new CommaDelimitedTokenizer());
            Record record = null;

            for (record = reader.read(); !record.isEnd(); record = reader.read())
                processInputs(record.getTokens());
        } catch (ParseException e) {
            throw new Exception("Inputs field not in correct format: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("Could not read inputs info: " + e.getMessage());
        }
    }

    private void readProcessAllJobsLines(File file) throws Exception {
        try {
            DelimitedFileReader reader = new DelimitedFileReader(file, new CommaDelimitedTokenizer());
            Record record = null;

            for (record = reader.read(); !record.isEnd(); record = reader.read())
                processJobs(record.getTokens());
        } catch (ParseException e) {
            throw new Exception("Jobs field not in correct format: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("Could not read jobs info: " + e.getMessage());
        }
    }

    private void processSummary(String[] data) throws Exception {
        String line = data[0];

        if (line.isEmpty())
            return;

        if (line.startsWith("\"") && line.endsWith("\""))
            line = line.substring(1, line.length() - 1);

        if (line.startsWith("#")) {
            populateCaseMainFields(line);
            return;
        }

        if (line.startsWith("Tab")) {
            paramColNames = data;
            return;
        }

        if (line.startsWith("Summary")) {
            populateCase(data);
            return;
        }

        if (line.startsWith("Parameters")) {
            addParameter(data);
            return;
        }
    }

    private void populateCaseMainFields(String line) {
        int index = line.indexOf('=') + 1;

        if (line.startsWith("#EMF_CASE_NAME")) {
            caseObj.setName(line.substring(index));
            return;
        }

        if (line.startsWith("#EMF_CASE_ABBREVIATION")) {
            caseObj.setAbbreviation(new Abbreviation(line.substring(index)));
            return;
        }

        if (line.startsWith("#EMF_CASE_DESCRIPTION")) {
            caseObj.setDescription(line.substring(index));
            return;
        }

        if (line.startsWith("#EMF_CASE_CATEGORY")) {
            caseObj.setCaseCategory(new CaseCategory(line.substring(index)));
            return;
        }

        if (line.startsWith("#EMF_PROJECT")) {
            caseObj.setProject(new Project(line.substring(index)));
            return;
        }

        if (line.startsWith("#EMF_CASE_COPIED_FROM")) {
            caseObj.setTemplateUsed(line.substring(index));
            return;
        }

        if (line.startsWith("#EMF_IS_FINAL")) {
            caseObj.setIsFinal(line.substring(index).equalsIgnoreCase("true"));
            return;
        }

        if (line.startsWith("#EMF_IS_TEMPLATE")) {
            caseObj.setCaseTemplate(line.substring(index).equalsIgnoreCase("true"));
            return;
        }
    }

    private void populateCase(String[] values) throws ParseException {
        // NOTE: the order of fields:
        // Tab,Parameter,Order,Envt. Var.,Sector,Job,Program,Value,Type,Reqd?,Local?,Last Modified,Notes,Purpose
        // pEnvVars.add(new ParameterEnvVar(values[3]));
        String value = values[7];

        if (values[1].equalsIgnoreCase("Model to Run")) {
            caseObj.setModel(new ModelToRun(value));
            return;
        }

        if (values[1].equalsIgnoreCase("Model Version")) {
            caseObj.setModelVersion(value);
            return;
        }

        if (values[1].equalsIgnoreCase("Modeling Region")) {
            caseObj.setModelingRegion(new Region(value));
            return;
        }

        if (values[1].equalsIgnoreCase("Grid Name")) {
            caseObj.setGrid(new Grid(value));
            return;
        }

        if (values[1].equalsIgnoreCase("Grid Resolution")) {
            caseObj.setGridResolution(new GridResolution(value));
            return;
        }

        if (values[1].equalsIgnoreCase("Met Layers")) {
            caseObj.setNumMetLayers(value == null || value.equalsIgnoreCase("null") ? null : new Integer(value));
            return;
        }

        if (values[1].equalsIgnoreCase("Emission Layers")) {
            caseObj.setNumEmissionsLayers(value == null || value.equalsIgnoreCase("null") ? null : new Integer(value));
            return;
        }

        if (values[1].equalsIgnoreCase("Downstream Model")) {
            caseObj.setAirQualityModel(new AirQualityModel(value));
            return;
        }

        if (values[1].equalsIgnoreCase("Speciation")) {
            caseObj.setSpeciation(new Speciation(value));
            return;
        }

        if (values[1].equalsIgnoreCase("Meteorological Year")) {
            caseObj.setMeteorlogicalYear(new MeteorlogicalYear(value));
            return;
        }

        if (values[1].equalsIgnoreCase("Base Year")) {
            if (value != null && !value.trim().isEmpty())
                caseObj.setBaseYear(Integer.parseInt(value));

            return;
        }

        if (values[1].equalsIgnoreCase("Future Year")) {
            if (value != null && !value.trim().isEmpty())
                caseObj.setFutureYear(Integer.parseInt(value));
            
            return;
        }

        if (values[1].equalsIgnoreCase("Start Date & Time")) {
            caseObj.setStartDate(CustomDateFormat.parse_MM_DD_YYYY_HH_mm(value));
            return;
        }

        if (values[1].equalsIgnoreCase("End Date & Time")) {
            caseObj.setEndDate(CustomDateFormat.parse_MM_DD_YYYY_HH_mm(value));
            return;
        }

    }

    private void addParameter(String[] fields) throws Exception {
        CaseParameter newParam = new CaseParameter();

        // NOTE: the order of fields:
        // Tab,Parameter,Order,Envt. Var.,Sector,Job,Program,Value,Type,Reqd?,Local?,Last Modified,Notes,Purpose

        newParam.setParameterName(new ParameterName(fields[1]));
        newParam.setOrder(Float.valueOf(fields[2]));
        ParameterEnvVar envar = new ParameterEnvVar(fields[3]);
        pEnvVars.add(envar);
        newParam.setEnvVar(envar);
        newParam.setSector(new Sector(fields[4], fields[4]));
        newParam.setJobName(fields[5]);
        newParam.setProgram(new CaseProgram(fields[6]));
        newParam.setValue(fields[7]);
        newParam.setType(new ValueType(fields[8]));
        newParam.setRequired(fields[9].equalsIgnoreCase("TRUE"));
        newParam.setLocal(fields[10].equalsIgnoreCase("TRUE"));
        newParam.setLastModifiedDate(CustomDateFormat.parse_MM_DD_YYYY_HH_mm(fields[11]));
        newParam.setNotes(fields[12]);
        newParam.setPurpose(fields[13]);

        this.params.add(newParam);
    }

    private void processInputs(String[] data) throws ParseException {
        if (data[0].startsWith("Tab")) {
            inputColNames = data;
            return;
        }

        CaseInput input = new CaseInput();

        // NOTE: the order of fields:
        // Tab,Inputname,Envt Variable,Sector,Job,Program,Dataset,Version,QA status,DS Type,Reqd?,Local?,Subdir,Last
        // Modified,Parentcase

        input.setInputName(new InputName(data[1]));
        InputEnvtVar envVar = new InputEnvtVar(data[2]);
        this.inEnvVars.add(envVar);
        input.setEnvtVars(envVar);
        input.setSector(new Sector(data[3], data[3].equalsIgnoreCase("All sectors") ? "" : data[3]));
        input.setJobName(data[4].equalsIgnoreCase("All jobs for sector") ? "" : data[4]);
        input.setProgram(new CaseProgram(data[5]));
        input.setDataset(new EmfDataset(0, data[6], 0, 0, data[9]));
        Version version = (data[7] == null || data[7].equalsIgnoreCase("null") || data[7].trim().isEmpty()) ? null
                : new Version(Integer.parseInt(data[7]));
        input.setVersion(version);
        input.setDatasetType(new DatasetType(data[9]));
        input.setRequired(data[10].equalsIgnoreCase("TRUE"));
        input.setLocal(data[11].equalsIgnoreCase("TRUE"));
        input.setSubdirObj(new SubDir(data[12]));
        input.setLastModifiedDate(data[13].trim().isEmpty() ? null : CustomDateFormat.parse_MM_DD_YYYY_HH_mm(data[13]));
        input.setParentCase(data[14]);

        this.inputs.add(input);
    }

    private void processJobs(String[] data) throws ParseException {
        if (data[0].startsWith("Tab")) {
            jobColNames = data;
            return;
        }

        CaseJob job = new CaseJob();

        // NOTE: the order of fields:
        // Tab,JobName,Order,Sector,RunStatus,StartDate,CompletionDate,Executable,Arguments,Path,QueueOptions,JobGroup,Local,QueueID,User,Host,Notes,Purpose,DependsOn

        job.setName(data[1]);
        job.setOrder(Integer.parseInt(data[2]));
        job.setSector(new Sector(data[3], data[3].equalsIgnoreCase("All sectors") ? "" : data[3]));
        job.setRunstatus(new JobRunStatus(data[4]));
        job.setRunStartDate(data[5].trim().isEmpty() ? null : CustomDateFormat.parse_MM_DD_YYYY_HH_mm(data[5]));
        job.setRunCompletionDate(data[6].trim().isEmpty() ? null : CustomDateFormat.parse_MM_DD_YYYY_HH_mm(data[6]));
        job.setExecutable(new Executable(data[7]));
        job.setArgs(data[8]);
        job.setPath(data[8]);
        job.setQueOptions(data[9]);
        job.setJobGroup(data[10]);
        job.setLocal(data[11].equalsIgnoreCase("TRUE"));
        job.setIdInQueue(data[12]);
        job.setUser(new User(data[13]));
        job.setHost(new Host(data[14]));
        job.setRunNotes(data[15]);
        job.setPurpose(data[16]);

        this.jobs.add(job);
    }

}

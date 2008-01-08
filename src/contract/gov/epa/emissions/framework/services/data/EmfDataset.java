package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.Country;
import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.ExternalSource;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.data.Mutex;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class EmfDataset implements Dataset, Lockable {

    private int id;// unique id needed for hibernate persistence

    private String name;

    private int year;

    private String description;

    private String status = ""; //so the selection based on status won't fail

    private Region region;

    private IntendedUse intendedUse;

    private Country country;

    private String units;

    private String creator;

    private String temporalResolution;

    private Date startDateTime;

    private Date endDateTime;

    private Project project;

    private Date createdDateTime;

    private Date modifiedDateTime;

    private Date accessedDateTime;

    private DatasetType datasetType;

    private List internalSources;

    private List externalSources;

    private List keyValsList;

    private List sectorsList;

    private int defaultVersion;

    private Mutex lock;

    public EmfDataset() {
        internalSources = new ArrayList();
        externalSources = new ArrayList();
        keyValsList = new ArrayList();
        sectorsList = new ArrayList();
        lock = new Mutex();
    }

    public int getDefaultVersion() {
        return defaultVersion;
    }

    public void setDefaultVersion(int defaultVersion) {
        this.defaultVersion = defaultVersion;
    }

    public Date getAccessedDateTime() {
        return accessedDateTime;
    }

    public void setAccessedDateTime(Date accessedDateTime) {
        this.accessedDateTime = accessedDateTime;
    }

    public Date getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(Date createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public Date getModifiedDateTime() {
        return modifiedDateTime;
    }

    public void setModifiedDateTime(Date modifiedDateTime) {
        this.modifiedDateTime = modifiedDateTime;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getDatasetTypeName() {
        return datasetType != null ? datasetType.getName() : null;
    }

    public DatasetType getDatasetType() {
        return datasetType;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public void setTemporalResolution(String temporalResolution) {
        this.temporalResolution = temporalResolution;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setStartDateTime(Date time) {
        this.startDateTime = time;
    }

    public void setStopDateTime(Date time) {
        this.endDateTime = time;
    }

    public Region getRegion() {
        return region;
    }

    public int getYear() {
        return year;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDatasetType(DatasetType datasetType) {
        this.datasetType = datasetType;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public String getUnits() {
        return units;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreator() {
        return creator;
    }

    public String getTemporalResolution() {
        return temporalResolution;
    }

    public Date getStartDateTime() {
        return startDateTime;
    }

    public Date getStopDateTime() {
        return endDateTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean equals(Object other) {
        if (other == null || !(other instanceof Dataset)) {
            return false;
        }

        Dataset otherDataset = (Dataset) other;

        return (id == otherDataset.getId());
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public InternalSource[] getInternalSources() {
        return (InternalSource[]) this.internalSources.toArray(new InternalSource[0]);
    }

    public void setInternalSources(InternalSource[] internalSources) {
        this.internalSources.clear();
        this.internalSources.addAll(Arrays.asList(internalSources));
    }

    public void addInternalSource(InternalSource source) {
        this.internalSources.add(source);
    }

    public ExternalSource[] getExternalSources() {
        return (ExternalSource[]) this.externalSources.toArray(new ExternalSource[0]);
    }

    public void setExternalSources(ExternalSource[] externalSources) {
        this.externalSources.clear();
        this.externalSources.addAll(Arrays.asList(externalSources));
    }

    public void addExternalSource(ExternalSource source) {
        this.externalSources.add(source);
    }

    public void addSector(Sector sector) {
        sectorsList.add(sector);
    }

    public Sector[] getSectors() {
        return (Sector[]) sectorsList.toArray(new Sector[0]);
    }

    public void setSectors(Sector[] sectors) {
        sectorsList.clear();
        sectorsList.addAll(Arrays.asList(sectors));
    }

    public void addKeyVal(KeyVal keyval) {
        keyValsList.add(keyval);
    }

    public KeyVal[] getKeyVals() {
        return mergeKeyVals();
    }

    public void setKeyVals(KeyVal[] keyvals) {
        keyValsList.clear();
        keyValsList.addAll(Arrays.asList(keyvals));
    }

    public void setSummarySource(InternalSource summary) {
        // TODO: implement Summary
    }

    public InternalSource getSummarySource() {
        return null;
    }

    public Date getLockDate() {
        return lock.getLockDate();
    }

    public void setLockDate(Date lockDate) {
        lock.setLockDate(lockDate);
    }

    public String getLockOwner() {
        return lock.getLockOwner();
    }

    public void setLockOwner(String owner) {
        lock.setLockOwner(owner);
    }

    public boolean isLocked(String owner) {
        return lock.isLocked(owner);
    }

    public boolean isLocked(User owner) {
        return lock.isLocked(owner);
    }

    public boolean isLocked() {
        return lock.isLocked();
    }

    public IntendedUse getIntendedUse() {
        return intendedUse;
    }

    public void setIntendedUse(IntendedUse intendedUse) {
        this.intendedUse = intendedUse;
    }

    public boolean getInlineCommentSetting() {
        KeyVal[] keyvals = getKeyVals();
        for (int i = 0; i < keyvals.length; i++) {
            String keyword = keyvals[i].getKeyword().getName();
            String value = keyvals[i].getValue();
            if (keyword.equalsIgnoreCase(inline_comment_key))
                return (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes")) ? true : false;
        }

        return true;
    }

    public boolean getHeaderCommentsSetting() {
        KeyVal[] keyvals = getKeyVals();
        for (int i = 0; i < keyvals.length; i++) {
            String keyword = keyvals[i].getKeyword().getName();
            String value = keyvals[i].getValue();
            if (keyword.equalsIgnoreCase(header_comment_key))
                return (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes")) ? true : false;
        }

        return true;
    }

    public String getInlineCommentChar() {
        KeyVal[] keyvals = getKeyVals();
        for (int i = 0; i < keyvals.length; i++) {
            String keyword = keyvals[i].getKeyword().getName();
            String value = keyvals[i].getValue();
            if (keyword.equalsIgnoreCase(inline_comment_char))
                return (value.length() == 1) ? value : "!";
        }

        return "!";
    }

    public String getHeaderCommentChar() {
        KeyVal[] keyvals = getKeyVals();
        for (int i = 0; i < keyvals.length; i++) {
            String keyword = keyvals[i].getKeyword().getName();
            String value = keyvals[i].getValue();
            if (keyword.equalsIgnoreCase(header_comment_char))
                return (value.length() == 1) ? value : "#";
        }

        return "#";
    }
    
    public boolean getCSVHeaderLineSetting() {
        KeyVal[] keyvals = getKeyVals();
        for (int i = 0; i < keyvals.length; i++) {
            String keyword = keyvals[i].getKeyword().getName();
            String value = keyvals[i].getValue();
            if (keyword.equalsIgnoreCase(csv_header_line))
                return (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes")) ? true : false;
        }

        return true;
    }

    public boolean isExternal() {
        return getInternalSources().length == 0;
    }

    public KeyVal[] mergeKeyVals() {
        List result = new ArrayList();
        result.addAll(keyValsList);

        if (datasetType == null)
            return (KeyVal[]) result.toArray(new KeyVal[0]);

        KeyVal[] datasetTypeKeyVals = datasetType.getKeyVals();

        for (int i = 0; i < datasetTypeKeyVals.length; i++) {
            if (!contains(result, datasetTypeKeyVals[i])) {
                result.add(datasetTypeKeyVals[i]);
            }
        }
        return (KeyVal[]) result.toArray(new KeyVal[0]);
    }

    public boolean contains(List keyVals, KeyVal newKeyVal) {
        for (Iterator iter = keyVals.iterator(); iter.hasNext();) {
            KeyVal element = (KeyVal) iter.next();
            if (element.getKeyword().equals(newKeyVal.getKeyword()))
                return true;
        }

        return false;
    }

    public String toString() {
        return name;
    }
    
    public void checkAndRunSummaryQASteps(QAStep[] summarySteps, int version) throws EmfException {
        if (!templateExists(summarySteps))
            throw new EmfException("Summary QAStepTemplate doesn't exist in dataset type: " + datasetType.getName());
        
        try {
            //
        } catch (Exception e) {
            throw new EmfException("Cann't run summary QASteps: " + e.getMessage());
        }
    }

    private boolean templateExists(QAStep[] summarySteps) {
        QAStepTemplate[] templates = datasetType.getQaStepTemplates();
        String[] names = new String[templates.length];
        
        for (int i = 0; i < templates.length; i++)
            names[i] = templates[i].getName();
        
        List templateNames = Arrays.asList(names);
        
        for (int i = 0; i < summarySteps.length; i++)
            if (!templateNames.contains(summarySteps[i].getName()))
                return false;
        
        return true;
    }

    public int applicableMonth() {

        //code was copied from SQLQAAnnualQuery.java class
        
        //Check for month name and year name here
        
        //String year = "";
        int month = -1;
        
        
        // The names and/or properties of the dataset are to be checked to determine year and month that 
        // the dataset is for. If there is more than one file for a month, it must be put in its own list
        // with other such files.
        
        // New String Tokenizers for the StartDate and StopDate values.
        // They are compared to determine if they fall in the same month.
        int startYear = -1;
        int startMonth = -1;
        int stopYear = -1;
        int stopMonth = -1;
        Calendar cal = Calendar.getInstance();
        if (startDateTime != null) {
            cal.setTime(startDateTime);
            startYear = cal.get(cal.YEAR);
            startMonth = cal.get(cal.MONTH) + 1;
        }

        if (endDateTime != null) {
            cal.setTime(endDateTime);
            stopYear = cal.get(cal.YEAR);
            stopMonth = cal.get(cal.MONTH) + 1;
        }
        
        // New String Tokenizer to parse the dataset names to find month values.
        StringTokenizer tokenizer7 = new StringTokenizer(name, "_");
        String month2 = "";
        while (tokenizer7.hasMoreTokens()) {
            String unsure = tokenizer7.nextToken();
            if(unsure.equalsIgnoreCase("jan")||unsure.toLowerCase().startsWith("jan")) {
                month2 = "jan";
                break;
            }
            if(unsure.equalsIgnoreCase("feb")||unsure.toLowerCase().startsWith("feb"))
            {
                month2 = "feb";
            break;
            }
            if(unsure.equalsIgnoreCase("mar")||unsure.toLowerCase().startsWith("mar"))
            {
                month2 = "mar";
            break;
            }
            if(unsure.equalsIgnoreCase("apr")||unsure.toLowerCase().startsWith("apr"))
            {
                month2 = "apr";
            break;
            }
            if(unsure.equalsIgnoreCase("may")||unsure.toLowerCase().startsWith("may"))
            {
                month2 = "may";
            break;
            }
            if(unsure.equalsIgnoreCase("jun")||unsure.toLowerCase().startsWith("jun"))
            {
                month2 = "jun";
            break;
            }
            if(unsure.equalsIgnoreCase("jul")||unsure.toLowerCase().startsWith("jul"))
            {
                month2 = "jul";
            break;
            }
            if(unsure.equalsIgnoreCase("aug")||unsure.toLowerCase().startsWith("aug"))
            {
                month2 = "aug";
            break;
            }
            if(unsure.equalsIgnoreCase("sep")||unsure.toLowerCase().startsWith("sep"))
            {
                month2 = "sep";
            break;
            }
            if(unsure.equalsIgnoreCase("oct")||unsure.toLowerCase().startsWith("oct"))
            {
                month2 = "oct";
            break;
            }
            if(unsure.equalsIgnoreCase("nov")||unsure.toLowerCase().startsWith("nov"))
            {
                month2 = "nov";
            break;
            }
            if(unsure.equalsIgnoreCase("dec")||unsure.toLowerCase().startsWith("dec")) {
                {
                    month2 = "dec";
                break;
            }
            }
        }        
    
        if(startMonth == stopMonth && startYear == stopYear) {
            month = startMonth;
            //System.out.println("The month of the dataset from startMonth is: " + month);
        } else if (!(month2.equals(""))){
            if (month2.equalsIgnoreCase("jan") || month2.equalsIgnoreCase("january") || month2.equals("01"))
                month = cal.JANUARY;
            else if (month2.equalsIgnoreCase("feb") || month2.equalsIgnoreCase("february") || month2.equals("02"))
                month = cal.FEBRUARY;
            else if (month2.equalsIgnoreCase("mar") || month2.equalsIgnoreCase("march") || month2.equals("03"))
                month = cal.MARCH;
            else if (month2.equalsIgnoreCase("apr") || month2.equalsIgnoreCase("april") || month2.equals("04"))
                month = cal.APRIL;
            else if (month2.equalsIgnoreCase("may") || month2.equals("05"))
                month = cal.MAY;
            else if (month2.equalsIgnoreCase("jun") || month2.equalsIgnoreCase("june") || month2.equals("06"))
                month = cal.JUNE;
            else if (month2.equalsIgnoreCase("jul") || month2.equalsIgnoreCase("july") || month2.equals("07"))
                month = cal.JULY;
            else if (month2.equalsIgnoreCase("aug") || month2.equalsIgnoreCase("august") || month2.equals("08"))
                month = cal.AUGUST;
            else if (month2.equalsIgnoreCase("sep") || month2.equalsIgnoreCase("september") || month2.equals("09"))
                month = cal.SEPTEMBER;
            else if (month2.equalsIgnoreCase("oct") || month2.equalsIgnoreCase("october") || month2.equals("10"))
                month = cal.OCTOBER;
            else if (month2.equalsIgnoreCase("nov") || month2.equalsIgnoreCase("november") || month2.equals("11"))
                month = cal.NOVEMBER;
            else if (month2.equalsIgnoreCase("dec") || month2.equalsIgnoreCase("december") || month2.equals("12"))
                month = cal.DECEMBER;
            System.out.println("The month of the dataset from month2 is: " + month);
        }
        // Then the file or files must be put into the appropriate method call to create a monthly 
        // query for them.
        
        //System.out.println("The dataset is :" + allDatasetNames.get(j).toString());
        
        //Add exceptions for case where month value not found
        
        return month;
    }

//    public Integer getApplicableYear() throws EmfException {
//
//        //code was copied from SQLQAAnnualQuery.java class
//        
//        //Check for month name and year name here
//        
//        Integer year = null;
//        
//        
//        // The names and/or properties of the dataset are to be checked to determine year and month that 
//        // the dataset is for. If there is more than one file for a month, it must be put in its own list
//        // with other such files.
//        
//        // New String Tokenizers for the StartDate and StopDate values.
//        // They are compared to determine if they fall in the same month.
//        
//        StringTokenizer tokenizer5 = new StringTokenizer(startDateTime.toString());
//        
//        String yearMonthDay = tokenizer5.nextToken();
//        StringTokenizer tokenizer8 = new StringTokenizer(yearMonthDay, "-");
//        
//        String startYear = tokenizer8.nextToken();
//        String startMonth = tokenizer8.nextToken();
//        
//        StringTokenizer tokenizer6 = new StringTokenizer(endDateTime.toString());
//        
//        String yearMonthDay2 = tokenizer6.nextToken();
//        StringTokenizer tokenizer9 = new StringTokenizer(yearMonthDay2, "-");
//        
//        String stopYear = tokenizer9.nextToken();
//        String stopMonth = tokenizer9.nextToken();
//        
//        if(startMonth.equals(stopMonth) && startYear.equals(stopYear)) {
//            year = Integer.parseInt(startYear);
//            //System.out.println("The month of the dataset from startMonth is: " + month);
//            //System.out.println("The month of the dataset from month2 is: " + month);
//        }else {
//            throw new EmfException("The dataset covers more than one month.");
//        }
//        return year;
//    }
}

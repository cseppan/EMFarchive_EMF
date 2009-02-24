package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.TableCreator;
import gov.epa.emissions.commons.db.TableModifier;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.meta.keywords.Keywords;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasureClass;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;

public class DatasetCreator {

//    private String tablePrefix;

    private User user;

//    private String outputDatasetName;

    private HibernateSessionFactory sessionFactory;

    private DbServerFactory dbServerFactory;

//    private String datasetNamePrefix;
    
    private ControlStrategy controlStrategy;

    private Keywords keywordMasterList;
    
    private Datasource datasource;

    public DatasetCreator(ControlStrategy controlStrategy, User user, 
            HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory,
            Datasource datasource, Keywords keywordMasterList) {
//        this.datasetNamePrefix = datasetNamePrefix;
//        this.tablePrefix = tablePrefix;
        this.user = user;
        this.sessionFactory = sessionFactory;
        this.dbServerFactory = dbServerFactory;
//        this.outputDatasetName = getResultDatasetName(strategy.getName());
        this.controlStrategy = controlStrategy;
        this.datasource = datasource;
        this.keywordMasterList = keywordMasterList;//new Keywords(new DataCommonsServiceImpl(sessionFactory).getKeywords());
    }

    public EmfDataset addDataset(String datasetNamePrefix, String tablePrefix, 
            EmfDataset inputDataset, DatasetType type, 
            TableFormat tableFormat, String description) throws EmfException {
        String outputDatasetName = createResultDatasetName(datasetNamePrefix, inputDataset);
        String outputTableName = createTableName(tablePrefix, inputDataset);
        
        //create dataset
        EmfDataset dataset = createDataset(outputDatasetName, description, type, inputDataset);

        setDatasetInternalSource(dataset, outputTableName, 
                tableFormat, inputDataset.getName());

        //persist dataset to db
        add(dataset);
        try {
            addVersionZeroEntryToVersionsTable(dataset);
        } catch (Exception e) {
            throw new EmfException("Cannot add version zero entry to versions table for dataset: " + dataset.getName());
        }

        createTable(outputTableName, tableFormat);

        return dataset;
    }

    public EmfDataset addDataset(String tablePrefix, 
            String datasetName, DatasetType type, 
            TableFormat tableFormat, String description) throws EmfException {
        String outputTableName = createTableName(tablePrefix, datasetName);
        
        //create dataset
        EmfDataset dataset = createDataset(datasetName, description, type);

        setDatasetInternalSource(dataset, outputTableName, 
                tableFormat, datasetName);

        //persist dataset to db
        add(dataset);
        try {
            addVersionZeroEntryToVersionsTable(dataset);
        } catch (Exception e) {
            throw new EmfException("Cannot add version zero entry to versions table for dataset: " + dataset.getName());
        }

        createTable(outputTableName, tableFormat);

        return dataset;
    }

    public EmfDataset addDataset(String datasetName, 
            EmfDataset inputDataset, DatasetType type, 
            TableFormat tableFormat, String description
//            ,Map<String,String> keywordValues
            ) throws EmfException {
//        return addDataset(datasetName, "DS", 
//                inputDataset, type, 
//                tableFormat, description);
        String outputDatasetName = createDatasetName(datasetName);
        String outputTableName = createTableName(datasetName);
        
        //create dataset
        EmfDataset dataset = createDataset(outputDatasetName, description, type, inputDataset);
        
//        Iterator iterator = keywordValues.entrySet().iterator();
//
//        Map.Entry entry =  (Map.Entry)iterator.next();
//        String keyword = (String) entry.getKey();
//        String value = (String) entry.getValue();
//
//        while (iterator.hasNext()) {
//            entry =  (Map.Entry)iterator.next();
//            keyword = (String) entry.getKey();
//            value = (String) entry.getValue();
//            addKeyVal(dataset, keyword, value);
//        }

        setDatasetInternalSource(dataset, outputTableName, 
                tableFormat, inputDataset.getName());

        //persist dataset to db
        add(dataset);
        try {
            addVersionZeroEntryToVersionsTable(dataset);
        } catch (Exception e) {
            throw new EmfException("Cannot add version zero entry to versions table for dataset: " + dataset.getName());
        }

        createTable(outputTableName, tableFormat);

        return dataset;
    }

    public EmfDataset addDataset(String datasetNamePrefix, String tablePrefix, 
            EmfDataset inputDataset, DatasetType type, 
            TableFormat tableFormat) throws EmfException {
        return addDataset(datasetNamePrefix, tablePrefix, 
                inputDataset, type, 
                tableFormat, detailedResultDescription(inputDataset));
    }

    private EmfDataset createDataset(String name, 
            String description,
            DatasetType type,
            EmfDataset inputDataset) {
        EmfDataset dataset = createDataset(name, 
                description,
                type);

        //Add properties from input dataset...
        dataset.setStartDateTime(inputDataset.getStartDateTime());
        dataset.setStopDateTime(inputDataset.getStopDateTime());
        dataset.setTemporalResolution(inputDataset.getTemporalResolution());
        dataset.setSectors(inputDataset.getSectors());
        dataset.setRegion(inputDataset.getRegion());
        dataset.setCountry(inputDataset.getCountry());
        
        return dataset;
    }
    
    private EmfDataset createDataset(String name, 
            String description,
            DatasetType type) {
        EmfDataset dataset = new EmfDataset();
        Date start = new Date();

        dataset.setName(name);
        dataset.setCreator(user.getUsername());
        dataset.setDatasetType(type);
        dataset.setDescription(description);
        dataset.setCreatedDateTime(start);
        dataset.setModifiedDateTime(start);
        dataset.setAccessedDateTime(start);
        dataset.setStatus("Created by control strategy");
        
        dataset.setRegion(controlStrategy.getRegion());

        //Add keywords to the dataset
        addKeyVals(dataset);
        
        return dataset;
    }
    
    private void addKeyVals(EmfDataset dataset) {
        //Add keywords to the dataset
        addKeyVal(dataset, "COST_YEAR", controlStrategy.getCostYear() + "");
        addKeyVal(dataset, "STRATEGY_TYPE", controlStrategy.getStrategyType().getName());
        addKeyVal(dataset, "TARGET_POLLUTANT", controlStrategy.getTargetPollutant() != null ? controlStrategy.getTargetPollutant().getName() : "Not Specified");
        addKeyVal(dataset, "REGION", controlStrategy.getRegion() != null ? controlStrategy.getRegion().getName() : "Not Specified");
        addKeyVal(dataset, "STRATEGY_NAME", controlStrategy.getName());
        addKeyVal(dataset, "STRATEGY_ID", controlStrategy.getId()+"");
//        addKeyVal(dataset, "STRATEGY_INVENTORY_NAME", inputDataset.getName());
//        addKeyVal(dataset, "STRATEGY_INVENTORY_VERSION", inputDataset.getDefaultVersion()+"");
        int measureCount = (controlStrategy.getControlMeasures() != null ? controlStrategy.getControlMeasures().length : 0);
        ControlMeasureClass[] controlMeasureClasses = controlStrategy.getControlMeasureClasses();
        String classList = "All";
        if (controlMeasureClasses != null) {
            if (controlMeasureClasses.length > 0) classList = "";
            for (int i = 0; i < controlMeasureClasses.length; i++) {
                if (classList.length() > 0) classList += ", ";  
                classList += controlMeasureClasses[i].getName();
            }
        }
        if (measureCount > 0) 
            addKeyVal(dataset, "MEASURES_INCLUDED", measureCount + "");
        else
            addKeyVal(dataset, "MEASURE_CLASSES", classList);
        addKeyVal(dataset, "DISCOUNT_RATE", controlStrategy.getDiscountRate()+"%");
        addKeyVal(dataset, "USE_COST_EQUATION", (controlStrategy.getUseCostEquations()==true? "true" : "false"));
        addKeyVal(dataset, "INCLUDE_UNSPECIFIED_COSTS", (controlStrategy.getIncludeUnspecifiedCosts()==true? "true" : "false"));
    }
    
    private void addKeyVal(EmfDataset dataset, String keywordName, String value) {
        Keyword keyword = keywordMasterList.get(keywordName);
        KeyVal keyval = new KeyVal(keyword, value); 
        dataset.addKeyVal(keyval);
    }
    
    private void addVersionZeroEntryToVersionsTable(Dataset dataset) throws Exception {
        TableModifier modifier = new TableModifier(datasource, "versions");
        String[] data = { null, dataset.getId() + "", "0", "Initial Version", "", "true", null };
        modifier.insertOneRow(data);
    }

    private void setDatasetInternalSource(EmfDataset dataset, String tableName, TableFormat tableFormat, String source) {
        InternalSource internalSource = new InternalSource();
        internalSource.setTable(tableName);
        internalSource.setType(tableFormat.identify());
        internalSource.setCols(colNames(tableFormat.cols()));
        internalSource.setSource(source);
        dataset.setInternalSources(new InternalSource[] { internalSource });
    }

    private String[] colNames(Column[] cols) {
        List names = new ArrayList();
        for (int i = 0; i < cols.length; i++)
            names.add(cols[i].name());

        return (String[]) names.toArray(new String[0]);
    }

    private String createResultDatasetName(String datasetNamePrefix, EmfDataset inputDataset) {
        return createDatasetName(datasetNamePrefix+ "_" + inputDataset.getId() 
                + "_V" + inputDataset.getDefaultVersion());
    }
        
    public static String createDatasetName(String name) {
        //name += "_" + CustomDateFormat.format_YYYYMMDDHHMMSSSS(new Date());
        if (name.length() > 46) {     //postgresql table name max length is 64
            name = name.substring(0, 45);
        }

        for (int i = 0; i < name.length(); i++) {
            if (!Character.isLetterOrDigit(name.charAt(i))) {
                name = name.replace(name.charAt(i), '_');
            }
        }

        return name.trim().replaceAll(" ", "_") + "_" + CustomDateFormat.format_YYYYMMDDHHMMSSSS(new Date());
    }
        
    private String createTableName(String tablePrefix, EmfDataset inputDataset) {
        String prefix = tablePrefix + "_" + inputDataset.getId() 
            + "_V" + inputDataset.getDefaultVersion();
        String name = inputDataset.getName();
        return createTableName(prefix, name);
    }
    
    private String createTableName(String tablePrefix, String name) {
        return createTableName(tablePrefix + "_" + name);
    }

    private String createTableName(String name) {
        String table = name;
        //truncate if necessary so a unique timestamp can be added to ensure uniqueness
        if (table.length() > 46) {     //postgresql table name max length is 64
            table = table.substring(0, 45);
        }

        for (int i = 0; i < table.length(); i++) {
            if (!Character.isLetterOrDigit(table.charAt(i))) {
                table = table.replace(table.charAt(i), '_');
            }
        }

        //add unique timestamp to ensure uniqueness
        return table.trim().replaceAll(" ", "_") + "_" + CustomDateFormat.format_YYYYMMDDHHMMSSSS(new Date());
    }

    private void add(EmfDataset dataset) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            DatasetDAO dao = new DatasetDAO(dbServerFactory);
            if (dao.datasetNameUsed(dataset.getName()))
                throw new EmfException("The selected dataset name is already in use.");

            dao.add(dataset, session);
        } catch (Exception e) {
            throw new EmfException("Could not add dataset: " + dataset.getName());
        } finally {
            session.close();
        }
    }
    
    private void createTable(String tableName, TableFormat tableFormat) throws EmfException {
        TableCreator creator = new TableCreator(datasource);
        try {
            if (creator.exists(tableName))
                creator.drop(tableName);

            creator.create(tableName, tableFormat);
        } catch (Exception e) {
            throw new EmfException("Could not create table '" + tableName + "'+\n" + e.getMessage());
        }
    }
    
    public String detailedResultDescription(EmfDataset inputDataset) {
        return "#Control strategy detailed result\n" + 
           "#Implements control strategy: " + controlStrategy.getName() + "\n"
                + "#Input dataset used: " + inputDataset.getName()+"\n#";
    }

}

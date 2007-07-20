package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.TableModifier;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.importer.DataTable;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.meta.keywords.Keywords;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasureClass;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.data.DataCommonsServiceImpl;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;

public class DatasetCreator {

    private String prefix;

    private User user;

    private String outputDatasetName;

    private HibernateSessionFactory sessionFactory;

    private DbServerFactory dbServerFactory;

    private String datasetNamePrefix;
    
    private ControlStrategy strategy;

    private Keywords keywordMasterList;
    
    public DatasetCreator(String datasetNamePrefix, String tablePrefix, ControlStrategy strategy, User user, HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory) throws EmfException {
        this.datasetNamePrefix = datasetNamePrefix;
        this.prefix = tablePrefix;
        this.user = user;
        this.sessionFactory = sessionFactory;
        this.dbServerFactory = dbServerFactory;
        this.outputDatasetName = getResultDatasetName(strategy.getName());
        this.strategy = strategy;
        this.keywordMasterList = new Keywords(new DataCommonsServiceImpl(sessionFactory).getKeywords());
    }

    public String outputTableName() {
        return prefix+outputDatasetName;
    }
    
    public EmfDataset addDataset(EmfDataset inputDataset, DatasetType type, String description, TableFormat tableFormat, String source, Datasource datasource) throws EmfException{
        EmfDataset dataset = new EmfDataset();
        Date start = new Date();

        dataset.setName(datasetNamePrefix+outputDatasetName);
        dataset.setCreator(user.getUsername());
        dataset.setDatasetType(type);
        dataset.setDescription(description);
        dataset.setCreatedDateTime(start);
        dataset.setModifiedDateTime(start);
        dataset.setAccessedDateTime(start);
        dataset.setStatus("Created by control strategy");

        //Add properties from input dataset...
        dataset.setStartDateTime(inputDataset.getStartDateTime());
        dataset.setStopDateTime(inputDataset.getStopDateTime());
        dataset.setTemporalResolution(inputDataset.getTemporalResolution());
        dataset.setSectors(inputDataset.getSectors());
        dataset.setRegion(inputDataset.getRegion());
        dataset.setCountry(inputDataset.getCountry());
        
        //Add keyword to the dataset
        addKeyVal(dataset, "COST_YEAR", strategy.getCostYear() + "");
        addKeyVal(dataset, "STRATEGY_TYPE", strategy.getStrategyType().getName());
        addKeyVal(dataset, "TARGET_POLLUTANT", strategy.getTargetPollutant().getName());
        addKeyVal(dataset, "REGION", strategy.getRegion() != null ? strategy.getRegion().getName() : "");
        addKeyVal(dataset, "STRATEGY_NAME", strategy.getName());
        addKeyVal(dataset, "STRATEGY_ID", strategy.getId()+"");
        addKeyVal(dataset, "STRATEGY_INVENTORY_NAME", inputDataset.getName());
        addKeyVal(dataset, "STRAGEGY_INVENTORY_VERSION", inputDataset.getDefaultVersion()+"");
        int measureCount = (strategy.getControlStrategyMeasures() != null ? strategy.getControlStrategyMeasures().length : 0);
        addKeyVal(dataset, "MEASURES_INCLUDED", measureCount + "");
        ControlMeasureClass[] controlMeasureClasses = strategy.getControlMeasureClasses();
        String classList = "All";
        if (controlMeasureClasses != null) {
            if (controlMeasureClasses.length > 0) classList = "";
            for (int i = 0; i < controlMeasureClasses.length; i++) {
                if (classList.length() > 0) classList += ", ";  
                classList += controlMeasureClasses[i].getName();
            }
        }
        addKeyVal(dataset, "MEASURE_CLASSES", classList);
        
        setDatasetInternalSource(dataset, outputTableName(),tableFormat, source);
        add(dataset);
        try {
            addVersionZeroEntryToVersionsTable(dataset, datasource);
        } catch (Exception e) {
            throw new EmfException("Cannot add version zero entry to versions table for dataset: " + dataset.getName());
        }

        return dataset;
    }
    
    private void addKeyVal(EmfDataset dataset, String keywordName, String value) {
        Keyword keyword = keywordMasterList.get(keywordName);
        KeyVal keyval = new KeyVal(keyword, value); 
        dataset.addKeyVal(keyval);
    }
    
    private void addVersionZeroEntryToVersionsTable(Dataset dataset, Datasource datasource) throws Exception {
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

    private String getResultDatasetName(String name) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MMddyyyy_HHmmss");
        String timestamp = dateFormatter.format(new Date());
        return DataTable.encodeTableName(
                ((prefix + name + "_" + timestamp).length() <= 63 
                    ? name
                    : name.substring(0, 63 - (prefix + "_" + timestamp).length())) 
                        + "_" + timestamp);
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
    


    

}

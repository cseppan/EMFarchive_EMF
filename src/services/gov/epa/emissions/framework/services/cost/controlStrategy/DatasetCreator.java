package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.TableModifier;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.importer.DataTable;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
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

    private String datasetNamePrefix;

    public DatasetCreator(String datasetNamePrefix, String tablePrefix, ControlStrategy strategy, User user, HibernateSessionFactory sessionFactory) {
        this.datasetNamePrefix = datasetNamePrefix;
        this.prefix = tablePrefix;
        this.user = user;
        this.sessionFactory = sessionFactory;
        this.outputDatasetName = getResultDatasetName(strategy.getName());
    }

    public String outputTableName() {
        return prefix+outputDatasetName;
    }
    
    public EmfDataset addDataset(DatasetType type,String description, TableFormat tableFormat, String source, Datasource datasource) throws EmfException{
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
        setDatasetInternalSource(dataset, outputTableName(),tableFormat, source);
        add(dataset);
        try {
            addVersionZeroEntryToVersionsTable(dataset, datasource);
        } catch (Exception e) {
            throw new EmfException("Cannot add version zero entry to versions table for dataset: " + dataset.getName());
        }

        return dataset;
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

        return DataTable.encodeTableName(name + "_" + timestamp);
    }
    
    private void add(EmfDataset dataset) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            DatasetDAO dao = new DatasetDAO();
            if (dao.nameUsed(dataset.getName(), EmfDataset.class, session))
                throw new EmfException("The selected dataset name is already in use.");

            dao.add(dataset, session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not add dataset: " + dataset.getName());
        } finally {
            session.close();
        }
    }
    


    

}

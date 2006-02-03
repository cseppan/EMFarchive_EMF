package gov.epa.emissions.framework.dao;

import gov.epa.emissions.commons.db.DbUpdate;
import gov.epa.emissions.commons.db.HibernateTestCase;
import gov.epa.emissions.commons.db.postgres.PostgresDbUpdate;
import gov.epa.emissions.commons.io.Country;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.commons.io.Project;
import gov.epa.emissions.commons.io.Region;
import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.framework.db.ExImDbUpdate;
import gov.epa.emissions.framework.services.EmfDataset;

import java.util.Date;
import java.util.List;
import java.util.Random;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Transaction;

public class DatasetPersistenceTest extends HibernateTestCase {

    private String datasetName;

    private DataCommonsDAO dcDao;

    protected void setUp() throws Exception {
        super.setUp();
        datasetName = "A1" + new Random().nextLong();
        dcDao = new DataCommonsDAO();

    }

    protected void doTearDown() throws Exception {
        DbUpdate emissionsUpdate = new PostgresDbUpdate(emissions().getConnection());
        emissionsUpdate.deleteAll(emissions().getName(), "versions");

        ExImDbUpdate eximUpdate = new ExImDbUpdate();
        eximUpdate.deleteAllDatasets();

        super.doTearDown();
    }

    public void testVerifySimplePropertiesAreStored() throws Exception {
        Country country = new Country("FR" + Math.random());
        Project project = new Project("P1" + Math.random());
        EmfDataset dataset = new EmfDataset();

        Region region = new Region("USA" + Math.random());
        try {
            dcDao.add(country, session);
            dcDao.add(project, session);
            dcDao.add(region, session);

            dataset.setAccessedDateTime(new Date());
            dataset.setCountry(country);
            dataset.setCreatedDateTime(new Date());
            dataset.setCreator("CFD");
            dataset.setDescription("DESCRIPTION");
            dataset.setModifiedDateTime(new Date());
            dataset.setName(datasetName);
            dataset.setProject(project);
            dataset.setRegion(region);
            dataset.setSectors(new Sector[] { new Sector("", "S1" + Math.random()) });
            dataset.setStartDateTime(new Date());
            dataset.setStatus("imported");
            dataset.setYear(42);
            dataset.setUnits("orl");
            dataset.setTemporalResolution("t1");
            dataset.setStopDateTime(new Date());

            DatasetType type = load("ORL Nonpoint Inventory");
            dataset.setDatasetType(type);

            KeyVal kv = new KeyVal();
            kv.setValue("bar-1");
            kv.setKeyword(new Keyword("bar-key"));
            dataset.addKeyVal(kv);

            save(dataset);
        } finally {
            remove(dataset);

            remove(country);
            remove(region);
            remove(project);
        }

    }

    private DatasetType load(String name) {
        Query query = session.createQuery("SELECT type FROM DatasetType AS type WHERE name='" + name + "'");
        List list = query.list();
        return list.size() == 1 ? (DatasetType) list.get(0) : null;
    }

    private void save(Object element) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(element);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    private void remove(Object object) {
        Transaction tx = session.beginTransaction();
        session.delete(object);
        tx.commit();
    }

}

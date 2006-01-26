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
        Country country = new Country("FR");
        Project project = new Project("P1");
        EmfDataset ds = new EmfDataset();

        Region region = new Region("USA");
        try {
            dcDao.add(country, session);
            dcDao.add(project, session);
            dcDao.add(region, session);

            ds.setAccessedDateTime(new Date());
            ds.setCountry(country);
            ds.setCreatedDateTime(new Date());
            ds.setCreator("CFD");
            ds.setDescription("DESCRIPTION");
            ds.setModifiedDateTime(new Date());
            ds.setName(datasetName);
            ds.setProject(project);
            ds.setRegion(region);
            ds.setSectors(new Sector[] { new Sector("", "S1") });
            ds.setStartDateTime(new Date());
            ds.setStatus("imported");
            ds.setYear(42);
            ds.setUnits("orl");
            ds.setTemporalResolution("t1");
            ds.setStopDateTime(new Date());

            DatasetType type = load("ORL Nonpoint Inventory");
            ds.setDatasetType(type);

            KeyVal kv = new KeyVal();
            kv.setValue("bar-1");
            kv.setKeyword(new Keyword("bar-key"));
            ds.addKeyVal(kv);

            save(ds);

            kv.setValue(null);
            update(ds);
        } finally {
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

    private void update(Object element) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.update(element);
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

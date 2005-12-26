package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.db.PostgresDbUpdate;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.HibernateTestCase;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class DatasetTypePersistenceTest extends HibernateTestCase {

    protected void setUp() throws Exception {
        super.setUp();
        drop("NAME");
    }

    protected void doTearDown() throws Exception {
        drop("NAME");
    }

    public void testVerifySimplePropertiesAreStored() throws Exception {
        DatasetType type = new DatasetType();
        type.setDescription("TEST");
        type.setName("NAME");
        type.setMaxfiles(1);
        type.setMinfiles(1);

        save(type);
        DatasetType loadedType = load("NAME");
        assertNotNull("DatasetType with name - 'NAME' should have been persisted ", loadedType);
    }

    public void testVerifyKeywordsAreStored() throws Exception {
        DatasetType type = new DatasetType();
        type.setDescription("TEST");
        type.setName("NAME");
        type.addKeyword(new Keyword("key1"));
        type.addKeyword(new Keyword("key2"));

        save(type);
        DatasetType loadedType = load("NAME");
        assertNotNull(loadedType);
        assertEquals(2, loadedType.getKeywords().length);
        assertEquals("key1", loadedType.getKeywords()[0].getName());
        assertEquals("key2", loadedType.getKeywords()[1].getName());
    }

    public void FIXME_testVerifyUpdatedKeywordIsStored() throws Exception {
        DatasetType type = new DatasetType();
        type.setDescription("TEST");
        type.setName("NAME");
        type.addKeyword(new Keyword("key1"));
        type.addKeyword(new Keyword("key2"));

        save(type);

        DatasetType loadedType = load("NAME");
        assertEquals(2, loadedType.getKeywords().length);
        Keyword key1 = loadedType.getKeywords()[0];
        assertEquals("key1", key1.getName());
        key1.setName("updated-key1");

        update(type);

        DatasetType updatedType = load("NAME");
        Keyword updatedKey = updatedType.getKeywords()[0];
        assertEquals("updated-key1", updatedKey.getName());
    }

    private DatasetType load(String name) {
        Session session = session();
        try {
            Query query = session.createQuery("SELECT type FROM DatasetType AS type WHERE name='" + name + "'");
            List list = query.list();
            return list.size() == 1 ? (DatasetType) list.get(0) : null;
        } finally {
            session.close();
        }
    }

    private void drop(String name) {
        try {
            PostgresDbUpdate update = new PostgresDbUpdate();
            update.delete("emf.dataset_types", "name", name + "");
            update.deleteAll("emf.emf_keywords");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void save(DatasetType type) {
        Transaction tx = null;
        Session session = session();
        try {
            tx = session.beginTransaction();
            session.save(type);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    private void update(DatasetType type) {
        Transaction tx = null;
        Session session = session();
        try {
            tx = session.beginTransaction();
            session.update(type);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

}

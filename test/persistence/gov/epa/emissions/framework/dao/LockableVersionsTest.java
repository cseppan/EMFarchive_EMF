package gov.epa.emissions.framework.dao;

import gov.epa.emissions.commons.db.DataModifier;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.HibernateTestCase;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;

import java.sql.SQLException;

public class LockableVersionsTest extends HibernateTestCase {

    private Datasource datasource;

    private String versionsTable;

    private LockableVersions lockableVersions;

    private Versions versions;

    private UserDAO userDao;

    private User owner;

    protected void setUp() throws Exception {
        super.setUp();

        DbServer dbServer = dbSetup.getDbServer();
        datasource = dbServer.getEmissionsDatasource();
        versionsTable = "versions";

        setupData(datasource, versionsTable);

        versions = new Versions();
        lockableVersions = new LockableVersions(versions);

        userDao = new UserDAO();
        owner = userDao.get("emf", session);
    }

    protected void doTearDown() throws Exception {
        dropData(versionsTable, datasource);
    }

    private void setupData(Datasource datasource, String table) throws SQLException {
        addRecord(datasource, table, new String[] { null, "1", "0", "version zero", "", "true" });
    }

    private void addRecord(Datasource datasource, String table, String[] data) throws SQLException {
        DataModifier modifier = datasource.dataModifier();
        modifier.insertRow(table, data);
    }

    public void testShouldObtainLockedVersionForUpdate() {
        Version version = versions.get(1, 0, session);

        Version locked = lockableVersions.obtainLocked(owner, version, session);
        assertEquals(locked.getLockOwner(), owner.getUsername());

        Version loadedFromDb = versions.get(1, 0, session);
        assertEquals(loadedFromDb.getLockOwner(), owner.getUsername());
    }

    public void testShouldFailToGetLockWhenAlreadyLockedByAnotherUser() {
        Version version = versions.get(1, 0, session);
        lockableVersions.obtainLocked(owner, version, session);

        User user = userDao.get("admin", session);
        Version result = lockableVersions.obtainLocked(owner, version, session);
        // failed to obtain lock for another user
        assertFalse("Should have failed to obtain lock as it's already locked by another user", result.isLocked(user));
    }

    public void testShouldReleaseLockOnRelease() throws EmfException {
        Version version = versions.get(1, 0, session);
        Version locked = lockableVersions.obtainLocked(owner, version, session);

        Version released = lockableVersions.releaseLocked(locked, session);
        assertFalse("Should have released lock", released.isLocked());

        Version loadedFromDb = versions.get(1, 0, session);
        assertFalse("Should have released lock", loadedFromDb.isLocked());
    }

    public void testShouldFailToReleaseLockIfNotObtained() {
        Version version = versions.get(1, 0, session);

        try {
            lockableVersions.releaseLocked(version, session);
        } catch (EmfException e) {
            assertEquals("Cannot release without owning lock", e.getMessage());
            return;
        }

        fail("Should have failed to release lock that was not obtained");
    }

    public void testShouldUpdateVersionAfterObtainingLock() throws Exception {
        Version version = versions.get(1, 0, session);

        Version locked = lockableVersions.obtainLocked(owner, version, session);
        assertEquals(locked.getLockOwner(), owner.getUsername());
        locked.setName("TEST");

        Version modified = lockableVersions.update(locked, session);
        assertEquals("TEST", locked.getName());
        assertEquals(modified.getLockOwner(), null);
    }
}

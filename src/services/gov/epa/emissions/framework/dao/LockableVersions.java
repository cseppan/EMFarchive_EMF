package gov.epa.emissions.framework.dao;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;

import java.util.Arrays;
import java.util.List;

import org.hibernate.Session;

public class LockableVersions {

    private Versions versions;

    private LockingScheme lockingScheme;

    public LockableVersions(Versions versions) {
        this.versions = versions;
        lockingScheme = new LockingScheme();
    }

    public Version obtainLocked(User owner, Version version, Session session) {
        return (Version) lockingScheme.getLocked(owner, version, session, all(version, session));
    }

    private List all(Version version, Session session) {
        Version[] all = versions.get(version.getDatasetId(), session);
        return Arrays.asList(all);
    }

    public Version releaseLocked(Version locked, Session session) throws EmfException {
        return (Version) lockingScheme.releaseLock(locked, session, all(locked, session));
    }

    public Version releaseLockOnUpdate(Version locked, Session session) throws EmfException {
        return (Version) lockingScheme.releaseLockOnUpdate(locked, session, all(locked, session));
    }

    public Version renewLockOnUpdate(Version locked, Session session) throws EmfException {
        return (Version) lockingScheme.renewLockOnUpdate(locked, session, all(locked, session));
    }

}

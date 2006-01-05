package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.io.Lockable;
import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.commons.security.User;

import java.util.Date;

import junit.framework.TestCase;

public class EmfDatasetTest extends TestCase {
    public void testShouldBeLockedOnlyIfUsernameAndDateIsSet() {
        Lockable locked = new EmfDataset();
        locked.setLockOwner("user");
        locked.setLockDate(new Date());
        assertTrue("Should be locked", locked.isLocked());

        Lockable unlockedAsOnlyUsernameIsSet = new Sector();
        unlockedAsOnlyUsernameIsSet.setLockOwner("user");
        assertFalse("Should be unlocked", unlockedAsOnlyUsernameIsSet.isLocked());

        Lockable unlockedAsOnlyLockedDateIsSet = new Sector();
        unlockedAsOnlyLockedDateIsSet.setLockDate(new Date());
        assertFalse("Should be unlocked", unlockedAsOnlyLockedDateIsSet.isLocked());
    }

    public void testShouldBeLockedIfUsernameMatches() throws Exception {
        Lockable locked = new EmfDataset();
        locked.setLockOwner("user");
        locked.setLockDate(new Date());

        User lockedByUser = new User();
        lockedByUser.setUsername("user");
        assertTrue("Should be locked", locked.isLocked(lockedByUser));

        User notLockedByUser = new User();
        notLockedByUser.setUsername("user2");
        assertFalse("Should not be locked", locked.isLocked(notLockedByUser));
    }
}

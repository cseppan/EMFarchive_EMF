package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.services.EmfException;

public interface PopulateUserStrategy {

    void populate(String name, String affiliation, String phone, String email, String username, char[] password,
            char[] confirmPassword, Boolean wantEmails, DatasetType[] eDatasetTypes) throws EmfException;
    void checkNewPwd(char[] password) throws EmfException;
}

package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.EmfException;

import java.security.MessageDigest;

import sun.misc.BASE64Encoder;

public class PasswordService {

    /**
     * This method encrypts the plain text password and returns a string.
     */
    public static String encrypt(String textPassword) throws EmfException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA");
            md.update(textPassword.getBytes("UTF-8"));

            byte raw[] = md.digest();
            String hash = (new BASE64Encoder()).encode(raw);
            return hash;
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }

    }

}

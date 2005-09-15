/*
 * Creation on Sep 12, 2005
 * Eclipse Project Name: EMF
 * File Name: PasswordService.java
 * Author: Conrad F. D'Cruz
 */
/**
 * 
 */

package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.EmfException;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import sun.misc.BASE64Encoder;

/**
 * @author Conrad F. D'Cruz
 *
 */
public class PasswordService {

	/**
	 * This method encrypts the plain text password and returns a
	 * string.
	 * 
	 * @param textPassword
	 * @return encrypted password
	 * @throws EmfException
	 */
	  public static String encrypt(String textPassword) throws EmfException
	  {
	    MessageDigest md = null;
	    try
	    {
	      md = MessageDigest.getInstance("SHA");
	    }
	    catch(NoSuchAlgorithmException e)
	    {
	      throw new EmfException(e.getMessage());
	    }
	    try
	    {
	      md.update(textPassword.getBytes("UTF-8"));
	    }
	    catch(UnsupportedEncodingException e)
	    {
	      throw new EmfException(e.getMessage());
	    }
	    byte raw[] = md.digest();
	    String hash = (new BASE64Encoder()).encode(raw);
	    return hash;
	  }

	  /**
	   * A utility main to encrypt some of the passwords
	   * in the database.  Remove after devlopment.
	   * @param args
	   */
	  public static void main(String[]args){

		String plainPwd="james12345";
		try {
			System.out.println(encrypt(plainPwd));
		} catch (EmfException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	  }
}//PasswordService

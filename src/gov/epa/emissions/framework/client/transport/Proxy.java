package gov.epa.emissions.framework.client.transport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import org.apache.soap.*;
import org.apache.soap.util.xml.*;
import org.apache.soap.encoding.*;
import org.apache.soap.encoding.soapenc.*;
import org.apache.soap.rpc.*;

public class Proxy implements InvocationHandler 
{
	private String urn = null;
	private static URL serverURL = null;
	final private static String _paramName = "Parameter";
	private SOAPMappingRegistry smr = null;
	private BeanSerializer beanSer = null;

	final public static Object newInstance(String urn, Class[] interfaces) 
	{
		Proxy pi = new Proxy(urn);
		return(pi.initialize(interfaces));         
	}

	// Implementation of the java.lang.reflect.InvocationHandler interface
	final public Object invoke(Object proxy, Method m, Object[] args) throws Throwable
	{
		Call call = new Call();         
		call.setTargetObjectURI(urn);
		call.setMethodName(m.getName());
		call.setEncodingStyleURI(Constants.NS_URI_SOAP_ENC);

		java.util.Vector params = new java.util.Vector();       
		for( int i=0; i<args.length; i++ )
		{
			if( isSimple(args[i]) || isSimpleArray(args[i]) )
			{
				params.add(new Parameter(_paramName+(i+1),args[i].getClass(),args[i],null));               
			}
			else if( isVector(args[i]) )
			{
				addMapping((java.util.Vector)args[i]);            
				params.add(new Parameter(_paramName+(i+1),args[i].getClass(),args[i],null));               
			}
			// if this is an non-simple array then
			// Assume that this is an array of beans
			else if( isArray(args[i]) )
			{            
				if( smr == null )
					smr = new SOAPMappingRegistry();
				if( beanSer == null )
					beanSer = new BeanSerializer();
				//System.out.println("Adding a default mapping");
				ArraySerializer arraySer = new ArraySerializer();
				smr.mapTypes(Constants.NS_URI_SOAP_ENC, null, null, beanSer, beanSer);	
				smr.mapTypes(Constants.NS_URI_SOAP_ENC,null,args[i].getClass(), arraySer, arraySer);                     
				params.add(new Parameter(_paramName+(i+1),args[i].getClass(),args[i],null));               
			}
			// Assume that this is a bean
			else
			{
				if( smr == null )
					smr = new SOAPMappingRegistry();
				if( beanSer == null )
					beanSer = new BeanSerializer();
				String qnamePart = args[i].getClass().getName();
				//System.out.println("qnamePart = " + qnamePart);
				smr.mapTypes(Constants.NS_URI_SOAP_ENC,
							 new QName(urn, qnamePart),args[i].getClass(), beanSer, beanSer);                                          
				params.add(new Parameter(_paramName+(i+1),args[i].getClass(),args[i],null));               
			}
		}
		if( params.size() != 0 )
			call.setParams(params);

		if( smr != null )
			call.setSOAPMappingRegistry(smr);

		// Invoke the call.
		Response resp = call.invoke(serverURL, "");         
		if( !resp.generatedFault() )
		{
			Parameter ret = resp.getReturnValue();
			return(ret.getValue());
		}
		else
		{
			Fault fault = resp.getFault();
			throw new SOAPException(fault.getFaultCode(),fault.getFaultString());
		}
	}

	// Private methods...

	// Not allowed to "construct" a new instance.
	private Proxy(String urn) 
	{
		this.urn = urn;
		if( serverURL == null )
		{
			synchronized(this)
			{
				if( serverURL == null )
				{
					String url = "http://ben.cep.unc.edu:8080/emf/services/EMFUserManagerService";
					if( url == null )
						throw new RuntimeException("System property SOAPEndPoint must be defined.");
					try
					{
						serverURL = new URL(url);
					}
					catch( Exception e )
					{
						throw new RuntimeException(e.getMessage());
					}
				}
			}
		}
	}

	private Object initialize(Class[] interfaces) 
	{
		return(java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(),interfaces,this));
	}            

	private boolean isSimple(Object o)
	{
		if( o instanceof Integer || o instanceof Double || o instanceof Boolean ||
			o instanceof Byte || o instanceof Character || o instanceof Float ||
			o instanceof String || o instanceof Short || o instanceof Long )
			return(true);

		return(false);
	}

	private boolean isSimpleArray(Object o)
	{
		if( o instanceof int[] || o instanceof boolean[] || o instanceof long[] || o instanceof float[] ||
			o instanceof short[] || o instanceof byte[] || o instanceof java.lang.Integer[] || o instanceof java.lang.Double[] || o instanceof java.lang.Boolean[] ||
			o instanceof java.lang.Byte[] || o instanceof java.lang.Float[] ||
			o instanceof java.lang.String[] || o instanceof java.lang.Short[] || o instanceof java.lang.Long[] )
			return(true);

		return(false);
	}

	private boolean isVector(Object o)
	{
		if( o instanceof java.util.Vector )
			return(true);
		return(false);
	}

	private boolean isArray(Object o)
	{
		if( o instanceof Object[] )
			return(true);
		return(false);
	}

	private void addMapping(java.util.Vector v)
	{
		Object o = v.get(0);
		if( o == null )
			return;

		if( isSimple(o) || isSimpleArray(o) )
		{
			//System.out.println("Vector contains simple elements only.");
		}
		else if( isVector(o) )
		{
			//System.out.println("Recursive Vector...");
			addMapping((java.util.Vector)o);
		}
		else
		{
			if( smr == null )
				smr = new SOAPMappingRegistry();
			if( beanSer == null )
				beanSer = new BeanSerializer();
			//System.out.println("Adding a default mapping");
			smr.mapTypes(Constants.NS_URI_SOAP_ENC, null, null, beanSer, beanSer);                                 
		}
	}
}



package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.framework.client.cost.controlstrategy.LocaleFilter;
import junit.framework.TestCase;

public class LocaleFilterTest extends TestCase {

    public void testShouldPassTheLocalForTheFips() {
        String fips = "101033";
        LocaleFilter filter = new LocaleFilter();
        assertTrue(filter.acceptLocale("", fips));
//FIXME  -- need to filter by country, state, and county...
//        assertTrue(filter.acceptLocale("10", fips));
//FIXME  -- need to filter correctly by country, state, and county...
//        assertTrue(filter.acceptLocale("101033", fips));
    }
    
    public void testShouldNotPassTheLocalForTheFips() {
        String fips = "101033";
        LocaleFilter filter = new LocaleFilter();
        assertFalse(filter.acceptLocale("20", fips));
        assertFalse(filter.acceptLocale("30000", fips));
        assertFalse(filter.acceptLocale("101037", fips));
        
        assertFalse(filter.acceptLocale("101031","10103"));
    }

}

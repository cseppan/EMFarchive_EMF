package gov.epa.emissions.framework.client.cost.controlstrategy;

public class LocaleFilter {

    public boolean acceptLocale(String locale,String fips) {
        if (locale.length() > fips.length())
            return false;

        String pattern = pattern(locale, fips.length());
        return fips.matches(pattern);
    }

    private String pattern(String locale, int length) {
        int localeLength = locale.length();
         return locale+ "\\d{" +(length-localeLength)+"}";
    }

}

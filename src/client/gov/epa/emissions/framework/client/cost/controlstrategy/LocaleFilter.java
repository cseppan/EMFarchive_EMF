package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

import java.util.ArrayList;
import java.util.List;

public class LocaleFilter {

    public boolean acceptLocale(String locale, String fips) {
        if (locale.length() > fips.length())
            return false;

        return fips.regionMatches(fips.length() - locale.length(), locale, 0, locale.length());
//        String pattern = pattern(locale, fips.length());
//        return fips.matches(pattern);
    }

//    private String pattern(String locale, int length) {
//        int localeLength = locale.length();
//        return locale + "\\d{" + (length - localeLength) + "}";
//    }

    public EfficiencyRecord[] closestRecords(List records) {
        if (records.isEmpty())
            return new EfficiencyRecord[0];

        List closesRecords = new ArrayList();
        EfficiencyRecord closeRecord = (EfficiencyRecord) records.get(0);
        closesRecords.add(closeRecord);

        for (int i = 1; i < records.size(); i++) {
            EfficiencyRecord record = (EfficiencyRecord) records.get(i);
            if (isCloser(closesRecords, record, closeRecord))
                closeRecord = record;
        }
        return (EfficiencyRecord[]) closesRecords.toArray(new EfficiencyRecord[0]);
    }

    private boolean isCloser(List closerRecords, EfficiencyRecord record, EfficiencyRecord closeRecord) {
        if (record.getLocale().length() > closeRecord.getLocale().length()) {
            closerRecords.clear();
            closerRecords.add(record);
            return true;
        }

        if (record.getLocale().length() == closeRecord.getLocale().length())
            closerRecords.add(closeRecord);

        return false;
    }

}
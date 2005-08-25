package gov.epa.emissions.commons.io.exporter.orl;

import corejava.Format;

public interface ORLFormats {

    public static final Format SCC_FORMAT = new Format("%10s");

    public static final Format SIC_FORMAT = new Format("%4s");

    public static final Format MACT_FORMAT = new Format("%6s");

    public static final Format SRCTYPE_FORMAT = new Format("%2s");

    public static final Format NAICS_FORMAT = new Format("%6s");

    public static final Format POLL_FORMAT = new Format("%16s");

    public static final Format PLANTID_FORMAT = new Format("%15s");

    public static final Format POINTID_FORMAT = new Format("%15s");

    public static final Format STACKID_FORMAT = new Format("%15s");

    public static final Format SEGMENT_FORMAT = new Format("%15s");

    public static final Format PLANT_FORMAT = new Format("%40s");

    public static final Format STKDIAM_FORMAT = new Format("%7.4f");

    public static final Format STKFLOW_FORMAT = new Format("%9.4f");

    public static final Format CTYPE_FORMAT = new Format("%1s");

    public static final Format XLOC_FORMAT = new Format("%9.4f");

    public static final Format YLOC_FORMAT = new Format("%9.4f");

    public static final Format UTMZ_FORMAT = new Format("%2d");

    public static final Format CPRI_FORMAT = new Format("%5d");

    public static final Format CSEC_FORMAT = new Format("%5d");

}

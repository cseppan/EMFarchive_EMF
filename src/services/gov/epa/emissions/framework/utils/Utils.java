package gov.epa.emissions.framework.utils;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.casemanagement.InputName;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.services.data.GeoRegion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Utils {

    public static final InputComparator G_TO_L_INPUT_COMPARATOR = new InputComparator();

    public static final ParameterComparator G_TO_L_PARAMETER_COMPARATOR = new ParameterComparator();

    public static void sortInputs(List<CaseInput> inputs) {
        Collections.sort(inputs, G_TO_L_INPUT_COMPARATOR);
    }

    public static void sortParameters(List<CaseParameter> parameters) {
        Collections.sort(parameters, G_TO_L_PARAMETER_COMPARATOR);
    }

    public static CaseInput match(CaseInput inputToMatch, List<CaseInput> inputs) {

        CaseInput matchedInput = null;

        // sortInputs(inputs);
        //
        // for (CaseInput caseInput : inputs) {
        //
        // int jobID = caseInput.getCaseJobID();
        // int jobIDtoMatch = inputToMatch.getCaseJobID();
        // GeoRegion region = caseInput.getRegion();
        // GeoRegion regionToMatch = inputToMatch.getRegion();
        // Sector sector = caseInput.getSector();
        // Sector sectorToMatch = inputToMatch.getSector();
        // }

        return matchedInput;
    }

    public static boolean areEqualOrBothNull(Object o1, Object o2) {

        boolean equal = true;

        if ((o1 == null && o2 != null) || (o1 != null && o2 == null)) {
            equal = false;
        } else {
            equal = o1.equals(o2);
        }

        return equal;
    }

    static class InputComparator implements Comparator<CaseInput> {

        enum Order {
            GREATEST_TO_LEAST(-1), LEAST_TO_GREATEST(1);

            private int factor = 1;

            Order(int factor) {
                this.factor = factor;
            }

            public int getFactor() {
                return factor;
            }
        }

        private Order order;

        public InputComparator() {
            this(Order.GREATEST_TO_LEAST);
        }

        public InputComparator(Order order) {
            this.order = order;
        }

        /**
         * null and null --> equal !null and !null --> equal
         */
        public int compare(CaseInput o1, CaseInput o2) {

            int retval = 0;

            System.out.println("Comparing:");
            String o1String = "o1=" + stringify(o1);
            System.out.println("  " + o1String);
            String o2String = "o2=" + stringify(o2);
            System.out.println("  " + o2String);

            int factor = order.getFactor();

            int id1 = this.getJobID(o1);
            int id2 = this.getJobID(o2);
            if (id1 != 0 && id2 == 0) {
                retval = 1 * factor;
            } else if (id1 == 0 && id2 != 0) {
                retval = -1 * factor;
            } else {

                Sector sector1 = this.getSector(o1);
                Sector sector2 = this.getSector(o2);
                if (sector1 != null && sector2 == null) {
                    retval = 1 * factor;
                } else if (sector1 == null && sector2 != null) {
                    retval = -1 * factor;
                } else {

                    GeoRegion region1 = this.getRegion(o1);
                    GeoRegion region2 = this.getRegion(o2);
                    if (region1 != null && region2 == null) {
                        retval = 1 * factor;
                    } else if (region1 == null && region2 != null) {
                        retval = -1 * factor;
                    }
                }
            }

            if (retval == 1) {
                System.out.println("o1>o2");
            } else if (retval == -1) {
                System.out.println("o2>o1");
            } else {
                System.out.println("o1==o2");
            }

            return retval;
        }

        int getJobID(CaseInput o) {
            return o.getCaseJobID();
        }

        GeoRegion getRegion(CaseInput o) {
            return o.getRegion();
        }

        Sector getSector(CaseInput o) {
            return o.getSector();
        }
    }

    static class ParameterComparator implements Comparator<CaseParameter> {

        enum Order {
            GREATEST_TO_LEAST(-1), LEAST_TO_GREATEST(1);

            private int factor = 1;

            Order(int factor) {
                this.factor = factor;
            }

            public int getFactor() {
                return factor;
            }
        }

        private Order order;

        public ParameterComparator() {
            this(Order.GREATEST_TO_LEAST);
        }

        public ParameterComparator(Order order) {
            this.order = order;
        }

        /**
         * null and null --> equal !null and !null --> equal
         */
        public int compare(CaseParameter o1, CaseParameter o2) {

            int retval = 0;

            System.out.println("Comparing:");
            String o1String = "o1=" + stringify(o1);
            System.out.println("  " + o1String);
            String o2String = "o2=" + stringify(o2);
            System.out.println("  " + o2String);

            int factor = order.getFactor();

            int id1 = this.getJobID(o1);
            int id2 = this.getJobID(o2);
            if (id1 != 0 && id2 == 0) {
                retval = 1 * factor;
            } else if (id1 == 0 && id2 != 0) {
                retval = -1 * factor;
            } else {

                Sector sector1 = this.getSector(o1);
                Sector sector2 = this.getSector(o2);
                if (sector1 != null && sector2 == null) {
                    retval = 1 * factor;
                } else if (sector1 == null && sector2 != null) {
                    retval = -1 * factor;
                } else {

                    GeoRegion region1 = this.getRegion(o1);
                    GeoRegion region2 = this.getRegion(o2);
                    if (region1 != null && region2 == null) {
                        retval = 1 * factor;
                    } else if (region1 == null && region2 != null) {
                        retval = -1 * factor;
                    }
                }
            }

            if (retval == 1) {
                System.out.println("o1>o2");
            } else if (retval == -1) {
                System.out.println("o2>o1");
            } else {
                System.out.println("o1==o2");
            }

            return retval;
        }

        int getJobID(CaseParameter o) {
            return o.getJobId();
        }

        GeoRegion getRegion(CaseParameter o) {
            return o.getRegion();
        }

        Sector getSector(CaseParameter o) {
            return o.getSector();
        }
    }

    // static abstract class AbstractComparator<T> implements Comparator<T> {
    //
    // private Order order;
    //
    // abstract int getJobID(T o);
    //
    // abstract Sector getSector(T o);
    //
    // abstract GeoRegion getRegion(T o);
    //
    // enum Order {
    // GREATEST_TO_LEAST(-1), LEAST_TO_GREATEST(1);
    //
    // private int factor = 1;
    //
    // Order(int factor) {
    // this.factor = factor;
    // }
    //
    // public int getFactor() {
    // return factor;
    // }
    // }
    //
    // public AbstractComparator() {
    // this(Order.GREATEST_TO_LEAST);
    // }
    //
    // public AbstractComparator(Order order) {
    // this.order = order;
    // }
    //
    // /**
    // * null and null --> equal !null and !null --> equal
    // */
    // public int compare(T o1, T o2) {
    //
    // int retval = 0;
    //
    // System.out.println("Comparing:");
    // String o1String = "o1=" + stringify(o1);
    // System.out.println("  " + o1String);
    // String o2String = "o2=" + stringify(o2);
    // System.out.println("  " + o2String);
    //
    // int factor = order.getFactor();
    //
    // int id1 = this.getJobID(o1);
    // int id2 = this.getJobID(o2);
    // if (id1 != 0 && id2 == 0) {
    // retval = 1 * factor;
    // } else if (id1 == 0 && id2 != 0) {
    // retval = -1 * factor;
    // } else {
    //
    // Sector sector1 = this.getSector(o1);
    // Sector sector2 = this.getSector(o2);
    // if (sector1 != null && sector2 == null) {
    // retval = 1 * factor;
    // } else if (sector1 == null && sector2 != null) {
    // retval = -1 * factor;
    // } else {
    //
    // GeoRegion region1 = this.getRegion(o1);
    // GeoRegion region2 = this.getRegion(o2);
    // if (region1 != null && region2 == null) {
    // retval = 1 * factor;
    // } else if (region1 == null && region2 != null) {
    // retval = -1 * factor;
    // }
    // }
    // }
    //
    // if (retval == 1) {
    // System.out.println("o1>o2");
    // } else if (retval == -1) {
    // System.out.println("o2>o1");
    // } else {
    // System.out.println("o1==o2");
    // }
    //
    // return retval;
    // }
    // }
    //
    // private static String stringify(Object o) {
    //
    // String retval = "";
    // if (o instanceof CaseInput) {
    // retval = stringify((CaseInput) o);
    // } else if (o instanceof CaseParameter) {
    // retval = stringify((CaseParameter) o);
    // } else {
    // retval = o.toString();
    // }
    //
    // return retval;
    // }

    public static String stringify(CaseInput input) {

        int id1 = input.getCaseJobID();
        Sector sector1 = input.getSector();
        GeoRegion region1 = input.getRegion();

        String jobId1 = "all";
        if (id1 != 0) {
            jobId1 = Integer.toString(id1);
        }

        String sectorName1 = "all";
        if (sector1 != null) {
            sectorName1 = sector1.getName();
        }

        String regionName1 = "all";
        if (region1 != null) {
            regionName1 = region1.getName();
        }

        return regionName1 + ", " + sectorName1 + ", " + jobId1;
    }

    public static String stringify(CaseParameter parameter) {

        int id1 = parameter.getJobId();
        Sector sector1 = parameter.getSector();
        GeoRegion region1 = parameter.getRegion();

        String jobId1 = "all";
        if (id1 != 0) {
            jobId1 = Integer.toString(id1);
        }

        String sectorName1 = "all";
        if (sector1 != null) {
            sectorName1 = sector1.getName();
        }

        String regionName1 = "all";
        if (region1 != null) {
            regionName1 = region1.getName();
        }

        return regionName1 + ", " + sectorName1 + ", " + jobId1;
    }

    public static void main(String[] args) {

        List<CaseInput> inputs = new ArrayList<CaseInput>();

        GeoRegion region1 = new GeoRegion("r1");
        // GeoRegion region2 = new GeoRegion("r2");
        Sector sector1 = new Sector("s1", "s1");
        // Sector sector2 = new Sector("s2", "s2");

        CaseInput input = new CaseInput();
        input.setInputName(new InputName("null, null, j1"));
        input.setCaseJobID(1);
        inputs.add(input);

        // input = new CaseInput();
        // input.setInputName(new InputName("r2, null, null"));
        // input.setRegion(region2);
        // inputs.add(input);

        input = new CaseInput();
        input.setInputName(new InputName("r1, s1, null"));
        input.setRegion(region1);
        input.setSector(sector1);
        inputs.add(input);

        // input = new CaseInput();
        // input.setInputName(new InputName("r2, s1, null"));
        // input.setRegion(region2);
        // input.setSector(sector1);
        // inputs.add(input);

        input = new CaseInput();
        input.setInputName(new InputName("null, s1, null"));
        input.setSector(sector1);
        inputs.add(input);

        // input = new CaseInput();
        // input.setInputName(new InputName("null, s2, null"));
        // input.setSector(sector2);
        // inputs.add(input);

        // input = new CaseInput();
        // input.setInputName(new InputName("r1, s2, null"));
        // input.setRegion(region1);
        // input.setSector(sector2);
        // inputs.add(input);

        // input = new CaseInput();
        // input.setInputName(new InputName("r2, s2, null"));
        // input.setRegion(region2);
        // input.setSector(sector1);
        // inputs.add(input);

        input = new CaseInput();
        input.setInputName(new InputName("null, null, null"));
        inputs.add(input);

        input = new CaseInput();
        input.setInputName(new InputName("r1, null, j1"));
        input.setCaseJobID(1);
        input.setRegion(region1);
        inputs.add(input);

        // input = new CaseInput();
        // input.setInputName(new InputName("r2, null, j1"));
        // input.setCaseJobID(1);
        // input.setRegion(region2);
        // inputs.add(input);

        input = new CaseInput();
        input.setInputName(new InputName("r1, s1, j1"));
        input.setCaseJobID(1);
        input.setRegion(region1);
        input.setSector(sector1);
        inputs.add(input);

        // input = new CaseInput();
        // input.setInputName(new InputName("r2, s1, j1"));
        // input.setCaseJobID(1);
        // input.setRegion(region2);
        // input.setSector(sector1);
        // inputs.add(input);

        input = new CaseInput();
        input.setInputName(new InputName("null, s1, j1"));
        input.setCaseJobID(1);
        input.setSector(sector1);
        inputs.add(input);

        input = new CaseInput();
        input.setInputName(new InputName("r1, null, null"));
        input.setRegion(region1);
        inputs.add(input);

        // input = new CaseInput();
        // input.setInputName(new InputName("null, s2, j1"));
        // input.setCaseJobID(1);
        // input.setSector(sector2);
        // inputs.add(input);

        // input = new CaseInput();
        // input.setInputName(new InputName("r1, s2, j1"));
        // input.setCaseJobID(1);
        // input.setRegion(region1);
        // input.setSector(sector2);
        // inputs.add(input);

        // input = new CaseInput();
        // input.setInputName(new InputName("r2, s2, j1"));
        // input.setCaseJobID(1);
        // input.setRegion(region2);
        // input.setSector(sector1);
        // inputs.add(input);

        System.out.println("Before:");
        for (CaseInput caseInput : inputs) {
            System.out.println(caseInput);
        }

        sortInputs(inputs);

        System.out.println();
        System.out.println("After:");
        for (CaseInput caseInput : inputs) {
            System.out.println(caseInput);
        }

    }
}

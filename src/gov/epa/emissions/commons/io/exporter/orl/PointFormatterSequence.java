package gov.epa.emissions.commons.io.exporter.orl;

import java.util.ArrayList;
import java.util.List;

public class PointFormatterSequence implements FormatterSequence {

    private List formatters;

    public PointFormatterSequence() {
        this.formatters = new ArrayList();

        formatters.add(new FipsFormatter());
        formatters.add(new PlantIdFormatter());
        formatters.add(new PointIdFormatter());
        formatters.add(new StackIdFormatter());
        formatters.add(new SegmentFormatter());
        formatters.add(new PlantFormatter());
        formatters.add(new SccFormatter());
        formatters.add(new ErpTypeFormatter());
        formatters.add(new SrcTypeFormatter());
        formatters.add(new StkHgtFormatter());
        formatters.add(new StkDiamFormatter());
        formatters.add(new StkTempFormatter());
        formatters.add(new StkFlowFormatter());
        formatters.add(new StkVelFormatter());
        formatters.add(new SicFormatter());
        formatters.add(new MactFormatter());
        formatters.add(new NaicsFormatter());
        formatters.add(new CTypeFormatter());
        formatters.add(new XLocFormatter());
        formatters.add(new YLocFormatter());
        formatters.add(new UTMZoneFormatter());
        formatters.add(new PollFormatter());
        formatters.add(new AnnEmisFormatter());
        formatters.add(new AvdEmisFormatter());
        formatters.add(new CeffFormatter());
        formatters.add(new ReffFormatter());
        formatters.add(new CpriFormatter());
        formatters.add(new CsecFormatter());
    }

    public List sequence() {
        return formatters;
    }

}

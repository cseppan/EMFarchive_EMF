package gov.epa.emissions.commons.io.exporter.orl;

import java.util.ArrayList;
import java.util.List;

public class NonRoadFormatterSequence implements FormatterSequence {

    private List formatters;

    public NonRoadFormatterSequence() {
        this.formatters = new ArrayList();

        formatters.add(new FipsFormatter());
        formatters.add(new SccFormatter());
        formatters.add(new PollFormatter());
        formatters.add(new AnnEmisFormatter());
        formatters.add(new AvdEmisFormatter());
        formatters.add(new CeffFormatter());
        formatters.add(new ReffFormatter());
        formatters.add(new RpenFormatter());
    }

    public List sequence() {
        return formatters;
    }

}

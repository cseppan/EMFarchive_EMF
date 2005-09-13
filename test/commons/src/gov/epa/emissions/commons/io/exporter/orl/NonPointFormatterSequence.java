package gov.epa.emissions.commons.io.exporter.orl;

import java.util.ArrayList;
import java.util.List;

public class NonPointFormatterSequence implements FormatterSequence {

    private List formatters;

    public NonPointFormatterSequence() {
        this.formatters = new ArrayList();

        formatters.add(new FipsFormatter());
        formatters.add(new SccFormatter());
        formatters.add(new SicFormatter());
        formatters.add(new MactFormatter());
        formatters.add(new SrcTypeFormatter());
        formatters.add(new NaicsFormatter());
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

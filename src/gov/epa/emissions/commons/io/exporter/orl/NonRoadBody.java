package gov.epa.emissions.commons.io.exporter.orl;

import java.util.ArrayList;
import java.util.List;

public class NonRoadBody extends AbstractORLBody {

    private List formatters;

    public NonRoadBody() {
        this.formatters = new ArrayList();

        formatters.add(new FipsFormatter());
        formatters.add(new SccFormatter());
        formatters.add(new CasFormatter());
        formatters.add(new AnnEmisFormatter());
        formatters.add(new AvdEmisFormatter());
        formatters.add(new CeffFormatter());
        formatters.add(new ReffFormatter());
        formatters.add(new RpenFormatter());
    }

    protected List getFormatters() {
        return formatters;
    }

}

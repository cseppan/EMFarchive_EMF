package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.RowSource;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class QAStepRowSource implements RowSource {

    private QAStep source;

    private DateFormat dateFormat;

    public QAStepRowSource(QAStep source) {
        this.source = source;
        dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    }

    public Object[] values() {
        return new Object[] { new Integer(source.getVersion()), source.getName(), Boolean.valueOf(source.isRequired()),
                source.getOrder() + "", source.getStatus(), format(source.getWhen()), source.getWho(),
                source.getResult(), source.getProgram(), source.getProgramArguments() };
    }

    private Object format(Date date) {
        return date == null ? "N/A" : dateFormat.format(date);
    }

    public Object source() {
        return source;
    }

    public void validate(int rowNumber) {// No Op
    }

    public void setValueAt(int column, Object val) {// No Op
    }
}
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
        return new Object[] { new Long(source.getVersion()), source.getName(), source.getWho(),
                format(source.getWhen()), source.getProgram(), Boolean.valueOf(source.isRequired()),
                new Float(source.getOrder()), source.getResult(), source.getStatus() };
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
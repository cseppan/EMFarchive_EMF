package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.RowSource;

import java.text.DateFormat;
import java.text.ParseException;

public class EditableQAStepRowSource implements RowSource {

    private QAStep source;

    private Boolean selected;

    public EditableQAStepRowSource(QAStep source) {
        this.source = source;
        this.selected = Boolean.FALSE;
    }

    public Object[] values() {
        return new Object[] { selected,
                Integer.toString(source.getVersion()), source.getName(), source.getProgram(),
                source.getProgramArguments(), Boolean.valueOf(source.isRequired()), new Float(source.getOrder()),
                source.getWhen(), source.getWho(), source.getResult(), source.getStatus() };
    }

    public void setValueAt(int column, Object val) {
        switch (column) {
        case 0:
            selected = (Boolean) val;
            break;
        case 1:
            source.setVersion(Integer.parseInt((String) val));
            break;
        case 2:
            source.setName((String) val);
            break;
        case 3:
            source.setProgram((String) val);
            break;
        case 4:
            source.setProgramArguments((String) val);
            break;
        case 5:
            source.setRequired(((Boolean) val).booleanValue());
            break;
        case 6:
            source.setOrder(((Float)val).floatValue());
            break;
        case 7:
            try {
                source.setWhen(DateFormat.getDateInstance().parse((String) val));
            } catch (ParseException e) {
                throw new RuntimeException(e.getMessage());
            }
            break;
        case 8:
            source.setWho((User) val);
            break;
        case 9:
            source.setResult((String) val);
            break;
        case 10:
            source.setStatus((String) val);
            break;
        default:
            throw new RuntimeException("invalid column - " + column);
        }
    }

    public Object source() {
        return source;
    }

    public boolean isSelected() {
        return selected.booleanValue();
    }

    public void validate(int rowNumber) {
        // NOTE no validation needed

    }

}

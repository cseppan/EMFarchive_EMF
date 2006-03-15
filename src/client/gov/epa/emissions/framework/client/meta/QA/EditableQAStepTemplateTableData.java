package gov.epa.emissions.framework.client.meta.QA;

import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.AbstractEditableTableData;
import gov.epa.emissions.framework.ui.EditableRow;
import gov.epa.emissions.framework.ui.RowSource;
import gov.epa.emissions.framework.ui.InlineEditableTableData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EditableQAStepTemplateTableData extends AbstractEditableTableData implements InlineEditableTableData {

    private List rows;
    
    public EditableQAStepTemplateTableData(QAStepTemplate[] templates) {
        this.rows = createRows(templates);
    }

    public void addBlankRow() {
        QAStepTemplate template = new QAStepTemplate();
        template.setName("");
        template.setProgram("");
        template.setProgramArguments("");
        template.setRequired(false);
        template.setOrder("");
        
        rows.add(row(template));
    }

    public void removeSelected() {
        remove(getSelected());
    }
    
    public void remove(QAStepTemplate template) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            QAStepTemplate source = (QAStepTemplate) row.source();
            if (source == template) {
                rows.remove(row);
                return;
            }
        }
    }
    
    public void remove(QAStepTemplate[] templates) {
        for(int i = 0; i < templates.length; i++)
            remove(templates[i]);
    }

    public String[] columns() {
        return new String[] { "Select", "Name", "Program", "Arguments", "Required", "Order" };
    }

    public List rows() {
        return this.rows;
    }

    public boolean isEditable(int col, int row) {
        EditableRow editableRow = (EditableRow) rows.get(row);
        QAStepTemplate template = (QAStepTemplate) editableRow.source();
        if (template != null && (col == 0 || col == 1)) {
            return false;
        }
        
        return true;
    }

    public boolean isEditable(int col) {
        return true;
    }
    
    private List createRows(QAStepTemplate[] templates) {
        List rows = new ArrayList();
        for (int i = 0; i < templates.length; i++)
            rows.add(row(templates[i]));

        return rows;
    }

    private EditableRow row(QAStepTemplate template) {
        RowSource source = new EditableQAStepTemplateRowSource(template);
        return new EditableRow(source);
    }

    public Class getColumnClass(int col) {
        if (col == 0 || col == 4)
            return Boolean.class;

        return String.class;
    }
    
    private QAStepTemplate[] getSelected() {
        List selected = new ArrayList();

        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            EditableRow row = (EditableRow) iter.next();
            EditableQAStepTemplateRowSource rowSource = (EditableQAStepTemplateRowSource) row.rowSource();
            if (rowSource.isSelected())
                selected.add(rowSource.source());
        }

        return (QAStepTemplate[]) selected.toArray(new QAStepTemplate[0]);
    }
    
    public QAStepTemplate[] sources() throws EmfException {
        List sources = sourcesList();
        return (QAStepTemplate[]) sources.toArray(new QAStepTemplate[0]);
    }

    private List sourcesList() throws EmfException {
        List sources = new ArrayList();
        int rowNumber = 0;
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            rowNumber++;
            EditableRow row = (EditableRow) iter.next();
            EditableQAStepTemplateRowSource rowSource = (EditableQAStepTemplateRowSource) row.rowSource();
            rowSource.validate(rowNumber);
            sources.add(rowSource.source());
        }
        return sources;
    }

}

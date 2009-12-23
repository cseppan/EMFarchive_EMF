package gov.epa.emissions.framework.ui;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.util.ArrayList;
import java.util.List;

public class EmfDatasetTableData extends AbstractTableData {

    private List rows;
    private EmfSession session;

    public EmfDatasetTableData(EmfDataset[] datasets, EmfSession session) {
        this.session = session;
        this.rows = createRows(datasets);
    }

    public String[] columns() {
        return new String[] { "Name", "Last Modified Date", "Type", "Status", "Creator", "Intended Use", "Project",
                "Region", "Start Date", "End Date", "Temporal Resolution" };
    }

    public List rows() {
        return this.rows;
    }

    private List createRows(EmfDataset[] datasets) {
        List rows = new ArrayList();

        for (int i = 0; i < datasets.length; i++) {
            EmfDataset dataset = datasets[i];
            Object[] values = { dataset.getName(), format(dataset.getModifiedDateTime()), dataset.getDatasetTypeName(),
                    dataset.getStatus(), getCreatorFullName(dataset), dataset.getIntendedUse(), dataset.getProject(),
                    dataset.getRegion(), format(dataset.getStartDateTime()), format(dataset.getStopDateTime()), dataset.getTemporalResolution() };

            Row row = new ViewableRow(dataset, values);
            rows.add(row);
        }

        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

    public Class getColumnClass(int col) {
        if (col < 0 || col > 10) {
            throw new IllegalArgumentException("Allowed values are between 0 and 8, but the value is " + col);
        }
        return String.class;
    }
    
    String getCreatorFullName(EmfDataset dataset){
        String fullName = "";
        try {
            fullName = session.getUserFullName(dataset.getCreator());
            if (fullName ==null)
                fullName = dataset.getCreator();
        } catch (EmfException e) {
            e.printStackTrace();
        }
        return fullName;
    }

}

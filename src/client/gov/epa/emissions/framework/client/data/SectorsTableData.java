package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.ViewableRow;
import gov.epa.emissions.framework.ui.Row;

import java.util.ArrayList;
import java.util.List;

public class SectorsTableData extends AbstractTableData {
	private List rows;

	public SectorsTableData(Sector[] sectors) {
		this.rows = createRows(sectors);
	}

	public String[] columns() {
		return new String[] { "Name", "Description" };
	}

	public List rows() {
		return rows;
	}

	public boolean isEditable(int col) {
		return true;
	}

	private List createRows(Sector[] sectors) {
		List rows = new ArrayList();

		for (int i = 0; i < sectors.length; i++) {
			Sector element = sectors[i];
			Object[] values = { element.getName(), element.getDescription() };

			Row row = new ViewableRow(element, values);
			rows.add(row);
		}

		return rows;
	}

}

package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.framework.ui.Row;

import java.util.List;

import junit.framework.TestCase;

public class SectorsTableDataTest extends TestCase {

	private SectorsTableData data;

	protected void setUp() {
		Sector sector1 = new Sector();
		sector1.setName("name1");
		sector1.setDescription("desc1");

		Sector sector2 = new Sector();
		sector2.setName("name2");
		sector2.setDescription("desc2");

		data = new SectorsTableData(new Sector[] { sector1, sector2 });
	}

	public void testShouldHaveFiveColumns() {
		String[] columns = data.columns();
		assertEquals(2, columns.length);
		assertEquals("Name", columns[0]);
		assertEquals("Description", columns[1]);
	}

	public void testAllColumnsShouldBeEditable() {
		assertTrue("All cells should be uneditable", data.isEditable(0));
		assertTrue("All cells should be uneditable", data.isEditable(1));
	}

	public void testShouldReturnTheRowsCorrespondingToSectorsCount() {
		List rows = data.rows();
		assertNotNull("Should have 2 rows", rows);
		assertEquals(2, rows.size());
	}

	public void testShouldFillTheColumnsCorrectly() {
		List rows = data.rows();

		Row row = (Row) rows.get(0);
		assertEquals("name1", row.getValueAt(0));
		assertEquals("desc1", row.getValueAt(1));
	}

	public void testShouldReturnARowRepresentingASectorEntry() {
		Sector sector1 = new Sector();
		sector1.setName("name1");
		sector1.setDescription("desc1");

		Sector sector2 = new Sector();
		sector2.setName("name2");
		sector2.setDescription("desc2");

		data = new SectorsTableData(new Sector[] { sector1, sector2 });

		assertEquals(sector1, data.element(0));
		assertEquals(sector2, data.element(1));
	}
}

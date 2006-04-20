package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseCategory;
import gov.epa.emissions.framework.services.casemanagement.EmissionsYear;
import gov.epa.emissions.framework.services.casemanagement.MeteorlogicalYear;
import gov.epa.emissions.framework.ui.Row;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

public class CasesTableDataTest extends TestCase {

    private CasesTableData data;

    private Case case1;

    private Case case2;

    protected void setUp() {
        case1 = new Case();
        case1.setName("name1");
        case1.setCaseCategory(new CaseCategory("category1"));
        case1.setRegion(new Region("region1"));
        case1.setEmissionsYear(new EmissionsYear("2003"));
        case1.setMeteorlogicalYear(new MeteorlogicalYear("2002"));
        case1.setLastModifiedDate(new Date());
        case1.setRunStatus("started");

        case2 = new Case();
        case2.setName("name2");
        case2.setCaseCategory(new CaseCategory("category2"));
        case2.setRegion(new Region("region2"));
        case2.setEmissionsYear(new EmissionsYear("2003"));
        case2.setMeteorlogicalYear(new MeteorlogicalYear("2002"));
        case2.setLastModifiedDate(new Date());
        case2.setRunStatus("started");

        data = new CasesTableData(new Case[] { case1, case2 });
    }

    public void testShouldHaveTwoColumns() {
        String[] columns = data.columns();
        assertEquals(7, columns.length);
        assertEquals("Name", columns[0]);
        assertEquals("Category", columns[1]);
        assertEquals("Region", columns[2]);
        assertEquals("Emissions Year", columns[3]);
        assertEquals("Meteorlogical Year", columns[4]);
        assertEquals("Last Modified", columns[5]);
        assertEquals("Run Status", columns[6]);
    }

    public void testShouldHaveAppropriateColumnClassDefinedForAllColumns() {
        assertEquals(String.class, data.getColumnClass(0));
        assertEquals(String.class, data.getColumnClass(1));
        assertEquals(String.class, data.getColumnClass(2));
        assertEquals(Integer.class, data.getColumnClass(3));
        assertEquals(Integer.class, data.getColumnClass(4));
        assertEquals(Date.class, data.getColumnClass(5));
        assertEquals(String.class, data.getColumnClass(6));
    }

    public void testAllColumnsShouldBeEditable() {
        assertTrue("All cells should be uneditable", data.isEditable(0));
        assertTrue("All cells should be uneditable", data.isEditable(1));
        assertTrue("All cells should be uneditable", data.isEditable(2));
        assertTrue("All cells should be uneditable", data.isEditable(3));
        assertTrue("All cells should be uneditable", data.isEditable(4));
        assertTrue("All cells should be uneditable", data.isEditable(5));
        assertTrue("All cells should be uneditable", data.isEditable(6));
    }

    public void testShouldReturnTheRowsCorrespondingToCount() {
        List rows = data.rows();
        assertNotNull("Should have 2 rows", rows);
        assertEquals(2, rows.size());
    }

    public void testShouldFillTheColumnsCorrectly() {
        List rows = data.rows();

        Row row = (Row) rows.get(0);
        assertEquals("name1", row.getValueAt(0));
        assertEquals("category1", row.getValueAt(1));
        assertEquals("region1", row.getValueAt(2));
        assertEquals(new Integer(2003), row.getValueAt(3));
        assertEquals(new Integer(2002), row.getValueAt(4));
        assertEquals(format(case1.getLastModifiedDate()), row.getValueAt(5));
        assertEquals(case1.getRunStatus(), row.getValueAt(6));
    }

    private String format(Date date) {
        return new SimpleDateFormat("MM/dd/yyyy HH:mm").format(date);
    }

    public void testShouldReturnARowRepresentingACaseEntry() {
        assertEquals(case1, data.element(0));
        assertEquals(case2, data.element(1));
    }
}

package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.casemanagement.Abbreviation;
import gov.epa.emissions.framework.services.casemanagement.AirQualityModel;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseCategory;
import gov.epa.emissions.framework.services.casemanagement.EmissionsYear;
import gov.epa.emissions.framework.services.casemanagement.MeteorlogicalYear;
import gov.epa.emissions.framework.services.casemanagement.Speciation;
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
        case1.setModelingRegion(new Region("region1"));
        case1.setEmissionsYear(new EmissionsYear("2003"));
        case1.setMeteorlogicalYear(new MeteorlogicalYear("2002"));
        case1.setLastModifiedDate(new Date());
        case1.setRunStatus("started");
        case1.setProject(new Project("projec1"));
        User user1 = new User();
        user1.setName("user1");
        case1.setLastModifiedBy(user1);
        case1.setAbbreviation(new Abbreviation("abb1"));
        case1.setAirQualityModel(new AirQualityModel("aqm1"));
        case1.setSpeciation(new Speciation("sp1"));

        case2 = new Case();
        case2.setName("name2");
        case2.setCaseCategory(new CaseCategory("category2"));
        case2.setModelingRegion(new Region("region2"));
        case2.setEmissionsYear(new EmissionsYear("2003"));
        case2.setMeteorlogicalYear(new MeteorlogicalYear("2002"));
        case2.setLastModifiedDate(new Date());
        case2.setRunStatus("started");
        case2.setProject(new Project("projec1"));
        User user2 = new User();
        user2.setName("user2");
        case2.setLastModifiedBy(user2);
        case2.setAbbreviation(new Abbreviation("abb2"));
        case2.setAirQualityModel(new AirQualityModel("aqm2"));
        case2.setSpeciation(new Speciation("sp2"));

        data = new CasesTableData(new Case[] { case1, case2 });
    }

    public void testShouldHaveTwelveColumns() {
        String[] columns = data.columns();
        assertEquals(12, columns.length);
        assertEquals("Name", columns[0]);
        assertEquals("Project", columns[1]);
        assertEquals("Modeling Regn.", columns[2]);
        assertEquals("Creator", columns[3]);
        assertEquals("Category", columns[4]);
        assertEquals("Run Status", columns[5]);
        assertEquals("Abbrev.", columns[6]);
        assertEquals("AQM", columns[7]);
        assertEquals("Base Year", columns[8]);
        assertEquals("Met. Year", columns[9]);
        assertEquals("Speciation", columns[10]);
        assertEquals("Last Modified Date", columns[11]);
    }

    public void testShouldHaveAppropriateColumnClassDefinedForAllColumns() {
        for (int i = 0; i < 11; i++)
            assertEquals(String.class, data.getColumnClass(1));
        assertEquals(Date.class, data.getColumnClass(11));
    }

    public void testAllColumnsShouldBeEditable() {
        for (int i = 0; i < 12; i++)
            assertTrue("All cells should be uneditable", data.isEditable(i));
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
        assertEquals("projec1", row.getValueAt(1));
        assertEquals("region1", row.getValueAt(2));
        assertEquals("user1", row.getValueAt(3));
        assertEquals("category1", row.getValueAt(4));
        assertEquals("started", row.getValueAt(5));
        assertEquals("abb1", row.getValueAt(6));
        assertEquals("aqm1", row.getValueAt(7));
        assertEquals("2003", row.getValueAt(8));
        assertEquals("2002", row.getValueAt(9));
        assertEquals("sp1", row.getValueAt(10));
        assertEquals(format(case1.getLastModifiedDate()), row.getValueAt(11));
    }

    private String format(Date date) {
        return new SimpleDateFormat("MM/dd/yyyy HH:mm").format(date);
    }

    public void testShouldReturnARowRepresentingACaseEntry() {
        assertEquals(case1, data.element(0));
        assertEquals(case2, data.element(1));
    }
}

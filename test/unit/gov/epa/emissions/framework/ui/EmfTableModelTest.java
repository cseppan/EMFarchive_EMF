package gov.epa.emissions.framework.ui;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.services.EmfDataset;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jmock.MockObjectTestCase;

public class EmfTableModelTest extends MockObjectTestCase {

    private EmfTableModel model;

    private EmfDataset dataset1;

    private EmfTableData tableData;

    private EmfDataset dataset2;

    protected void setUp() {
        List datasetList = new ArrayList();

        dataset1 = new EmfDataset();
        dataset1.setName("name1");
        dataset1.setDatasetType(new DatasetType("name1"));
        dataset1.setStatus("whatever-status");
        dataset1.setCreator("creator1");
        dataset1.setRegion("region1");
        dataset1.setStartDateTime(new Date());
        dataset1.setStopDateTime(new Date());
        datasetList.add(dataset1);

        dataset2 = new EmfDataset();
        dataset2.setName("name1");
        dataset2.setDatasetType(new DatasetType("name2"));
        dataset2.setStatus("whatever-status");
        dataset2.setCreator("creator1");
        dataset2.setRegion("region1");
        dataset2.setStartDateTime(new Date());
        dataset2.setStopDateTime(new Date());
        datasetList.add(dataset2);

        tableData = new EmfDatasetTableData(new EmfDataset[] { dataset1, dataset2 });

        model = new EmfTableModel(tableData);
    }

    public void testShouldReturnColumnsNames() {
        assertEquals(7, model.getColumnCount());

        assertEquals("Name", model.getColumnName(0));
        assertEquals("Type", model.getColumnName(1));
        assertEquals("Status", model.getColumnName(2));
        assertEquals("Creator", model.getColumnName(3));
        assertEquals("Region", model.getColumnName(4));
        assertEquals("Start Date", model.getColumnName(5));
        assertEquals("End Date", model.getColumnName(6));
    }

    public void testShouldReturnRowsEqualingNumberOfDatasets() {
        assertEquals(2, model.getRowCount());
    }

    public void testShouldReturnUserAttributesAtSpecifiedIndex() {
        assertEquals(dataset1.getName(), model.getValueAt(0, 0));
        assertEquals(dataset1.getDatasetTypeName(), model.getValueAt(0, 1));
        assertEquals(dataset1.getStatus(), model.getValueAt(0, 2));
        assertEquals(dataset1.getCreator(), model.getValueAt(0, 3));
        assertEquals(dataset1.getRegion(), model.getValueAt(0, 4));
        assertEquals(dataset1.getStartDateTime(), model.getValueAt(0, 5));
        assertEquals(dataset1.getStopDateTime(), model.getValueAt(0, 6));
    }

    public void testShouldMarkEmailColumnAsEditable() {
        assertFalse("All column should be uneditable", model.isCellEditable(0, 0));
        assertFalse("All column should be uneditable", model.isCellEditable(0, 1));
        assertFalse("All column should be uneditable", model.isCellEditable(0, 2));
        assertFalse("All column should be uneditable", model.isCellEditable(0, 3));
        assertFalse("All column should be uneditable", model.isCellEditable(0, 4));
    }

    public void testShouldReturnDatasetBasedOnIndex() {
        assertEquals(dataset1, model.element(0));
        assertEquals(dataset2, model.element(1));
    }
    
}

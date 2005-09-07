package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.framework.EmfException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jmock.MockObjectTestCase;

public class DatasetsBrowserTableModelTest extends MockObjectTestCase {

    private DatasetsBrowserTableModel model;

    private EmfDataset dataset1;

    private EmfDataset dataset2;

    protected void setUp() throws EmfException {
        List datasetList = new ArrayList();

        dataset1 = new EmfDataset();
        dataset1.setName("name1");
        dataset1.setCreator("creator1");
        dataset1.setRegion("region1");
        dataset1.setStartDateTime(new Date());
        dataset1.setStopDateTime(new Date());
        datasetList.add(dataset1);

        dataset2 = new EmfDataset();
        dataset2.setName("name2");
        dataset2.setCreator("creator2");
        dataset2.setRegion("region2");
        dataset2.setStartDateTime(new Date());
        dataset2.setStopDateTime(new Date());
        datasetList.add(dataset2);

        EmfDataset[] datasets = (EmfDataset[]) datasetList.toArray(new EmfDataset[0]);

        model = new DatasetsBrowserTableModel(datasets);
    }

    public void testShouldReturnColumnsNames() {
        assertEquals(5, model.getColumnCount());

        assertEquals("Name", model.getColumnName(0));
        assertEquals("Start Date", model.getColumnName(1));
        assertEquals("End Date", model.getColumnName(2));
        assertEquals("Region", model.getColumnName(3));
        assertEquals("Creator", model.getColumnName(4));
    }

    public void testShouldReturnRowsEqualingNumberOfDatasets() throws EmfException {
        assertEquals(2, model.getRowCount());
    }

    public void testShouldReturnUserAttributesAtSpecifiedIndex() {
        assertEquals(dataset1.getName(), model.getValueAt(0, 0));
        assertEquals(dataset1.getStartDateTime(), model.getValueAt(0, 1));
        assertEquals(dataset1.getStopDateTime(), model.getValueAt(0, 2));
        assertEquals(dataset1.getRegion(), model.getValueAt(0, 3));
        assertEquals(dataset1.getCreator(), model.getValueAt(0, 4));
    }

    public void testShouldMarkEmailColumnAsEditable() {
        assertFalse("All column should be uneditable", model.isCellEditable(0, 0));
        assertFalse("All column should be uneditable", model.isCellEditable(0, 1));
        assertFalse("All column should be uneditable", model.isCellEditable(0, 2));
        assertFalse("All column should be uneditable", model.isCellEditable(0, 3));
        assertFalse("All column should be uneditable", model.isCellEditable(0, 4));
    }

    public void testShouldReturnDatasetBasedOnIndex() {
        assertEquals(dataset1, model.getDataset(0));
        assertEquals(dataset2, model.getDataset(1));
    }
}

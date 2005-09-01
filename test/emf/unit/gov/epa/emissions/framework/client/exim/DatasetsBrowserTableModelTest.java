package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.io.Dataset;
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
        List datasets = new ArrayList();

        dataset1 = new EmfDataset();
        dataset1.setName("name1");
        dataset1.setCreator("creator1");
        dataset1.setRegion("region1");
        dataset1.setStartDateTime(new Date());
        dataset1.setStopDateTime(new Date());
        datasets.add(dataset1);

        dataset2 = new EmfDataset();
        datasets.add(dataset2);
        
        model = new DatasetsBrowserTableModel((Dataset[]) datasets.toArray(new Dataset[0]));
    }

    public void testShouldReturnColumnsNames() {
        assertEquals(4, model.getColumnCount());

        assertEquals("Name", model.getColumnName(0));
        assertEquals("Time Period", model.getColumnName(1));
        assertEquals("Region", model.getColumnName(2));
        assertEquals("Creator", model.getColumnName(3));
    }

    public void testShouldReturnRowsEqualingNumberOfDatasets() throws EmfException{       
        assertEquals(2, model.getRowCount());
    }

    public void testShouldReturnUserAttributesAtSpecifiedIndex() {
        assertEquals(dataset1.getName(), model.getValueAt(0, 0));
        assertEquals(dataset1.getStartDateTime() + "-" + dataset1.getStopDateTime(), model.getValueAt(0, 1));
        assertEquals(dataset1.getRegion(), model.getValueAt(0, 2));
        assertEquals(dataset1.getCreator(), model.getValueAt(0, 3));
    }
}

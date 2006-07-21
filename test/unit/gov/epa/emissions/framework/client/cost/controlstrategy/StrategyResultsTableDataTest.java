package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.framework.client.cost.controlstrategy.editor.StrategyResultsTableData;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.Row;

import java.util.List;

import junit.framework.TestCase;

public class StrategyResultsTableDataTest extends TestCase {

    private StrategyResultsTableData data;

    private StrategyResult result1;

    private StrategyResult result2;

    protected void setUp() {
        result1 = new StrategyResult();
        result1.setInputDatasetId(101);
        EmfDataset detailDataset1 = detailDataset("detailed dataset1");
        result1.setDetailedResultDataset(detailDataset1);
        result1.setStrategyResultType(type());

        result2 = new StrategyResult();
        result2.setInputDatasetId(102);
        EmfDataset detailDataset2 = detailDataset("detailed dataset2");
        result2.setDetailedResultDataset(detailDataset2);
        result2.setStrategyResultType(type());

        EmfDataset dataset1 = dataset("input dataset1");
        dataset1.setId(101);
        EmfDataset dataset2 = dataset("input dataset2");
        dataset2.setId(102);
        data = new StrategyResultsTableData(new EmfDataset[] { dataset1, dataset2 }, new StrategyResult[] { result1,
                result2 });
    }

    private EmfDataset detailDataset(String name) {
        EmfDataset dataset = dataset(name);
        dataset.setStatus("Created By Control Strategy");
        return dataset;

    }

    private EmfDataset dataset(String name) {
        EmfDataset dataset = new EmfDataset();
        dataset.setName(name);
        return dataset;
    }

    private StrategyResultType type() {
        StrategyResultType type = new StrategyResultType();
        type.setName("Detailed Strategy Result");

        return type;
    }

    public void testTableDataShouldHaveThreeColumns() {
        String[] columns = data.columns();
        String[] expectedCols = { "Input Dataset", "Output Dataset", "Product" };
        assertEquals(3, columns.length);
        assertEquals(expectedCols[0], columns[0]);
        assertEquals(expectedCols[1], columns[1]);
        assertEquals(expectedCols[2], columns[2]);
    }

    public void testShouldHaveAppropriateColumnClassDefinedForAllColumns() {
        for (int i = 0; i < 3; i++)
            assertEquals(String.class, data.getColumnClass(1));
    }

    public void testAllColumnsShouldNotBeEditable() {
        for (int i = 0; i < 3; i++)
            assertFalse("All cells should be uneditable", data.isEditable(i));
    }

    public void testShouldReturnTheRowsCorrespondingToCount() {
        List rows = data.rows();
        assertNotNull("Should have 2 rows", rows);
        assertEquals(2, rows.size());
    }

    public void testShouldFillTheColumnsCorrectly() {
        List rows = data.rows();

        Row row1 = (Row) rows.get(0);
        assertEquals("input dataset1", row1.getValueAt(0));
        assertEquals("detailed dataset1", row1.getValueAt(1));
        assertEquals("Detailed Strategy Result", row1.getValueAt(2));

        Row row2 = (Row) rows.get(1);
        assertEquals("input dataset2", row2.getValueAt(0));
        assertEquals("detailed dataset2", row2.getValueAt(1));
        assertEquals("Detailed Strategy Result", row2.getValueAt(2));

    }

    public void testShouldReturnARowRepresentingACaseEntry() {
        assertEquals(result1, data.element(0));
        assertEquals(result2, data.element(1));
    }
}

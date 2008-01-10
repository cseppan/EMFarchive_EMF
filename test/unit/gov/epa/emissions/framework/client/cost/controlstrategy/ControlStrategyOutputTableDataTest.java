package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.framework.client.cost.controlstrategy.editor.ControlStrategyOutputTableData;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.Row;

import java.util.List;

import junit.framework.TestCase;

public class ControlStrategyOutputTableDataTest extends TestCase {

    private ControlStrategyOutputTableData data;

    private ControlStrategyResult result;

    protected void setUp() {
        result = new ControlStrategyResult();
        EmfDataset detailDataset1 = detailDataset("detailed dataset1");
        result.setDetailedResultDataset(detailDataset1);
        result.setStrategyResultType(type());

        EmfDataset dataset1 = dataset("input dataset1");
        dataset1.setId(101);
        result.setInputDataset(dataset1);
        EmfDataset dataset2 = dataset("input dataset2");
        dataset2.setId(102);
        data = new ControlStrategyOutputTableData(new ControlStrategyResult[] { result });
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
        assertNotNull("Should have 1 rows", rows);
        assertEquals(1, rows.size());
    }

    public void testShouldFillTheColumnsCorrectly() {
        List rows = data.rows();

        Row row1 = (Row) rows.get(0);
        assertEquals("input dataset1", row1.getValueAt(0));
        assertEquals("detailed dataset1", row1.getValueAt(1));
        assertEquals("Detailed Strategy Result", row1.getValueAt(2));

    }

    public void testShouldReturnARowRepresentingACaseEntry() {
        assertEquals(result.getDetailedResultDataset(), data.element(0));
    }
}

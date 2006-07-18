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
        result1.setDatasetId(101);
        result1.setDetailedResultDataset(detailDataset("dataset1"));
        result1.setStrategyResultType(type());
        
        result2 = new StrategyResult();
        result2.setDatasetId(102);
        result2.setDetailedResultDataset(detailDataset("dataset2"));
        result2.setStrategyResultType(type());
        
        data = new StrategyResultsTableData(new StrategyResult[] { result1, result2 });
    }

    private EmfDataset detailDataset(String name) {
        
        EmfDataset dataset = new EmfDataset();
        dataset.setStatus("Created By Control Strategy");
        dataset.setName(name);
        
        return dataset;
    }

    private StrategyResultType type() {
        StrategyResultType type = new StrategyResultType();
        type.setName("Detailed Strategy Result");
        
        return type;
    }

    public void testTableDataShouldHaveFourColumns() {
        String[] columns = data.columns();
        String[] expectedCols = { "Input Dataset Id", "Output Dataset", "Status", "Product" };
        assertEquals(4, columns.length);
        assertEquals(expectedCols[0], columns[0]);
        assertEquals(expectedCols[1], columns[1]);
        assertEquals(expectedCols[2], columns[2]);
        assertEquals(expectedCols[3], columns[3]);
    }

    public void testShouldHaveAppropriateColumnClassDefinedForAllColumns() {
        for (int i = 0; i < 4; i++)
            assertEquals(String.class, data.getColumnClass(1));
    }

    public void testAllColumnsShouldNotBeEditable() {
        for (int i = 0; i < 4; i++)
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
        assertEquals(""+101, row1.getValueAt(0));
        assertEquals("dataset1", row1.getValueAt(1));
        assertEquals("Created By Control Strategy", row1.getValueAt(2));
        assertEquals("Detailed Strategy Result",row1.getValueAt(3));
        
        Row row2 = (Row) rows.get(1);
        assertEquals(""+102, row2.getValueAt(0));
        assertEquals("dataset2", row2.getValueAt(1));
        assertEquals("Created By Control Strategy", row2.getValueAt(2));
        assertEquals("Detailed Strategy Result",row2.getValueAt(3));
        
    }


    public void testShouldReturnARowRepresentingACaseEntry() {
        assertEquals(result1, data.element(0));
        assertEquals(result2, data.element(1));
    }
}

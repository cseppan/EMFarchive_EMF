package gov.epa.emissions.gui;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class SortFilterSelectModelTest extends MockObjectTestCase {

    private Mock delegate;
    private SortFilterSelectModel model;

    protected void setUp() {
        delegate = mock(SimpleTableModel.class);
        model = new SortFilterSelectModel((SimpleTableModel) delegate.proxy());               
    }
    
    public void testShouldReturnHashAsFirstColumn() {
        assertEquals("#", model.getColumnName(0));        
    }
    
    public void testShouldReturnSelectAsSecondColumn() {
        assertEquals("Select", model.getColumnName(1));
    }
    
    public void testShouldReturnFirstColumnOfDelegateAsSecondColumn() {
        delegate.stubs().method("getColumnName").with(eq(0)).will(returnValue("Name"));
        
        assertEquals("Name", model.getColumnName(2));
    }
    
    public void testShouldReturnTwoPlusDelegateColumnCountAsColumnCount() {
        delegate.stubs().method("getColumnCount").withNoArguments().will(returnValue(3));
        
        assertEquals(5, model.getColumnCount());
    }
    
}

package gov.epa.emissions.framework.client.cost.controlmeasure;

import junit.framework.TestCase;

public class SccFileReaderTest extends TestCase {
    
    public void testShouldCreateFiveSccFromReadingSCCFile() throws Exception{
        String fileName = "test/data/ref/scc.txt";
        Sccs sccs = new Sccs();
        SccFileReader reader = new SccFileReader(fileName, sccs);
        reader.read();
        assertEquals(5,sccs.size());
        assertEquals("10100101",sccs.get(0).code());
        assertEquals("External Combustion Boilers;Electric Generation;Anthracite Coal;Pulverized Coal",sccs.get(0).description());
        assertEquals("10100203",sccs.get(4).code());
        assertEquals("",sccs.get(4).description());
    }

}

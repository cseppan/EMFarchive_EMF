package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.commons.io.Keyword;

import org.jmock.cglib.MockObjectTestCase;

public class KeyValueRowSourceTest extends MockObjectTestCase {

    public void testShouldSetNewKeywordOnKeyValueIfKeywordDoesNotExist() {
        Keyword[] keywords = new Keyword[0];
        KeyVal keyval = new KeyVal();
        KeyValueRowSource source = new KeyValueRowSource(keyval, keywords);

        source.setValueAt(1, "new-key");

        KeyVal result = (KeyVal) source.source();
        assertEquals("new-key", result.getKeyword().getName());
    }

    public void testShouldSetExistingKeywordOnKeyValueIfKeywordDoesExist() {
        Keyword[] keywords = { new Keyword("1"), new Keyword("2") };
        KeyVal keyval = new KeyVal();
        KeyValueRowSource source = new KeyValueRowSource(keyval, keywords);

        source.setValueAt(1, "1");

        KeyVal result = (KeyVal) source.source();
        assertSame(keywords[0], result.getKeyword());
    }

    public void testShouldReturnKeywordNameAsColumnTwo() {
        KeyVal keyval = new KeyVal();
        Keyword keyword = new Keyword("1");
        keyval.setKeyword(keyword);
        KeyValueRowSource source = new KeyValueRowSource(keyval, null);

        assertSame(keyword.getName(), source.values()[1]);
    }
}

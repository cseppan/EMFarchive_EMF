package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.client.EmfConsole;
import gov.epa.emissions.framework.client.UserAcceptanceTestCase;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JComboBox;

import abbot.finder.Matcher;

public class ImportTest extends UserAcceptanceTestCase {

    private ImportWindow importWindow;

    protected void setUp() throws Exception {
        EmfConsole window = gotoConsole();

        click(window, "file");
        click(window, "import");

        importWindow = (ImportWindow) findInternalFrame(window, "import");
        assertNotNull(importWindow);
    }

    public void testShouldShowFourORLDatasetTypesAsOptions() throws Exception {
        JComboBox comboBox = findComboBox(importWindow, "datasetTypes");

        assertNotNull(comboBox);
        assertEquals(4, comboBox.getModel().getSize());
    }

    private JComboBox findComboBox(Container container, final String name) throws Exception {
        return (JComboBox) getFinder().find(container, new Matcher() {
            public boolean matches(Component component) {
                if (!(component instanceof JComboBox))
                    return false;

                JComboBox comboBox = (JComboBox) component;
                return name.equals(comboBox.getName());
            }
        });
    }

}

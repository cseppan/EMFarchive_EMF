package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.framework.client.admin.EMFUserAdminStub;

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;

import junit.extensions.abbot.ComponentTestFixture;
import abbot.finder.matchers.NameMatcher;
import abbot.tester.ComponentTester;

public class LoginTest extends ComponentTestFixture {
    
    private boolean isWindowClosed = false;
    public void testShouldCloseOnClickCancel() throws Exception {
        LoginWindow window = new LoginWindow(new EMFUserAdminStub(Collections.EMPTY_LIST));        
        showWindow(window);
        
        window.addWindowListener(new WindowAdapter(){
            public void windowClosed(WindowEvent arg0) {
                isWindowClosed  = true;
            }});
                
        Component cancel = getFinder().find(window, new NameMatcher("Cancel"));
        
        ComponentTester tester = new ComponentTester();        
        tester.actionClick(cancel);
        
        assertTrue(isWindowClosed);
    }
}

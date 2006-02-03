package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.framework.client.ManagedView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class WindowMenu extends JMenu implements WindowMenuView {

    private List menuItems;
    
    public WindowMenu() {
        super("Window");
        super.setName("window");

        menuItems = new ArrayList();
    }

    public void register(ManagedView view) {
        JMenuItem menuItem = new WindowMenuItem(view);
        menuItems.add(menuItem);

        super.add(menuItem);
        refreshLayout();
    }

    private void refreshLayout() {
        super.validate();
    }

    public void unregister(ManagedView view) {
        super.remove(getMenuItem(view));
    }

    private JMenuItem getMenuItem(ManagedView view) {
        for (Iterator iter = menuItems.iterator(); iter.hasNext();) {
            WindowMenuItem element = (WindowMenuItem) iter.next();
            if (element.view() == view)
                return element;
        }

        return null;
    }

    public class WindowMenuItem extends JMenuItem {

        final private ManagedView view;

        public WindowMenuItem(ManagedView view) {
            super(view.getTitle());
            super.setName(view.getName());
            this.view = view;

            super.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    WindowMenuItem.this.view.bringToFront();
                }
            });
        }

        private ManagedView view() {
            return view;
        }

    }

}

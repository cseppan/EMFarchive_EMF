package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.ui.IconButton;
import gov.epa.emissions.framework.ui.ImageResources;
import gov.epa.emissions.framework.ui.NumberFormattedTextField;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class VersionedPaginationPanel extends JPanel {

    private JFormattedTextField recordInput;

    private VersionedTablePresenter presenter;

    private JLabel current;

    private MessagePanel messagePanel;

    private JSlider slider;

    public VersionedPaginationPanel(MessagePanel messagePanel) {
        super(new BorderLayout());
        this.messagePanel = messagePanel;
    }

    private void doLayout(int totalRecords) {
        JPanel container = new JPanel();

        current = new JLabel("               ");
        current.setToolTipText("Range of displayed records");
        container.add(new JLabel("Current: "));
        container.add(current);

        JLabel total = new JLabel("Total: " + totalRecords);
        total.setToolTipText("Total Records");
        container.add(total);

        container.add(layoutControls(totalRecords));

        super.add(container, BorderLayout.LINE_END);
    }

    public void init(VersionedTablePresenter presenter) {
        this.presenter = presenter;
        try {
            doLayout(presenter.totalRecords());
        } catch (EmfException e) {
            messagePanel.setError("Could not obtain Total Records. Reason: " + e.getMessage());
        }
    }

    public void updateStatus(Page page) {
        current.setText(page.min() + " - " + page.max());
        slider.setValue(page.min());
    }

    private JPanel layoutControls(int totalRecords) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JToolBar top = new JToolBar();
        top.setFloatable(false);

        ImageResources res = new ImageResources();
        top.add(firstButton(res));
        top.add(prevButton(res));
        top.add(recordInputField(totalRecords));
        top.add(nextButton(res));
        top.add(lastButton(res));

        JPanel bottom = new JPanel();
        bottom.add(slider(totalRecords));

        panel.add(top);
        panel.add(bottom);

        return panel;
    }

    private JFormattedTextField recordInputField(final int max) {
        recordInput = new NumberFormattedTextField(1, max, 7, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (!verifyInput(max))
                    return;
                displayPage(Integer.parseInt(recordInput.getText()));
            }
        });
        recordInput.setInputVerifier(new NumberVerifier(max));
        recordInput.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if ("value".equals(event.getPropertyName())) {
                    Number value = (Number) event.getNewValue();
                    slider.setValue(value.intValue());
                }
            }
        });

        recordInput.setToolTipText("Please input 'record number', and press Enter.");
        return recordInput;
    }

    private boolean verifyInput(final int max) {
        AbstractFormatter formatter = recordInput.getFormatter();
        String val = recordInput.getText();

        try {
            Integer.parseInt(val);
            formatter.stringToValue(val);
            recordInput.commitEdit();

            messagePanel.clear();
            return true;
        } catch (Exception pe) {
            messagePanel.setError("Invalid value: " + val + ". Please use numbers between 1 and " + max);
            recordInput.selectAll();
            return false;
        }
    }

    public class NumberVerifier extends InputVerifier {
        private int max;

        NumberVerifier(int max) {
            this.max = max;
        }

        public boolean verify(JComponent input) {
            return verifyInput(max);
        }
    }

    private JSlider slider(int max) {
        slider = new JSlider(JSlider.HORIZONTAL, 1, max, 1);

        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                int val = source.getValue();
                if (!source.getValueIsAdjusting()) { // done adjusting
                    recordInput.setValue(new Integer(val)); // update value
                    displayPage(val);
                } else { // value is adjusting; just set the text
                    recordInput.setText(String.valueOf(val));
                }
            }
        });

        return slider;
    }

    private void displayPage(int record) {
        try {
            presenter.doDisplayPageWithRecord(record);
        } catch (EmfException e) {
            messagePanel.setError("Could not display Page with record: " + record + ". Reason: " + e.getMessage());
        }
    }

    // FIXME: change messages about 'page' to 'range' ??
    private IconButton lastButton(ImageResources res) {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    presenter.doDisplayLast();
                } catch (EmfException e) {
                    messagePanel.setError("Could not display Last Page. Reason: " + e.getMessage());
                }

            }
        };
        return new IconButton("Last", "Go to Last Page", res.last("Go to Last Page"), action);
    }

    private IconButton nextButton(ImageResources res) {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    presenter.doDisplayNext();
                } catch (EmfException e) {
                    messagePanel.setError("Could not display Next Page. Reason: " + e.getMessage());
                }
            }
        };
        return new IconButton("Next", "Go to Next Page", res.next("Go to Next Page"), action);
    }

    private IconButton prevButton(ImageResources res) {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    presenter.doDisplayPrevious();
                } catch (EmfException e) {
                    messagePanel.setError("Could not display Previous Page. Reason: " + e.getMessage());
                }
            }
        };
        return new IconButton("Prev", "Go to Previous Page", res.prev("Go to Previous Page"), action);
    }

    private IconButton firstButton(ImageResources res) {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    presenter.doDisplayFirst();
                } catch (EmfException e) {
                    messagePanel.setError("Could not display First Page. Reason: " + e.getMessage());
                }
            }
        };
        return new IconButton("First", "Go to First Page", res.first("Go to First Page"), action);
    }

}

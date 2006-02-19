package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.ui.IconButton;
import gov.epa.emissions.framework.ui.ImageResources;
import gov.epa.emissions.framework.ui.MessagePanel;
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

public class PaginationPanel extends JPanel {

    private NumberFormattedTextField recordInput;

    private TablePresenter presenter;

    private JLabel current;

    private MessagePanel messagePanel;

    private JSlider slider;

    private JLabel filteredRecords;

    public PaginationPanel(MessagePanel messagePanel) {
        super(new BorderLayout());
        this.messagePanel = messagePanel;
    }

    private void doLayout(int totalRecordsCount) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        container.add(recordsPanel(totalRecordsCount));
        container.add(layoutControls(totalRecordsCount));

        super.add(container, BorderLayout.LINE_END);
    }

    private JPanel recordsPanel(int totalRecordsCount) {
        JPanel panel = new JPanel();

        JLabel currentName = new JLabel("Current: ");
        panel.add(currentName);
        current = new JLabel("               ");
        current.setToolTipText("Range of displayed records");
        panel.add(current);

        JLabel filtered = new JLabel("Filtered: ");
        panel.add(filtered);
        filteredRecords = new JLabel("" + totalRecordsCount);
        filteredRecords.setToolTipText("Filtered Records");
        panel.add(filteredRecords);

        panel.add(new JLabel("Total: "));
        JLabel totalRecordsLabel = new JLabel("" + totalRecordsCount);
        totalRecordsLabel.setToolTipText("Total Records");
        panel.add(totalRecordsLabel);

        return panel;
    }

    public void init(TablePresenter presenter) {
        this.presenter = presenter;
        try {
            doLayout(presenter.totalRecords());
        } catch (EmfException e) {
            messagePanel.setError("Could not obtain Total Records. Reason: " + e.getMessage());
        }
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
                if (!verifyInput(slider))
                    return;
                int record = Integer.parseInt(recordInput.getText());
                displayPage(record);
            }
        });
        recordInput.setInputVerifier(new NumberVerifier());
        recordInput.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if ("value".equals(event.getPropertyName())) {
                    Number record = (Number) event.getNewValue();
                    displayPage(record.intValue());
                }
            }
        });

        recordInput.setToolTipText("Please input 'record number', and press Enter.");
        return recordInput;
    }

    private JSlider slider(int max) {
        slider = new JSlider(JSlider.HORIZONTAL, 1, max, 1);

        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                int val = source.getValue();
                if (!source.getValueIsAdjusting()) { // done adjusting
                    recordInput.setValue(new Integer(val)); // update value
                } else { // value is adjusting; just set the text
                    recordInput.setText(String.valueOf(val));
                }
            }
        });

        return slider;
    }

    public void updateStatus(Page page) {
        current.setText(page.getMin() + " - " + page.getMax());
        slider.setValue(page.getMin());
    }

    public void updateFilteredRecordsCount(int filtered) {
        filteredRecords.setText("" + filtered);
        slider.setMaximum(filtered);
        recordInput.setRange(1, filtered);
    }

    private boolean verifyInput(JSlider slider) {
        AbstractFormatter formatter = recordInput.getFormatter();
        String val = recordInput.getText();

        try {
            Integer.parseInt(val);
            formatter.stringToValue(val);
            recordInput.commitEdit();

            messagePanel.clear();
            return true;
        } catch (Exception pe) {
            messagePanel.setError("Invalid value: " + val + ". Please use numbers between " + slider.getMinimum()
                    + " and " + slider.getMaximum());
            recordInput.selectAll();
            return false;
        }
    }

    public class NumberVerifier extends InputVerifier {
        public boolean verify(JComponent input) {
            return verifyInput(slider);
        }
    }

    // FIXME: change messages about 'page' to 'range' ??
    private IconButton lastButton(ImageResources res) {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doDisplayLast();
            }
        };
        return new IconButton("Last", "Go to Last Page", res.last("Go to Last Page"), action);
    }

    private IconButton nextButton(ImageResources res) {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doDisplayNext();
            }
        };
        return new IconButton("Next", "Go to Next Page", res.next("Go to Next Page"), action);
    }

    private IconButton prevButton(ImageResources res) {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doDisplayPrevious();
            }

        };
        return new IconButton("Prev", "Go to Previous Page", res.prev("Go to Previous Page"), action);
    }

    private IconButton firstButton(ImageResources res) {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doDisplayFirst();
            }
        };
        return new IconButton("First", "Go to First Page", res.first("Go to First Page"), action);
    }

    private void doDisplayPrevious() {
        clearMessages();
        try {
            presenter.doDisplayPrevious();
        } catch (EmfException e) {
            setErrorMessage("Could not display Previous Page. Reason: " + e.getMessage());
        }
    }

    private void doDisplayNext() {
        clearMessages();
        try {
            presenter.doDisplayNext();
        } catch (EmfException e) {
            setErrorMessage("Could not display Next Page. Reason: " + e.getMessage());
        }
    }

    private void setErrorMessage(String message) {
        messagePanel.setError(message);
    }

    private void clearMessages() {
        messagePanel.clear();
    }

    private void displayPage(int record) {
        clearMessages();
        try {
            presenter.doDisplayPageWithRecord(record);
        } catch (EmfException e) {
            messagePanel.setError("Could not display Page with record: " + record + ". Reason: " + e.getMessage());
        }
    }

    private void doDisplayLast() {
        clearMessages();
        try {
            presenter.doDisplayLast();
        } catch (EmfException e) {
            setErrorMessage("Could not display Last Page. Reason: " + e.getMessage());
        }
    }

    private void doDisplayFirst() {
        clearMessages();
        try {
            presenter.doDisplayFirst();
        } catch (EmfException e) {
            messagePanel.setError("Could not display First Page. Reason: " + e.getMessage());
        }
    }

}

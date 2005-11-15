package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.MessagePanel;
import gov.epa.emissions.framework.services.Page;
import gov.epa.emissions.framework.ui.IconButton;
import gov.epa.emissions.framework.ui.ImageResources;
import gov.epa.emissions.framework.ui.NumberFormattedTextField;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;

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

    private JFormattedTextField recordInput;

    private PageViewPresenter presenter;

    private JLabel current;

    private JLabel total;

    private MessagePanel messagePanel;

    private JSlider slider;

    public PaginationPanel(MessagePanel messagePanel) {
        super(new BorderLayout());

        this.messagePanel = messagePanel;

        JPanel container = new JPanel();

        current = new JLabel("               ");
        current.setToolTipText("Range of displayed records");
        container.add(new JLabel("Current: "));
        container.add(current);

        total = new JLabel("             ");
        total.setToolTipText("Total Records");
        container.add(new JLabel("Total: "));
        container.add(total);

        container.add(layoutControls());

        super.add(container, BorderLayout.LINE_END);
    }

    public void updateStatus(Page page) {
        current.setText(page.min() + " - " + page.max());
    }

    public void observe(PageViewPresenter presenter) {
        this.presenter = presenter;
    }

    private JPanel layoutControls() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JToolBar top = new JToolBar();
        top.setFloatable(false);

        ImageResources res = new ImageResources();
        top.add(firstButton(res));
        top.add(prevButton(res));
        int totalRecords = 20;
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
        recordInput = new NumberFormattedTextField(max, 7, new AbstractAction() {
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

        return recordInput;
    }

    private boolean verifyInput(final int max) {
        AbstractFormatter formatter = recordInput.getFormatter();
        try {
            formatter.stringToValue(recordInput.getText());
            recordInput.commitEdit();
            messagePanel.clear();
            return true;
        } catch (ParseException pe) {
            messagePanel.setError("Invalid value: " + recordInput.getText() + ". Please use numbers between 0 and "
                    + max);
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
        slider = new JSlider(JSlider.HORIZONTAL, 0, max, 0);

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
        // TODO:
    }

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

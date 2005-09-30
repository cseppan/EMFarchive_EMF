package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.client.UserAcceptanceTestCase;
import gov.epa.emissions.framework.client.meta.SummaryTab.FormattedTextField;

import java.text.ParseException;
import java.util.Date;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class SummaryTabActions {

    private SummaryTab tab;

    private UserAcceptanceTestCase test;

    public SummaryTabActions(SummaryTab tab, UserAcceptanceTestCase test) {
        this.tab = tab;
        this.test = test;
    }

    public String name() {
        return textField("name");
    }

    public String description() {
        JTextArea desc = (JTextArea) test.findByName(tab, "description");
        return desc.getText();
    }

    public String project() {
        return selectedComboBoxItem("projects");
    }

    public String datasetType() {
        return label("datasetType");
    }

    public String creator() {
        return label("creator");
    }

    private String label(String name) {
        JLabel creator = (JLabel) test.findByName(tab, name);
        return creator.getText();
    }

    public Date startDateTime() {
        return date("startDateTime");
    }

    public Date endDateTime() {
        return date("endDateTime");
    }

    private Date date(String widgetName) {
        FormattedTextField date = (FormattedTextField) test.findByName(tab, widgetName);
        return parseDate(date.getText(), widgetName);
    }

    private Date parseDate(String date, String widgetName) {
        try {
            return tab.DATE_FORMATTER.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException("could not parse value of widget - " + widgetName + " as Date");
        }
    }

    public String temporalResolution() {
        return selectedComboBoxItem("temporalResolutions");
    }

    public String sector() {
        return selectedComboBoxItem("sectors");
    }

    public String region() {
        return selectedComboBoxItem("regions");
    }

    private String textField(String widgetName) {
        JTextField textField = (JTextField) test.findByName(tab, widgetName);
        return textField.getText();
    }

    public String country() {
        return selectedComboBoxItem("countries");
    }

    private String selectedComboBoxItem(String comboBoxName) {
        JComboBox combo = (JComboBox) test.findByName(tab, comboBoxName);
        return (String) combo.getSelectedItem();
    }

    public String status() {
        return label("status");
    }

    public Date lastModifiedDate() {
        return parseDate(label("lastModifiedDate"), "lastModifiedDate");
    }

    public Date lastAccessedDate() {
        return parseDate(label("lastAccessedDate"), "lastAccessedDate");
    }

    public Date creationDate() {
        return parseDate(label("creationDate"), "creationDate");
    }

}

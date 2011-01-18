package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.commons.CommonDebugLevel;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.Component;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;

public class DateDataCellEditor extends DefaultCellEditor {
    private MessagePanel messagePanel;

    private Class editedClass;

    private Object value;

    private Object oldValue;

    public DateDataCellEditor(MessagePanel messagePanel) {
        super(new JTextField());
        this.messagePanel = messagePanel;
    }

    public Object getCellEditorValue() {
        
        if ( CommonDebugLevel.DEBUG_PAGE) {
            System.out.println( "editor - value class: " + value.getClass());
            System.out.println( "editor - edited class: " + this.editedClass);
        }
        return value;
    }

    public boolean stopCellEditing() {
        Object value = super.getCellEditorValue();
        this.value = validate(value);
        return super.stopCellEditing();

    }

    private Object validate(Object value) {
        Object updatedValue = null;
        updatedValue = validateDateValue(value);

        return updatedValue;
    }

    private Object validateDateValue(Object value) {
        try {
            if (value == null || value.getClass().equals( this.editedClass)) {
                return value;
            } 
            
            if ( CommonDebugLevel.DEBUG_PAGE) {
                System.out.println( "editor - value class: " + value.getClass());
                System.out.println( "editor - edited class: " + this.editedClass);
            }
           
            SimpleDateFormat dateFormat = new SimpleDateFormat();
            dateFormat.applyPattern("yyyy-MM-dd HH:mm:ss");
            
            if ( this.editedClass.equals( java.sql.Date.class)) {
                if ( value instanceof java.util.Date) { 
                    return new java.sql.Date( ((java.util.Date) value).getTime());
                } else if ( value instanceof Calendar) {
                    return new java.sql.Date( ((java.util.Calendar) value).getTime().getTime());
                } else if ( value instanceof String){
                    return new java.sql.Date( (dateFormat.parse( (String) value).getTime()));
                } else {
                    messagePanel.setError("Value of class " + value.getClass() + " is not supported.");
                    return oldValue;
                }
            } else if ( this.editedClass.equals( java.sql.Time.class)) {
                if ( value instanceof java.util.Date) { 
                    return new java.sql.Time( ((java.util.Date) value).getTime());
                } else if ( value instanceof Calendar) {
                    return new java.sql.Time( ((java.util.Calendar) value).getTime().getTime());
                } else if ( value instanceof String){
                    return new java.sql.Time ( (dateFormat.parse( (String) value)).getTime());
                } else {
                    messagePanel.setError("Value of class " + value.getClass() + " is not supported.");
                    return oldValue;
                }
            } else if ( this.editedClass.equals( java.sql.Timestamp.class)) {
                if ( value instanceof java.util.Date) { 
                    return new java.sql.Timestamp( ((java.util.Date) value).getTime());
                } else if ( value instanceof Calendar) {
                    return new java.sql.Timestamp( ((java.util.Calendar) value).getTime().getTime());
                } else if ( value instanceof String){
                    return new java.sql.Timestamp ( (dateFormat.parse( (String) value)).getTime());
                } else {
                    messagePanel.setError("Value of class " + value.getClass() + " is not supported.");
                    return oldValue;
                }
            } else if ( this.editedClass.equals( java.util.Date.class)) {
                if ( value instanceof java.util.Date) { 
                    return new java.util.Date( ((java.util.Date) value).getTime());
                } else if ( value instanceof Calendar) {
                    return new java.util.Date( ((java.util.Calendar) value).getTime().getTime());
                } else if ( value instanceof String){
                    return dateFormat.parse( (String) value);
                } else {
                    messagePanel.setError("Value of class " + value.getClass() + " is not supported.");
                    return oldValue;
                }
            } else if ( this.editedClass.equals( java.util.GregorianCalendar.class) || 
                        this.editedClass.equals( java.util.Calendar.class)) {
                if ( value instanceof java.util.Date) { 
                    java.util.Calendar c = new java.util.GregorianCalendar();
                    c.setTime((java.util.Date) value);
                    return c;
                } else if ( value instanceof Calendar) {
                    java.util.Calendar c = new java.util.GregorianCalendar();
                    c.setTime(((java.util.GregorianCalendar) value).getTime());
                    return c;
                } else if ( value instanceof String){
                    java.util.Calendar c = new java.util.GregorianCalendar();
                    c.setTime( dateFormat.parse( (String) value));
                    return c;
                } else {
                    messagePanel.setError("Value of class " + value.getClass() + " is not supported.");
                    return oldValue;
                }
            } else {
                messagePanel.setError("Edited call " + this.editedClass + " is not supported.");
                return oldValue;
            }
        } catch (Exception e) {
            messagePanel.setError("Please enter a valid date. " + e.getMessage());
            return oldValue;
        }
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        messagePanel.clear();
        this.editedClass = table.getColumnClass(column);
        this.oldValue = value;
        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }

}

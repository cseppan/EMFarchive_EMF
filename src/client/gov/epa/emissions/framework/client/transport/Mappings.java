package gov.epa.emissions.framework.client.transport;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

import org.apache.axis.Constants;
import org.apache.axis.client.Call;

public class Mappings {

    protected Mapper mapper;

    public Mappings() {
        mapper = new Mapper();
    }

    public void array(Call call, Class clazz, String name) {
        array(call, clazz, qname(name));
    }

    public void array(Call call, Class clazz, QName name) {
        mapper.registerArrayMapping(call, clazz, name);
    }

    public void bean(Call call, Class clazz, String name) {
        bean(call, clazz, qname(name));
    }

    public void bean(Call call, Class clazz, QName name) {
        mapper.registerBeanMapping(call, clazz, name);
    }

    public QName qname(String name) {
        return mapper.qname(name);
    }

    public void addParam(Call call, String id, QName name) {
        call.addParameter(id, name, ParameterMode.IN);
    }

    public void addParam(Call call, String id, String name) {
        call.addParameter(id, qname(name), ParameterMode.IN);
    }

    public void addStringParam(Call call, String id) {
        call.addParameter(id, Constants.XSD_STRING, ParameterMode.IN);
    }

    public void addBooleanParameter(Call call, String id) {
        call.addParameter(id, Constants.XSD_BOOLEAN, ParameterMode.IN);
    }

    public void addIntegerParam(Call call, String id) {
        call.addParameter(id, Constants.XSD_INTEGER, ParameterMode.IN);
    }

    public void setOperation(Call call, String id) {
        call.setOperationName(qname(id));
    }

    public void setAnyReturnType(Call call) {
        call.setReturnType(Constants.XSD_ANY);
    }

    public void setBooleanReturnType(Call call) {
        call.setReturnType(Constants.XSD_BOOLEAN);
    }

    public void setVoidReturnType(Call call) {
        call.setReturnType(Constants.XSD_ANY);// FIXME: what's it exactly?
    }

    public void setStringReturnType(Call call) {
        call.setReturnType(Constants.XSD_STRING);
    }

    public QName string() {
        return Constants.XSD_STRING;
    }

    public QName strings() {
        return Constants.XSD_ANY;
    }

    public void setReturnType(Call call, QName name) {
        call.setReturnType(name);
    }

    public void addLongParam(Call call, String id) {
        call.addParameter(id, Constants.XSD_LONG, ParameterMode.IN);
    }

    public void setIntegerReturnType(Call call) {
        call.setReturnType(Constants.XSD_INT);
    }

}

package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.io.Table;

import javax.xml.namespace.QName;

import org.apache.axis.client.Call;
import org.apache.axis.encoding.ser.ArrayDeserializerFactory;
import org.apache.axis.encoding.ser.ArraySerializerFactory;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;

public class Mapper {
    public QName qname(String name) {
        return new QName("urn:gov.epa.services.EmfServices", name);
    }

    public void registerBeanMapping(Call call, Class clazz, QName beanQName) {
        call.registerTypeMapping(clazz, beanQName, new BeanSerializerFactory(clazz, beanQName),
                new BeanDeserializerFactory(clazz, beanQName));

    }

    public void registerArrayMapping(Call call, Class cls, QName qname) {
        call.registerTypeMapping(cls, qname, new ArraySerializerFactory(cls, qname),
                new ArrayDeserializerFactory(qname));
    }

    public void registerMappingForTable(Call call) {
        QName tableQName = qname("ns1:Table");
        registerBeanMapping(call, Table.class, tableQName);
    }
}

package net.veldor.rdc_info.utils;

import net.veldor.rdc_info.App;
import net.veldor.rdc_info.subclasses.Execution;
import net.veldor.rdc_info.subclasses.PriceInfo;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.HashMap;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class XMLHandler {
    private final String mXml;
    private InputSource mDomInputSource;

    public XMLHandler(String xml) {
        mXml = xml;
    }

    public PriceInfo getPriceInfo() {
        // получу список обследований
        XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();
        PriceInfo pi = new PriceInfo();
        try {
            HashMap<String, Execution> executionsList = new HashMap<>();
            NodeList executions = (NodeList) xPath.evaluate("prices/price[@type='execution']", new InputSource(new StringReader(mXml)), XPathConstants.NODESET);
            int length = executions.getLength();
            for (int i = 0; i < length; i++) {
                Element show = (Element)executions.item(i);
                if(show != null){
                    Execution ex = new Execution();
                    ex.id = Integer.parseInt(show.getAttribute(Execution.ATTR_ID));
                    ex.name = show.getAttribute(Execution.ATTR_NAME);
                    ex.summ = show.getAttribute(Execution.ATTR_PRICE);
                    ex.type = Execution.TYPE_SIMPLE;
                    ex.summWithDiscount = ex.summ;
                    ex.price = CashHandler.toRubles(ex.summWithDiscount);
                    pi.executions.add(ex);
                    executionsList.put(ex.name, ex);
                }
            }
            executions = (NodeList) xPath.evaluate("prices/complexes/complex", new InputSource(new StringReader(mXml)), XPathConstants.NODESET);
            length = executions.getLength();
            for (int i = 0; i < length; i++) {
                Element show = (Element)executions.item(i);
                if(show != null){
                    Execution ex = new Execution();
                    ex.id = Integer.parseInt(show.getAttribute(Execution.ATTR_ID));
                    ex.name = show.getAttribute(Execution.ATTR_NAME);
                    ex.summ = show.getAttribute(Execution.ATTR_PRICE);
                    ex.type = Execution.TYPE_COMPLEX;
                    ex.summWithDiscount = ex.summ;
                    ex.price = CashHandler.toRubles(ex.summWithDiscount);
                    pi.executions.add(ex);
                    executionsList.put(ex.name, ex);
                }
            }
            App.getInstance().executionsHandler.allExecutionsList = executionsList;
            return pi;
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return null;
    }

}
package net.veldor.rdc_info.utils;

import android.util.Log;

import net.veldor.rdc_info.App;
import net.veldor.rdc_info.subclasses.Anesthesia;
import net.veldor.rdc_info.subclasses.Contrast;
import net.veldor.rdc_info.subclasses.Execution;
import net.veldor.rdc_info.subclasses.PriceInfo;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
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
            NodeList items = (NodeList) xPath.evaluate("prices/price[@type='execution']", new InputSource(new StringReader(mXml)), XPathConstants.NODESET);
            int length = items.getLength();
            for (int i = 0; i < length; i++) {
                Element show = (Element)items.item(i);
                if(show != null){
                    Execution ex = new Execution();
                    ex.id = Integer.parseInt(show.getAttribute(Execution.ATTR_ID));
                    ex.name = show.getAttribute(Execution.ATTR_NAME);
                    ex.shortName = show.getAttribute(Execution.ATTR_SHORT_NAME);
                    ex.summ = show.getAttribute(Execution.ATTR_PRICE);
                    ex.type = Execution.TYPE_SIMPLE;
                    ex.summWithDiscount = ex.summ;
                    ex.price = CashHandler.toRubles(ex.summWithDiscount);
                    pi.executions.add(ex);
                    executionsList.put(ex.name, ex);
                }
            }
            items = (NodeList) xPath.evaluate("prices/complexes/complex", new InputSource(new StringReader(mXml)), XPathConstants.NODESET);
            length = items.getLength();
            for (int i = 0; i < length; i++) {
                Element show = (Element)items.item(i);
                if(show != null){
                    Execution ex = new Execution();
                    ex.id = Integer.parseInt(show.getAttribute(Execution.ATTR_ID));
                    ex.name = show.getAttribute(Execution.ATTR_NAME);
                    ex.shortName = show.getAttribute(Execution.ATTR_SHORT_NAME);
                    ex.summ = show.getAttribute(Execution.ATTR_PRICE);
                    ex.type = Execution.TYPE_COMPLEX;
                    ex.summWithDiscount = ex.summ;
                    ex.price = CashHandler.toRubles(ex.summWithDiscount);
                    // добавлю отдельные обследования
                    NodeList children = show.getChildNodes();
                    int childLength = children.getLength();
                    int count = 0;
                    for (int j = 0; j < childLength; j++) {
                        Node child = children.item(j);
                        if(child.getNodeName().equals("execution")){
                            NamedNodeMap attrs = child.getAttributes();
                            String name = attrs.getNamedItem("name").getNodeValue();
                            count++;
                            ex.innerExecutions.put(name, executionsList.get(name));
                        }
                    }
                    ex.innerExecutionsLength = count;
                    pi.executions.add(ex);
                    executionsList.put(ex.name, ex);
                }
            }
            App.getInstance().executionsHandler.allExecutionsList = executionsList;
            // получу сведения о контрасте
            items = (NodeList) xPath.evaluate("prices/price[@type='contrast']", new InputSource(new StringReader(mXml)), XPathConstants.NODESET);
            length = items.getLength();
            for (int i = 0; i < length; i++) {
                Element show = (Element)items.item(i);
                if(show != null){
                    Contrast c = new Contrast();
                    c.name = show.getAttribute(Execution.ATTR_NAME);
                    c.summ = show.getAttribute(Execution.ATTR_PRICE);
                    pi.contrasts.add(c);
                }
            }
            // получу сведения о наркозе
            items = (NodeList) xPath.evaluate("prices/price[@type='anesthesia']", new InputSource(new StringReader(mXml)), XPathConstants.NODESET);
            length = items.getLength();
            for (int i = 0; i < length; i++) {
                Element show = (Element)items.item(i);
                if(show != null){
                    Anesthesia c = new Anesthesia();
                    c.name = show.getAttribute(Execution.ATTR_TIME);
                    c.summ = show.getAttribute(Execution.ATTR_PRICE);
                    pi.anesthesia.add(c);
                }
            }
// получу сведения о контрасте
            items = (NodeList) xPath.evaluate("prices/price[@name='МРТ снимок']", new InputSource(new StringReader(mXml)), XPathConstants.NODESET);
            Log.d("surprise", "getPriceInfo: print " + items.getLength());
            pi.printPrice = items.item(0).getAttributes().getNamedItem("price").getNodeValue();
            return pi;
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return null;
    }

}
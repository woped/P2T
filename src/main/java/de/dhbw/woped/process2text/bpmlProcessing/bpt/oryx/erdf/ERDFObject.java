package de.dhbw.woped.process2text.bpmlProcessing.bpt.oryx.erdf;

import de.hpi.bpt.oryx.erdf.IERDFObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Artem Polyvyanyy
 *
 */
public class ERDFObject implements IERDFObject {

	Map<String, String> spans = new HashMap<String,String>();
	
	/*
	 * (non-Javadoc)
	 * @see de.hpi.bpt.oryx.erdf.IERDFObject#getProperty(java.lang.String)
	 */
	public String getProperty(String name) {
		return this.spans.get(name);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.hpi.bpt.oryx.erdf.IERDFObject#setProperty(java.lang.String, java.lang.String)
	 */
	public String setProperty(String name, String value) {
		return this.spans.put(name, value);
	}

	/*
	 * (non-Javadoc)
	 * @see de.hpi.bpt.oryx.erdf.IERDFObject#parseERDF(org.w3c.dom.Node)
	 */
	public void parseERDF(Node node) {
		NodeList nodes = node.getChildNodes();
		for (int i=0; i < nodes.getLength(); i++) {
			Node n = nodes.item(i);
			
			if (n.getNodeName().equals("span"))
				this.spans.put(n.getAttributes().getNamedItem("class").getTextContent(), n.getTextContent());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see de.hpi.bpt.oryx.erdf.IERDFObject#serializeERDF()
	 */
	public Node serializeERDF() {
		// TODO Auto-generated method stub
		return null;
	}

	

}

package de.dhbw.woped.process2text.bpmnProcessing.bpt.oryx.erdf;

import de.dhbw.woped.process2text.bpmnProcessing.bpt.hypergraph.abs.Vertex;
import org.w3c.dom.Node;

/**
 * ERDF node implementation
 * 
 * @author Artem Polyvyanyy
 */
public class ERDFNode extends Vertex implements IERDFObject {

	public ERDFNode() {
		super();
	}

	public ERDFNode(String name, String desc) {
		super(name, desc);
	}

	public ERDFNode(String name) {
		super(name);
	}

	private IERDFObject obj = new ERDFObject();
	
	/*
	 * (non-Javadoc)
	 * @see de.hpi.bpt.oryx.erdf.IERDFObject#getProperty(java.lang.String)
	 */
	public String getProperty(String name) {
		return this.obj.getProperty(name);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.hpi.bpt.oryx.erdf.IERDFObject#setProperty(java.lang.String, java.lang.String)
	 */
	public String setProperty(String name, String value) {
		return this.obj.setProperty(name, value);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.hpi.bpt.oryx.erdf.IERDFObject#parseERDF(org.w3c.dom.Node)
	 */
	public void parseERDF(Node node) {
		this.setId(node.getAttributes().getNamedItem("id").getTextContent());
		
		this.obj.parseERDF(node);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.hpi.bpt.oryx.erdf.IERDFObject#serializeERDF()
	 */
	public Node serializeERDF() {
		return this.obj.serializeERDF();
	}
}

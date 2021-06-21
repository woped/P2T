package de.dhbw.woped.process2text.bpmlProcessing.bpt.oryx.erdf;

import de.hpi.bpt.oryx.erdf.ERDFEdge;
import de.hpi.bpt.oryx.erdf.ERDFNode;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;


/**
 * Interface to an eRDF model
 * 
 * @author Artem Polyvyanyy
 */
public interface IERDFModel<E extends ERDFEdge<V>, V extends ERDFNode> {
	
	/**
	 * Parse eRDF model form eRDF string
	 * @param erdfString String containing eRDF encoding
	 */
	public void parseERDF(String erdfString) throws SAXException, IOException, ParserConfigurationException;
	
	/**
	 * Parse eRDF model form eRDF file
	 * @param erdfFile File containing eRDF encoding
	 */
	public void parseERDFFile(String erdfFile) throws SAXException, IOException, ParserConfigurationException;

	/**
	 * Get eRDF model serialization string
	 * @return eRDF serialization string of the model
	 */
	public String serializeERDF();
	
	public V createNode(String type);
	
	public E createEdge(String type, V s, V t);
}

package de.dhbw.woped.process2text.bpmnProcessing.bpt.process.epc;

import de.dhbw.woped.process2text.bpmnProcessing.bpt.graph.abs.AbstractDirectedEdge;
import de.dhbw.woped.process2text.bpmnProcessing.bpt.hypergraph.abs.AbstractGraphNotifier;

/**
 * EPC connection implementation
 * 
 * @author Artem Polyvyanyy
 */
public class Connection extends AbstractDirectedEdge<Node> implements IConnection<Node> {
	
	@SuppressWarnings("unchecked")
	protected Connection(AbstractGraphNotifier g, Node source, Node target) {
		super(null, source, target);
		this.source = source;
		this.target = target;
	}
	
	protected Connection (Node source, Node target) {
		super(null, source, target);
		this.source = source;
		this.target = target;
	}
}

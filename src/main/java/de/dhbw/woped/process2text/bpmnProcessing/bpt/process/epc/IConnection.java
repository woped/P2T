package de.dhbw.woped.process2text.bpmnProcessing.bpt.process.epc;

import de.hpi.bpt.graph.abs.IDirectedEdge;
import de.hpi.bpt.process.epc.INode;

/**
 * A connection in an EPC diagram. Connections are all the edges in the graph other than control flow.
 * 
 * @author Artem Polyvyanyy
 */
public interface IConnection<V extends INode> extends IDirectedEdge<V>
{}
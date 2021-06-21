package de.dhbw.woped.process2text.bpmnProcessing.bpt.process;

import de.dhbw.woped.process2text.bpmnProcessing.bpt.hypergraph.abs.Vertex;


public abstract class Node extends Vertex {

	public Node() {
		super();
	}

	public Node(String name, String desc) {
		super(name, desc);
	}

	public Node(String name) {
		super(name);
	}
}

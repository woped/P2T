package de.dhbw.woped.process2text.bpmlProcessing.bpt.hypergraph.abs;

import de.hpi.bpt.hypergraph.abs.GObject;
import de.hpi.bpt.hypergraph.abs.IVertex;

/**
 * Basic graph vertex implementation
 * 
 * @author Artem Polyvyanyy
 */
public class Vertex extends GObject implements IVertex
{	
	public Vertex() {
		super();
	}

	public Vertex(String name, String desc) {
		super(name, desc);
	}

	public Vertex(String name) {
		super(name);
	}
}

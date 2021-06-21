package de.dhbw.woped.process2text.dataModel.bpmnReader.jsonStructure;

import dataModel.jsonStructure.LaneLevel;
import dataModel.jsonStructure.Outgoing;
import dataModel.jsonStructure.PoolProperties;
import dataModel.jsonStructure.Stencil;
import dataModel.jsonStructure.Target;

import java.util.ArrayList;

public class PoolLevel {
	
	String resourceId;
	dataModel.jsonStructure.PoolProperties properties;
	dataModel.jsonStructure.Stencil stencil;
	ArrayList<dataModel.jsonStructure.LaneLevel>childShapes;
	dataModel.jsonStructure.Target target;
	ArrayList<dataModel.jsonStructure.Outgoing> outgoing;
	
	
	
	public ArrayList<dataModel.jsonStructure.Outgoing> getOutgoing() {
		return outgoing;
	}
	public void setOutgoing(ArrayList<Outgoing> outgoing) {
		this.outgoing = outgoing;
	}
	public dataModel.jsonStructure.Target getTarget() {
		return target;
	}
	public void setTarget(Target target) {
		this.target = target;
	}
	public String getResourceId() {
		return resourceId;
	}
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	public dataModel.jsonStructure.PoolProperties getProps() {
		return properties;
	}
	public void setProps(PoolProperties props) {
		this.properties = props;
	}
	public dataModel.jsonStructure.Stencil getStencil() {
		return stencil;
	}
	public void setStencil(Stencil stencil) {
		this.stencil = stencil;
	}
	public ArrayList<dataModel.jsonStructure.LaneLevel> getChildShapes() {
		return childShapes;
	}
	public void setChildShapes(ArrayList<LaneLevel> childShapes) {
		this.childShapes = childShapes;
	}
	
	
	
}

package de.dhbw.woped.process2text.bpmnProcessing.dataModel.jsonStructure;

import dataModel.jsonStructure.ElementProperties;
import dataModel.jsonStructure.Stencil;

import java.util.ArrayList;

public class ElementLevel {
	
	String resourceId;
	ElementProperties properties;
	Stencil stencil;
	ArrayList<dataModel.jsonStructure.ElementLevel>childShapes;
	ArrayList<dataModel.jsonStructure.ElementLevel>outgoing;
	ArrayList<dataModel.jsonStructure.ElementLevel>dockers;

	public String getResourceId() {
		return resourceId;
	}
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	public ElementProperties getProps() {
		return properties;
	}
	public void setProps(ElementProperties props) {
		this.properties = props;
	}
	public Stencil getStencil() {
		return stencil;
	}
	public void setStencil(Stencil stencil) {
		this.stencil = stencil;
	}
	public ArrayList<dataModel.jsonStructure.ElementLevel> getChildShapes() {
		return childShapes;
	}
	public void setChildShapes(ArrayList<dataModel.jsonStructure.ElementLevel> childShapes) {
		this.childShapes = childShapes;
	}
	public ArrayList<dataModel.jsonStructure.ElementLevel> getOutgoing() {
		return outgoing;
	}
	public void setOutgoing(ArrayList<dataModel.jsonStructure.ElementLevel> outgoing) {
		this.outgoing = outgoing;
	}
	public ArrayList<dataModel.jsonStructure.ElementLevel> getDockers() {
		return dockers;
	}
	public void setDockers(ArrayList<dataModel.jsonStructure.ElementLevel> dockers) {
		this.dockers = dockers;
	}
	
	
}

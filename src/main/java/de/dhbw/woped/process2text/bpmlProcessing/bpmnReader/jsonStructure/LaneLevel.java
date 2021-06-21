package de.dhbw.woped.process2text.bpmlProcessing.bpmnReader.jsonStructure;

import dataModel.jsonStructure.ElementLevel;
import dataModel.jsonStructure.LaneProperties;
import dataModel.jsonStructure.Stencil;

import java.util.ArrayList;

public class LaneLevel {
	
	String resourceId;
	LaneProperties properties;
	dataModel.jsonStructure.Stencil stencil;
	ArrayList<dataModel.jsonStructure.ElementLevel>childShapes;
	
	public String getResourceId() {
		return resourceId;
	}
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	public LaneProperties getProps() {
		return properties;
	}
	public void setProps(LaneProperties props) {
		this.properties = props;
	}
	public dataModel.jsonStructure.Stencil getStencil() {
		return stencil;
	}
	public void setStencil(Stencil stencil) {
		this.stencil = stencil;
	}
	public ArrayList<dataModel.jsonStructure.ElementLevel> getChildShapes() {
		return childShapes;
	}
	public void setChildShapes(ArrayList<ElementLevel> childShapes) {
		this.childShapes = childShapes;
	}
	
	

}

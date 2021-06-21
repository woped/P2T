package de.dhbw.woped.process2text.bpmlProcessing.bpmnReader.jsonStructure;

import dataModel.jsonStructure.Doc;

import java.util.ArrayList;

public class Collection {
	
	ArrayList <dataModel.jsonStructure.Doc> models;
	
	public Collection() {
		models = new ArrayList<dataModel.jsonStructure.Doc>();
	}
	
	public void add (dataModel.jsonStructure.Doc model) {
		models.add(model);
	}
	
	public ArrayList<Doc> getModels() {
		return models;
	}

}

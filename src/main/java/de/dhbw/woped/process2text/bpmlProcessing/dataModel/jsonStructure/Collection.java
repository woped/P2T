package de.dhbw.woped.process2text.bpmlProcessing.dataModel.jsonStructure;

import dataModel.jsonStructure.Doc;

import java.util.ArrayList;

public class Collection {
	
	ArrayList <Doc> models;
	
	public Collection() {
		models = new ArrayList<Doc>();
	}
	
	public void add (Doc model) {
		models.add(model);
	}
	
	public ArrayList<Doc> getModels() {
		return models;
	}

}

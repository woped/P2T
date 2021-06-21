package de.dhbw.woped.process2text.bpmlProcessing.dataModel.process;


import dataModel.process.Element;
import dataModel.process.Lane;
import dataModel.process.Pool;

public class Gateway extends Element {
	
	private int type;

	public Gateway(int id, String label, Lane lane, Pool pool, int type) {
		super(id, label, lane, pool);
		this.type = type;
	}

	public int getType() {
		return type;
	}

}

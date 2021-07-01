package de.dhbw.woped.process2text.bpmnProcessing.dataModel.process;


public abstract class Element {
	
	private int id;
	private String label;
	private Lane lane;
	private Pool pool;
	private int subProcessID;

	public Element(int id, String label, Lane lane, Pool pool) {
		this.id = id;
		this.label = label;
		this.lane = lane;
		this.pool = pool;
		this.subProcessID = -1;
	}

	public int getSubProcessID() {
		return this.subProcessID;
	}

	public void setSubProcessID(int id) {
		this.subProcessID = id;
	}

	public Pool getPool() {
		return pool;
	}

	public int getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public Lane getLane() {
		return lane;
	}

	public void setLane(Lane lane) {
		this.lane = lane;
	}

	public void setPool(Pool pool) {
		this.pool = pool;
	}
	
	

}

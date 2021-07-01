package de.dhbw.woped.process2text.bpmnProcessing.preprocessing;

import de.hpi.bpt.process.*;
import de.dhbw.woped.process2text.bpmnProcessing.dataModel.process.Arc;
import de.dhbw.woped.process2text.bpmnProcessing.dataModel.process.Element;
import de.dhbw.woped.process2text.bpmnProcessing.dataModel.process.ProcessModel;
import de.dhbw.woped.process2text.bpmnProcessing.dataModel.process.Activity;
import de.dhbw.woped.process2text.bpmnProcessing.dataModel.process.Event;
import de.dhbw.woped.process2text.bpmnProcessing.dataModel.process.Gateway;
import de.dhbw.woped.process2text.bpmnProcessing.dataModel.process.GatewayType;

import de.hpi.bpt.process.Process;

import java.util.HashMap;

public class FormatConverter {
	
	private HashMap<Integer, Element> converterMap;
	private int newElems;
	
	
	/**
	 * Reconstructs the ProcessModel format from HPI Process Model after Rigid Structuring 
	 */
	public ProcessModel transformFromRigidFormat(Process p) {
		ProcessModel pm = new ProcessModel(0, "Structured Model");
		
		HashMap<String,Integer> idMap = new HashMap<String, Integer>();
		HashMap<Integer,Element> elemMap = new HashMap<Integer, Element>();
		newElems = 0;
		
		for (Task t: p.getTasks()) {
			int id = Integer.valueOf(t.getName());
			if (converterMap.containsKey(id)) {
				Element elem = converterMap.get(id);
				
				if (elem.getClass().toString().endsWith("Activity")) { 
					Activity a = (Activity) elem;
					pm.addActivity(a);
					idMap.put(t.getId(), a.getId());
					elemMap.put(a.getId(), a);
				}
				if (elem.getClass().toString().endsWith("Event")) { 
					Event e = (Event) elem;
					pm.addEvent(e);
					idMap.put(t.getId(), e.getId());
					elemMap.put(e.getId(), e);
				}
			} else {
				System.out.println("ERROR: Transformation Problem");
			}
		}
		
		for (de.hpi.bpt.process.Gateway g: p.getGateways()) {
			if (!g.getName().equals("") && converterMap.containsKey(Integer.valueOf(g.getName()))) {
				int id = Integer.valueOf(g.getName());
				Gateway gw = (Gateway) converterMap.get(id);
				pm.addGateway(gw);
			} else {
				if (g.getGatewayType() == de.hpi.bpt.process.GatewayType.XOR) {
					Gateway gw = new Gateway(getId(), "", null, null, GatewayType.XOR);
					pm.addGateway(gw);
					idMap.put(g.getId(), gw.getId());
					elemMap.put(gw.getId(), gw);
				}
				if (g.getGatewayType() == de.hpi.bpt.process.GatewayType.OR) {
					Gateway gw = new Gateway(getId(), "", null, null, GatewayType.OR);
					pm.addGateway(gw);
					idMap.put(g.getId(), gw.getId());
					elemMap.put(gw.getId(), gw);
				}
				if (g.getGatewayType() == de.hpi.bpt.process.GatewayType.AND) {
					Gateway gw = new Gateway(getId(), "", null, null, GatewayType.AND);
					pm.addGateway(gw);
					idMap.put(g.getId(), gw.getId());
					elemMap.put(gw.getId(), gw);
				}
				if (g.getGatewayType() == de.hpi.bpt.process.GatewayType.EVENT) {
					Gateway gw = new Gateway(getId(), "", null, null, GatewayType.EVENT);
					pm.addGateway(gw);
					idMap.put(g.getId(), gw.getId());
					elemMap.put(gw.getId(), gw);
				}
			}
		}
		
		for (ControlFlow f: p.getControlFlow()) {
			Element source = elemMap.get(idMap.get(f.getSource().getId()));
			Element target = elemMap.get(idMap.get(f.getTarget().getId()));
			Arc arc = new Arc(getId(), f.getName(), source , target);
			pm.addArc(arc);
		}
		return pm;
	}

	/**
	 * Transforms ProcessModel format to HPI Process Format (writes IDs to labels in order to save the information)
	 */
	public Process transformToRigidFormat(ProcessModel pm) {
		Process p = new Process();
		converterMap = new HashMap<Integer, Element>();
		HashMap <Integer, Node> elementMap = new HashMap<Integer, Node>();

		// Transform activities
		for (Activity a: pm.getActivites().values()) {
			Task t = new Task(Integer.toString(a.getId()));
			elementMap.put(a.getId(), t);
			converterMap.put(a.getId(),a);
		}

		// Transform events
		for (Event e: pm.getEvents().values()) {
			Task et = new Task(Integer.toString(e.getId()));
			elementMap.put(e.getId(), et);
			converterMap.put(e.getId(),e);
		}

		// Transform gateway
		for (Gateway g: pm.getGateways().values()) {
			if (g.getType() == GatewayType.XOR) {
				de.hpi.bpt.process.Gateway gt = new de.hpi.bpt.process.Gateway(de.hpi.bpt.process.GatewayType.XOR,Integer.toString(g.getId()));
				elementMap.put(g.getId(), gt);

			}
			if (g.getType() == GatewayType.OR) {
				de.hpi.bpt.process.Gateway gt = new de.hpi.bpt.process.Gateway(de.hpi.bpt.process.GatewayType.OR,Integer.toString(g.getId()));
				elementMap.put(g.getId(), gt);
			}
			if (g.getType() == GatewayType.AND) {
				de.hpi.bpt.process.Gateway gt = new de.hpi.bpt.process.Gateway(de.hpi.bpt.process.GatewayType.AND,Integer.toString(g.getId()));
				elementMap.put(g.getId(), gt);
			}
			if (g.getType() == GatewayType.EVENT) {
				de.hpi.bpt.process.Gateway gt = new de.hpi.bpt.process.Gateway(de.hpi.bpt.process.GatewayType.EVENT,Integer.toString(g.getId()));
				elementMap.put(g.getId(), gt);
			}
			converterMap.put(g.getId(),g);
		}

		// Transform arcs
		for (Arc arc: pm.getArcs().values()) {
			if (arc.getSource() != null) {
				p.addControlFlow(elementMap.get(arc.getSource().getId()), elementMap.get(arc.getTarget().getId()));
			}
		}
		return p;
	}


	/**
	 * Transforms given ProcessModel to HPI format
	 */
	public Process transformToRPSTFormat(ProcessModel pm) {

		Process p = new Process();
		HashMap <Integer, Node> elementMap = new HashMap<Integer, Node>();

		// Transform activities
		for (Activity a: pm.getActivites().values()) {
			Task t = new Task(a.getLabel());
			t.setId(Integer.toString(a.getId()));
			elementMap.put(a.getId(), t);
		}

		// Transform events
		for (Event e: pm.getEvents().values()) {
			de.hpi.bpt.process.Event et = new de.hpi.bpt.process.Event(e.getLabel());
			et.setId(Integer.toString(e.getId()));
			elementMap.put(e.getId(), et);
		}

		// Transform gateway
		for (Gateway g: pm.getGateways().values()) {
			if (g.getType() == GatewayType.XOR) {
				de.hpi.bpt.process.Gateway gt = new de.hpi.bpt.process.Gateway(de.hpi.bpt.process.GatewayType.XOR, g.getLabel());
				gt.setId(Integer.toString(g.getId()));
				elementMap.put(g.getId(), gt);

			}
			if (g.getType() == GatewayType.OR) {
				de.hpi.bpt.process.Gateway gt = new de.hpi.bpt.process.Gateway(de.hpi.bpt.process.GatewayType.OR, g.getLabel());
				gt.setId(Integer.toString(g.getId()));
				elementMap.put(g.getId(), gt);
			}
			if (g.getType() == GatewayType.AND) {
				de.hpi.bpt.process.Gateway gt = new de.hpi.bpt.process.Gateway(de.hpi.bpt.process.GatewayType.AND, g.getLabel());
				gt.setId(Integer.toString(g.getId()));
				elementMap.put(g.getId(), gt);
			}
			if (g.getType() == GatewayType.EVENT) {
				de.hpi.bpt.process.Gateway gt = new de.hpi.bpt.process.Gateway(de.hpi.bpt.process.GatewayType.EVENT, g.getLabel());
				gt.setId(Integer.toString(g.getId()));
				elementMap.put(g.getId(), gt);
			}
		}

		// Transform arcs
		for (Arc arc: pm.getArcs().values()) {
			if (arc.getSource() != null) {
				p.addControlFlow(elementMap.get(arc.getSource().getId()), elementMap.get(arc.getTarget().getId()));
			}	
		}
		return p;
	}
	
	/**
	 * Calculates new ID 
	 */
	private int getId() {
		int max = -1;
		for (int i: converterMap.keySet()) {
			if (i>max) {
				max = i;
			}
		}
		newElems++;
		return max+newElems;
	}
}

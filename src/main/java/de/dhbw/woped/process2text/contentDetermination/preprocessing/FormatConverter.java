/* (C)2022 */
package de.dhbw.woped.process2text.contentDetermination.preprocessing;

import de.dhbw.woped.process2text.dataModel.process.Activity;
import de.dhbw.woped.process2text.dataModel.process.Element;
import de.dhbw.woped.process2text.dataModel.process.ProcessModel;
import de.hpi.bpt.process.ControlFlow;
import de.hpi.bpt.process.Event;
import de.hpi.bpt.process.Gateway;
import de.hpi.bpt.process.GatewayType;
import de.hpi.bpt.process.Node;
import de.hpi.bpt.process.Process;
import de.hpi.bpt.process.Task;
import java.util.HashMap;

public class FormatConverter {
  private HashMap<Integer, Element> converterMap;
  private int newElems;

  /** Reconstructs the ProcessModel format from HPI Process Model after Rigid Structuring */
  public ProcessModel transformFromRigidFormat(Process p) {
    ProcessModel pm = new ProcessModel();

    HashMap<String, Integer> idMap = new HashMap<>();
    HashMap<Integer, Element> elemMap = new HashMap<>();
    newElems = 0;

    for (Task t : p.getTasks()) {
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
          de.dhbw.woped.process2text.dataModel.process.Event e =
              (de.dhbw.woped.process2text.dataModel.process.Event) elem;
          pm.addEvent(e);
          idMap.put(t.getId(), e.getId());
          elemMap.put(e.getId(), e);
        }
      } else {
        System.out.println("ERROR: Transformation Problem");
      }
    }

    for (Gateway g : p.getGateways()) {
      if (!g.getName().equals("") && converterMap.containsKey(Integer.valueOf(g.getName()))) {
        int id = Integer.valueOf(g.getName());
        de.dhbw.woped.process2text.dataModel.process.Gateway gw =
            (de.dhbw.woped.process2text.dataModel.process.Gateway) converterMap.get(id);
        pm.addGateway(gw);
      } else {
        if (g.getGatewayType() == GatewayType.XOR) {
          de.dhbw.woped.process2text.dataModel.process.Gateway gw =
              new de.dhbw.woped.process2text.dataModel.process.Gateway(
                  getId(),
                  "",
                  null,
                  null,
                  de.dhbw.woped.process2text.dataModel.process.GatewayType.XOR);
          pm.addGateway(gw);
          idMap.put(g.getId(), gw.getId());
          elemMap.put(gw.getId(), gw);
        }
        if (g.getGatewayType() == GatewayType.OR) {
          de.dhbw.woped.process2text.dataModel.process.Gateway gw =
              new de.dhbw.woped.process2text.dataModel.process.Gateway(
                  getId(),
                  "",
                  null,
                  null,
                  de.dhbw.woped.process2text.dataModel.process.GatewayType.OR);
          pm.addGateway(gw);
          idMap.put(g.getId(), gw.getId());
          elemMap.put(gw.getId(), gw);
        }
        if (g.getGatewayType() == GatewayType.AND) {
          de.dhbw.woped.process2text.dataModel.process.Gateway gw =
              new de.dhbw.woped.process2text.dataModel.process.Gateway(
                  getId(),
                  "",
                  null,
                  null,
                  de.dhbw.woped.process2text.dataModel.process.GatewayType.AND);
          pm.addGateway(gw);
          idMap.put(g.getId(), gw.getId());
          elemMap.put(gw.getId(), gw);
        }
        if (g.getGatewayType() == GatewayType.EVENT) {
          de.dhbw.woped.process2text.dataModel.process.Gateway gw =
              new de.dhbw.woped.process2text.dataModel.process.Gateway(
                  getId(),
                  "",
                  null,
                  null,
                  de.dhbw.woped.process2text.dataModel.process.GatewayType.EVENT);
          pm.addGateway(gw);
          idMap.put(g.getId(), gw.getId());
          elemMap.put(gw.getId(), gw);
        }
      }
    }

    for (ControlFlow f : p.getControlFlow()) {
      Element source = elemMap.get(idMap.get(f.getSource().getId()));
      Element target = elemMap.get(idMap.get(f.getTarget().getId()));
      de.dhbw.woped.process2text.dataModel.process.Arc arc =
          new de.dhbw.woped.process2text.dataModel.process.Arc(
              getId(), f.getName(), source, target);
      pm.addArc(arc);
    }
    return pm;
  }

  /**
   * Transforms ProcessModel format to HPI Process Format (writes IDs to labels in order to save the
   * information)
   */
  public Process transformToRigidFormat(ProcessModel pm) {
    Process p = new Process();
    converterMap = new HashMap<>();
    HashMap<Integer, Node> elementMap = new HashMap<>();

    // Transform activities
    for (Activity a : pm.getActivites().values()) {
      Task t = new Task(Integer.toString(a.getId()));
      elementMap.put(a.getId(), t);
      converterMap.put(a.getId(), a);
    }

    // Transform events
    for (de.dhbw.woped.process2text.dataModel.process.Event e : pm.getEvents().values()) {
      Task et = new Task(Integer.toString(e.getId()));
      elementMap.put(e.getId(), et);
      converterMap.put(e.getId(), e);
    }

    // Transform gateway
    for (de.dhbw.woped.process2text.dataModel.process.Gateway g : pm.getGateways().values()) {
      if (g.getType() == de.dhbw.woped.process2text.dataModel.process.GatewayType.XOR) {
        Gateway gt = new Gateway(GatewayType.XOR, Integer.toString(g.getId()));
        elementMap.put(g.getId(), gt);
      }
      if (g.getType() == de.dhbw.woped.process2text.dataModel.process.GatewayType.OR) {
        Gateway gt = new Gateway(GatewayType.OR, Integer.toString(g.getId()));
        elementMap.put(g.getId(), gt);
      }
      if (g.getType() == de.dhbw.woped.process2text.dataModel.process.GatewayType.AND) {
        Gateway gt = new Gateway(GatewayType.AND, Integer.toString(g.getId()));
        elementMap.put(g.getId(), gt);
      }
      if (g.getType() == de.dhbw.woped.process2text.dataModel.process.GatewayType.EVENT) {
        Gateway gt = new Gateway(GatewayType.EVENT, Integer.toString(g.getId()));
        elementMap.put(g.getId(), gt);
      }
      converterMap.put(g.getId(), g);
    }

    // Transform arcs
    for (de.dhbw.woped.process2text.dataModel.process.Arc arc : pm.getArcs().values()) {
      if (arc.getSource() != null) {
        p.addControlFlow(
            elementMap.get(arc.getSource().getId()), elementMap.get(arc.getTarget().getId()));
      }
    }
    return p;
  }

  /** Transforms given ProcessModel to HPI format */
  public Process transformToRPSTFormat(ProcessModel pm) {
    Process p = new Process();
    HashMap<Integer, Node> elementMap = new HashMap<>();

    // Transform activities
    for (Activity a : pm.getActivites().values()) {
      Task t = new Task(a.getLabel());
      t.setId(Integer.toString(a.getId()));
      // if (a.getType() == ActivityType.TYPE_MAP.get("Subprocess")) t.setDescription("sub");
      elementMap.put(a.getId(), t);
    }

    // Transform events
    for (de.dhbw.woped.process2text.dataModel.process.Event e : pm.getEvents().values()) {
      Event et = new Event(e.getLabel());
      et.setId(Integer.toString(e.getId()));
      elementMap.put(e.getId(), et);
    }

    // Transform gateway
    for (de.dhbw.woped.process2text.dataModel.process.Gateway g : pm.getGateways().values()) {
      if (g.getType() == de.dhbw.woped.process2text.dataModel.process.GatewayType.XOR) {
        Gateway gt = new Gateway(GatewayType.XOR, g.getLabel());
        gt.setId(Integer.toString(g.getId()));
        elementMap.put(g.getId(), gt);
      }
      if (g.getType() == de.dhbw.woped.process2text.dataModel.process.GatewayType.OR) {
        Gateway gt = new Gateway(GatewayType.OR, g.getLabel());
        gt.setId(Integer.toString(g.getId()));
        elementMap.put(g.getId(), gt);
      }
      if (g.getType() == de.dhbw.woped.process2text.dataModel.process.GatewayType.AND) {
        Gateway gt = new Gateway(GatewayType.AND, g.getLabel());
        gt.setId(Integer.toString(g.getId()));
        elementMap.put(g.getId(), gt);
      }
      if (g.getType() == de.dhbw.woped.process2text.dataModel.process.GatewayType.EVENT) {
        Gateway gt = new Gateway(GatewayType.EVENT, g.getLabel());
        gt.setId(Integer.toString(g.getId()));
        elementMap.put(g.getId(), gt);
      }
    }

    // Transform arcs
    for (de.dhbw.woped.process2text.dataModel.process.Arc arc : pm.getArcs().values()) {
      if (arc.getSource() != null) {
        p.addControlFlow(
            elementMap.get(arc.getSource().getId()), elementMap.get(arc.getTarget().getId()));
      }
    }
    return p;
  }

  /** Calculates new ID */
  private int getId() {
    int max = -1;
    for (int i : converterMap.keySet()) {
      if (i > max) {
        max = i;
      }
    }
    newElems++;
    return max + newElems;
  }
}

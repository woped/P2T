package de.dhbw.woped.process2text.model.reader.bpmn;

import de.dhbw.woped.process2text.model.process.*;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BPMNReader {

  Logger logger = LoggerFactory.getLogger(BPMNReader.class);

  public HashMap<Integer, String> transformedElemsRev;

  public ProcessModel getProcessModelFromBPMNString(InputStream input) {
    try {
      transformedElemsRev = new HashMap<>();
      // Umwandlung des Inputstream in die Klasse Document zur einfacheren Weiterverarbeitung
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(input);
      doc.getDocumentElement().normalize();

      // Initialisierung des ProcessModels
      ProcessModel model = new ProcessModel();
      // Aufruf von Extractpool --> ruft alle anderen Extractmethoden außer extractArc auf
      extractPool(doc, model);
      extractArc(doc, model);
      return model;
    } catch (Exception e) {
      logger.error(e.getLocalizedMessage());
    }
    return null;
  }

  private void extractActivity(
      Document doc,
      ProcessModel model,
      Element poolElement,
      Pool pool,
      Element laneElement,
      Lane lane) {
    // Bestimmung der Art der Activities im Pool
    int type = 1;
    NodeList participants = doc.getElementsByTagName("bpmn:Participant");
    for (int j = 0; j < participants.getLength(); j++) {
      Element participant = (Element) participants.item(j);
      if (participant.getAttribute("processRef") == pool.getBPMNId()) {
        if (participant.getAttribute("name") == "Company"
            || participant.getAttribute("name") == "User") {
          type = 2;
        }
      }
    }
    NodeList flowNodes = null;
    // Überprüfen ob lane vorhanden ist
    if (lane != null) {
      // extrahieren der Elemente innerhalb der Lane
      flowNodes = laneElement.getElementsByTagName("bpmn:flowNodeRef");
    }
    // Durchlaufen aller Activities im Pool,
    NodeList activities = poolElement.getElementsByTagName("bpmn:task");
    for (int j = 0; j < activities.getLength(); j++) {
      Element scdNode = (Element) activities.item(j);
      boolean inLane = false;
      // Überprüfen, ob es eine Lane gibt
      if (flowNodes != null) {
        // Überprüfen, ob activity in der Lane ist
        for (int k = 0; k < flowNodes.getLength(); k++) {
          if (flowNodes.item(k).getTextContent().equals(scdNode.getAttribute("id"))) {
            inLane = true;
            break;
          }
        }
      }
      if (inLane || flowNodes.getLength() < 0) {
        // erstellen der Activity, hinzufügen in model und transformedElemsRev
        Activity activity =
            new Activity(model.getNewId(), scdNode.getAttribute("name"), lane, pool, type);
        activity.addBPMNId(scdNode.getAttribute("id"));
        model.addActivity(activity);
        this.transformedElemsRev.put(model.getNewId(), scdNode.getAttribute("id"));
      }
    }
  }

  private void extractEvents(
      Document doc,
      ProcessModel model,
      Element poolElement,
      Pool pool,
      Element laneElement,
      Lane lane) {
    // Anlegen von Listen für die verschiedenen Eventarten
    NodeList intermediate_event = poolElement.getElementsByTagName("bpmn:intermediateCatchEvent");
    NodeList start_event = poolElement.getElementsByTagName("bpmn:startEvent");
    NodeList end_event = poolElement.getElementsByTagName("bpmn:endEvent");
    int newId;
    NodeList flowNodes = null;
    // Überprüfen ob lane vorhanden ist
    if (lane != null) {
      // extrahieren der Elemente innerhalb der Lane
      flowNodes = laneElement.getElementsByTagName("bpmn:flowNodeRef");
    }
    // Durchlaufen der verschiedenen Eventarten, erstellen der einzelnen Events, einfügen in model
    // und
    // transformedElemsRev

    // intermediateCatchEvents
    for (int j = 0; j < intermediate_event.getLength(); j++) {
      Node scdNode = intermediate_event.item(j);
      Element event = (Element) scdNode;
      boolean inLane = false;
      // Überprüfen, ob es eine Lane gibt
      if (flowNodes != null) {
        // Überprüfen, ob activity in der Lane ist
        for (int k = 0; k < flowNodes.getLength(); k++) {
          if (flowNodes.item(k).getTextContent().equals(event.getAttribute("id"))) {
            inLane = true;
            break;
          }
        }
      }
      if (inLane || flowNodes.getLength() < 0) {

        newId = model.getNewId();
        Event interelement = new Event(newId, "", lane, pool, EventType.INTM_MSG_THR);
        interelement.addBPMNId(event.getAttribute("id"));
        model.addEvent(interelement);
        // model.addEvent(new Event(newId, "", lane, pool, EventType.START_EVENT));
        this.transformedElemsRev.put(model.getNewId(), event.getAttribute("id"));
      }
    }

    // Startevent wird als Gateway abgespeichert (analog zur PNML-Klasse)
    for (int j = 0; j < start_event.getLength(); j++) {
      Node scdNode = start_event.item(j);
      Element event = (Element) scdNode;
      boolean inLane = false;
      // Überprüfen, ob es eine Lane gibt
      if (flowNodes != null) {
        // Überprüfen, ob activity in der Lane ist
        for (int k = 0; k < flowNodes.getLength(); k++) {
          if (flowNodes.item(k).getTextContent().equals(event.getAttribute("id"))) {
            inLane = true;
            break;
          }
        }
      }
      if (inLane || flowNodes.getLength() < 0) {
        newId = model.getNewId();
        Gateway gateway = new Gateway(newId, "", lane, pool, 0);
        gateway.addBPMNId(event.getAttribute("id"));
        model.addGateway(gateway);
        // model.addEvent(new Event(newId, "", lane, pool, EventType.START_EVENT));
        this.transformedElemsRev.put(model.getNewId(), event.getAttribute("id"));
      }
    }

    // Endevent wird als Activity abgespeichert (analog zur PNML-Klasse)
    for (int j = 0; j < end_event.getLength(); j++) {
      Node scdNode = end_event.item(j);
      Element event = (Element) scdNode;
      boolean inLane = false;
      // Überprüfen, ob es eine Lane gibt
      if (flowNodes != null) {
        // Überprüfen, ob activity in der Lane ist
        for (int k = 0; k < flowNodes.getLength(); k++) {
          if (flowNodes.item(k).getTextContent().equals(event.getAttribute("id"))) {
            inLane = true;
            break;
          }
        }
      }
      if (inLane || flowNodes.getLength() < 0) {
        newId = model.getNewId();
        Activity activity = new Activity(newId, "complete process", lane, pool, 0);
        activity.addBPMNId(event.getAttribute("id"));
        model.addActivity(activity);
        // model.addEvent(new Event(newId, "", lane, pool, EventType.END_EVENT));
        this.transformedElemsRev.put(model.getNewId(), event.getAttribute("id"));
      }
    }
  }

  private void extractGateways(
      Document doc,
      ProcessModel model,
      Element poolElement,
      Pool pool,
      Element laneElement,
      Lane lane) {
    // Anlegen von Listen für die verschiedenen Gateway-Arten
    NodeList list_AND = poolElement.getElementsByTagName("bpmn:parallelGateway");
    NodeList list_XOR = poolElement.getElementsByTagName("bpmn:exclusiveGateway");
    int newId;
    NodeList flowNodes = null;
    // Überprüfen ob lane vorhanden ist
    if (lane != null) {
      // extrahieren der Elemente innerhalb der Lane
      flowNodes = laneElement.getElementsByTagName("bpmn:flowNodeRef");
    }
    // Durchlaufen der verschiedenen Gatewayarten, erstellen der einzelnen Gateways, einfügen in
    // model und
    // transformedElemsRev

    // AND-Gateway
    for (int j = 0; j < list_AND.getLength(); j++) {
      Node scdNode = list_AND.item(j);
      Element gw = (Element) scdNode;
      boolean inLane = false;
      // Überprüfen, ob es eine Lane gibt
      if (flowNodes != null) {
        // Überprüfen, ob activity in der Lane ist
        for (int k = 0; k < flowNodes.getLength(); k++) {
          if (flowNodes.item(k).getTextContent().equals(gw.getAttribute("id"))) {
            inLane = true;
            break;
          }
        }
      }
      if (inLane || flowNodes.getLength() < 0) {
        newId = model.getNewId();
        Gateway gateway = new Gateway(newId, "", lane, pool, GatewayType.AND);
        gateway.addBPMNId(gw.getAttribute("id"));
        model.addGateway(gateway);
        this.transformedElemsRev.put(model.getNewId(), gw.getAttribute("id"));
      }
    }

    // XOR-Gateway
    for (int j = 0; j < list_XOR.getLength(); j++) {
      Node scdNode = list_XOR.item(j);
      Element gw = (Element) scdNode;
      boolean inLane = false;
      // Überprüfen, ob es eine Lane gibt
      if (flowNodes != null) {
        // Überprüfen, ob activity in der Lane ist
        for (int k = 0; k < flowNodes.getLength(); k++) {
          if (flowNodes.item(k).getTextContent().equals(gw.getAttribute("id"))) {
            inLane = true;
            break;
          }
        }
      }
      if (inLane || flowNodes.getLength() < 0) {
        newId = model.getNewId();
        Gateway gateway = new Gateway(newId, "", lane, pool, GatewayType.XOR);
        gateway.addBPMNId(gw.getAttribute("id"));
        model.addGateway(gateway);
        this.transformedElemsRev.put(model.getNewId(), gw.getAttribute("id"));
      }
    }
  }

  private void extractLane(Document doc, ProcessModel model, Element poolElement, Pool pool) {
    // Anlegen einer Liste aller Lanes innerhalb des Pools
    NodeList lanes = poolElement.getElementsByTagName("bpmn:lane");
    // erstellen der einzelnen Lanes, einfügen in model und
    // transformedElemsRev, aufruf der übrigen extract-Methoden
    for (int j = 0; j < lanes.getLength(); j++) {
      Element laneElement = (Element) lanes.item(j);
      Lane lane = new Lane(laneElement.getAttribute("name"), pool.getName());
      lane.addBPMNId(laneElement.getAttribute("id"));
      model.addLane(laneElement.getAttribute("name"));
      this.transformedElemsRev.put(model.getNewId(), laneElement.getAttribute("id"));
      extractActivity(doc, model, poolElement, pool, laneElement, lane);
      extractEvents(doc, model, poolElement, pool, laneElement, lane);
      extractGateways(doc, model, poolElement, pool, laneElement, lane);
    }
  }

  private void extractPool(Document doc, ProcessModel model) {
    NodeList pools = doc.getElementsByTagName("bpmn:process");
    for (int i = 0; i < pools.getLength(); i++) {
      Element poolElement = (Element) pools.item(i);
      NodeList lanes = poolElement.getElementsByTagName("bpmn:laneSet");
      Pool pool = new Pool(poolElement.getAttribute("id"));
      model.addPool(poolElement.getAttribute("id"));
      this.transformedElemsRev.put(model.getNewId(), poolElement.getAttribute("id"));
      if (lanes.getLength() == 0) {
        extractActivity(doc, model, poolElement, pool, null, null);
        extractEvents(doc, model, poolElement, pool, null, null);
        extractGateways(doc, model, poolElement, pool, null, null);
      } else {
        extractLane(doc, model, poolElement, pool);
      }
    }
  }

  private void extractArc(Document doc, ProcessModel model) {
    NodeList list = doc.getElementsByTagName("bpmn:sequenceFlow");
    for (int i = 0; i < list.getLength(); i++) {
      Node fstNode = list.item(i);
      Element arc = (Element) fstNode;
      String sourceRef = arc.getAttribute("sourceRef");
      String targetRef = arc.getAttribute("targetRef");
      String label = arc.getAttribute("id");
      de.dhbw.woped.process2text.model.process.Element source = null;
      de.dhbw.woped.process2text.model.process.Element target = null;
      HashMap<Integer, Activity> activities = model.getActivites();
      HashMap<Integer, Event> events = model.getEvents();
      HashMap<Integer, Gateway> gateways = model.getGateways();
      for (Map.Entry e : activities.entrySet()) {
        Activity activity = (Activity) e.getValue();
        if (activity.getBpmnId().equals(sourceRef)) {
          source = activity;
        }
        if (activity.getBpmnId().equals(targetRef)) {
          target = activity;
        }
      }
      if (source == (null) || target == (null)) {
        for (Map.Entry e : events.entrySet()) {
          Event event = (Event) e.getValue();
          if (event.getBpmnId().equals(sourceRef)) {
            source = event;
          }
          if (event.getBpmnId().equals(targetRef)) {
            target = event;
          }
        }
        if (source == (null) || target == (null)) {
          for (Map.Entry e : gateways.entrySet()) {
            Gateway gateway = (Gateway) e.getValue();
            if (gateway.getBpmnId().equals(sourceRef)) {
              source = gateway;
            }
            if (gateway.getBpmnId().equals(targetRef)) {
              target = gateway;
            }
          }
        }
      }
      model.addArc(new Arc(model.getNewId(), label, source, target));
      this.transformedElemsRev.put(model.getNewId(), arc.getAttribute("id"));
    }
  }
}

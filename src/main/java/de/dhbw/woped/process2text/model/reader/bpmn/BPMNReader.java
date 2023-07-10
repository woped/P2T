package de.dhbw.woped.process2text.model.reader.bpmn;

import de.dhbw.woped.process2text.model.process.*;
import java.io.InputStream;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class to extract the bpmn model elements out of xml*/
public class BPMNReader {

  Logger logger = LoggerFactory.getLogger(BPMNReader.class);

  private static final String BPMN_FLOW_NODE_REF = "bpmn:flowNodeRef";

  public HashMap<Integer, String> transformedElemsRev;

  /**
   * Method transform Input Stream for further processing into a document
   * @param input Input Stream of the XML file
   * */
  public ProcessModel getProcessModelFromBPMNString(InputStream input) {
    try {
      transformedElemsRev = new HashMap<>();
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(input);
      doc.getDocumentElement().normalize();

      ProcessModel model = new ProcessModel();

      // call extract methods
      extractPool(doc, model);
      extractArc(doc, model);
      return model;
    } catch (Exception e) {
      logger.error(e.getLocalizedMessage());
    }
    return null;
  }

  /** Method extract activities out of the xml document */
  private void extractActivity(
      Document doc,
      ProcessModel model,
      Element poolElement,
      Pool pool,
      Element laneElement,
      Lane lane) {

    // Determination of the type of activities in the pool
    int type = 1;
    NodeList participants = doc.getElementsByTagName("bpmn:Participant");
    for (int j = 0; j < participants.getLength(); j++) {
      Element participant = (Element) participants.item(j);
      if (participant.getAttribute("processRef") == pool.getBPMNId()
          && (participant.getAttribute("name") == "Company"
              || participant.getAttribute("name") == "User")) {
        type = 2;
      }
    }

    // Check if lane exists
    NodeList flowNodes = null;
    if (lane != null) {
      // extract the elements within the lane
      flowNodes = laneElement.getElementsByTagName(BPMN_FLOW_NODE_REF);
    }

    // Check all activities in pool
    NodeList activities = poolElement.getElementsByTagName("bpmn:task");
    for (int j = 0; j < activities.getLength(); j++) {
      Element scdNode = (Element) activities.item(j);
      boolean inLane = false;
      // Check if lane exists
      if (flowNodes != null) {
        // Check if activity is in the lane
        for (int k = 0; k < flowNodes.getLength(); k++) {
          if (flowNodes.item(k).getTextContent().equals(scdNode.getAttribute("id"))) {
            inLane = true;
            break;
          }
        }
      }

      // create the activity, add in model and transformedElemsRev
      if (inLane || flowNodes == null || flowNodes.getLength() < 0) {
        Activity activity =
            new Activity(model.getNewId(), scdNode.getAttribute("name"), lane, pool, type);
        activity.addBPMNId(scdNode.getAttribute("id"));
        model.addActivity(activity);
        this.transformedElemsRev.put(model.getNewId(), scdNode.getAttribute("id"));
      }
    }
  }

  /** Method extracts all events out of the document xml string*/
  private void extractEvents( ProcessModel model, Element poolElement, Pool pool, Element laneElement, Lane lane) {

    // Create lists for the different event types
    NodeList intermediateEvent = poolElement.getElementsByTagName("bpmn:intermediateCatchEvent");
    NodeList startEvent        = poolElement.getElementsByTagName("bpmn:startEvent");
    NodeList endEvent          = poolElement.getElementsByTagName("bpmn:endEvent");

    int newId;
    NodeList flowNodes = null;


    // Check if lane exists
    if (lane != null) {
      // extract all elements within lane
      flowNodes = laneElement.getElementsByTagName(BPMN_FLOW_NODE_REF);
    }

    // Run through the different event types, create the individual events, insert into model

    // intermediateCatchEvents
    for (int j = 0; j < intermediateEvent.getLength(); j++) {
      Node scdNode = intermediateEvent.item(j);
      Element event = (Element) scdNode;
      boolean inLane = false;
      if (flowNodes != null) {
        for (int k = 0; k < flowNodes.getLength(); k++) {
          if (flowNodes.item(k).getTextContent().equals(event.getAttribute("id"))) {
            inLane = true;
            break;
          }
        }
      }
      if (inLane || flowNodes == null || flowNodes.getLength() < 0) {
        newId = model.getNewId();
        Event interelement = new Event(newId, "", lane, pool, EventType.INTM_MSG_THR);
        interelement.addBPMNId(event.getAttribute("id"));
        model.addEvent(interelement);
        this.transformedElemsRev.put(model.getNewId(), event.getAttribute("id"));
      }
    }

    // Start event is stored as gateway (analog to PNML class)
    for (int j = 0; j < startEvent.getLength(); j++) {
      Node scdNode = startEvent.item(j);
      Element event = (Element) scdNode;
      boolean inLane = false;
      if (flowNodes != null) {
        for (int k = 0; k < flowNodes.getLength(); k++) {
          if (flowNodes.item(k).getTextContent().equals(event.getAttribute("id"))) {
            inLane = true;
            break;
          }
        }
      }
      if (inLane || flowNodes == null || flowNodes.getLength() < 0) {
        newId = model.getNewId();
        Gateway gateway = new Gateway(newId, "", lane, pool, 0);
        gateway.addBPMNId(event.getAttribute("id"));
        model.addGateway(gateway);
        this.transformedElemsRev.put(model.getNewId(), event.getAttribute("id"));
      }
    }

    // End event is saved as an activity
    for (int j = 0; j < endEvent.getLength(); j++) {
      Node scdNode = endEvent.item(j);
      Element event = (Element) scdNode;
      boolean inLane = false;
      if (flowNodes != null) {
        for (int k = 0; k < flowNodes.getLength(); k++) {
          if (flowNodes.item(k).getTextContent().equals(event.getAttribute("id"))) {
            inLane = true;
            break;
          }
        }
      }

      if (inLane || flowNodes == null || flowNodes.getLength() < 0) {
        newId = model.getNewId();
        Activity activity = new Activity(newId, "complete process", lane, pool, 0);
        activity.addBPMNId(event.getAttribute("id"));
        model.addActivity(activity);
        this.transformedElemsRev.put(model.getNewId(), event.getAttribute("id"));
      }
    }
  }

  /** Method extracts all gateway elements out of the document xml string*/
  private void extractGateways(
      ProcessModel model, Element poolElement, Pool pool, Element laneElement, Lane lane) {

    // Creating lists for the different gateway types
    NodeList listAND = poolElement.getElementsByTagName("bpmn:parallelGateway");
    NodeList listXOR = poolElement.getElementsByTagName("bpmn:exclusiveGateway");

    int newId;
    NodeList flowNodes = null;
    if (lane != null) {
      flowNodes = laneElement.getElementsByTagName(BPMN_FLOW_NODE_REF);
    }

    // Run through the different gateway types, create the individual gateways.

    // AND-Gateway
    for (int j = 0; j < listAND.getLength(); j++) {
      Node scdNode = listAND.item(j);
      Element gw = (Element) scdNode;
      boolean inLane = false;
      if (flowNodes != null) {
        for (int k = 0; k < flowNodes.getLength(); k++) {
          if (flowNodes.item(k).getTextContent().equals(gw.getAttribute("id"))) {
            inLane = true;
            break;
          }
        }
      }
      if (inLane || flowNodes == null || flowNodes.getLength() < 0) {
        newId = model.getNewId();
        Gateway gateway = new Gateway(newId, "", lane, pool, GatewayType.AND);
        gateway.addBPMNId(gw.getAttribute("id"));
        model.addGateway(gateway);
        this.transformedElemsRev.put(model.getNewId(), gw.getAttribute("id"));
      }
    }

    // XOR-Gateway
    for (int j = 0; j < listXOR.getLength(); j++) {
      Node scdNode = listXOR.item(j);
      Element gw = (Element) scdNode;
      boolean inLane = false;
      if (flowNodes != null) {
        for (int k = 0; k < flowNodes.getLength(); k++) {
          if (flowNodes.item(k).getTextContent().equals(gw.getAttribute("id"))) {
            inLane = true;
            break;
          }
        }
      }
      if (inLane || flowNodes == null || flowNodes.getLength() < 0) {
        newId = model.getNewId();
        Gateway gateway = new Gateway(newId, "", lane, pool, GatewayType.XOR);
        gateway.addBPMNId(gw.getAttribute("id"));
        model.addGateway(gateway);
        this.transformedElemsRev.put(model.getNewId(), gw.getAttribute("id"));
      }
    }
  }

  /** Method extracts all lanes out of the document xml string*/
  private void extractLane(Document doc, ProcessModel model, Element poolElement, Pool pool) {
    // Create a list of all lanes within the pool
    NodeList lanes = poolElement.getElementsByTagName("bpmn:lane");

    // create the individual lanes, insert them into model and transformedElemsRev, call the remaining extract methods
    for (int j = 0; j < lanes.getLength(); j++) {
      Element laneElement = (Element) lanes.item(j);
      Lane lane = new Lane(laneElement.getAttribute("name"), pool.getName());
      lane.addBPMNId(laneElement.getAttribute("id"));
      model.addLane(laneElement.getAttribute("name"));
      this.transformedElemsRev.put(model.getNewId(), laneElement.getAttribute("id"));
      extractActivity(doc, model, poolElement, pool, laneElement, lane);
      extractEvents(model, poolElement, pool, laneElement, lane);
      extractGateways(model, poolElement, pool, laneElement, lane);
    }
  }

  /** Method extracts pools out of the document xml strings*/
  private void extractPool(Document doc, ProcessModel model) {
    NodeList pools = doc.getElementsByTagName("bpmn:process");

    NodeList participants = doc.getElementsByTagName("bpmn:participant");

    for (int i = 0; i < pools.getLength(); i++) {
      Element poolElement = (Element) pools.item(i);
      NodeList lanes = poolElement.getElementsByTagName("bpmn:laneSet");
      Pool pool = new Pool(poolElement.getAttribute("id"));
      pool.setBPMNId(pool.getName());

      for (int j = 0; j < participants.getLength(); j++){
        Element participantElement = (Element) participants.item(j);
        String participantName = participantElement.getAttribute("name");
        String participantProcessRef = participantElement.getAttribute("processRef");

        if (participantProcessRef.trim().equals(pool.getBPMNId().trim())){
          pool.setName(participantName);
        }
      }



      model.addPool(poolElement.getAttribute("id"));
      this.transformedElemsRev.put(model.getNewId(), poolElement.getAttribute("id"));
      if (lanes.getLength() == 0) {
        extractActivity(doc, model, poolElement, pool, null, null);
        extractEvents(model, poolElement, pool, null, null);
        extractGateways(model, poolElement, pool, null, null);
      } else {
        extractLane(doc, model, poolElement, pool);
      }
    }
  }

  /** Methods extracts all arcs out of document xml strings*/
  private void extractArc(Document doc, ProcessModel model) {
    NodeList sequenceList = doc.getElementsByTagName("bpmn:sequenceFlow");
    NodeList messageList = doc.getElementsByTagName("bpmn:messageFlow");

    ArrayList<Node> mergedList = new ArrayList<>();

    for (int i = 0; i < sequenceList.getLength(); i++){
      mergedList.add(sequenceList.item((i)));
    }
    for (int i = 0; i < messageList.getLength(); i++){
      mergedList.add(messageList.item((i)));
    }

    Iterator<Node> iterator1 = mergedList.iterator();
    while (iterator1.hasNext()) {
      Node fstNode = iterator1.next();
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

package de.dhbw.woped.process2text.bpmnProcessing.dataModel.jsonStructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.dhbw.woped.process2text.bpmnProcessing.dataModel.camundaStructure.*;
import org.jbpt.pm.bpmn.Bpmn;

import java.util.ArrayList;

public class CamundaMapper {

    private Doc doc;
    private Stencil bpmnDiagram;
    private Stencil pool;
    private Stencil lane;
    private Stencil startEvent;
    private Stencil endEvent;
    private Stencil activity;
    private Stencil gatewayAnd;
    private Stencil gatewayXor;
    private Stencil sequenceFlow;
    private Root bpmn;
    private ArrayList<ElementLevel> existingElements;

    public CamundaMapper() {
        this.existingElements = new ArrayList<>();
        doc = new Doc();
        bpmnDiagram = new Stencil();
        bpmnDiagram.id = "BPMNDiagram";
        pool = new Stencil();
        pool.id = "Pool";
        lane = new Stencil();
        lane.id = "Lane";
        startEvent = new Stencil();
        startEvent.id = "StartNoneEvent";
        endEvent = new Stencil();
        endEvent.id = "EndNoneEvent";
        activity = new Stencil();
        activity.id = "Task";
        gatewayAnd = new Stencil();
        gatewayAnd.id = "ParallelGateway";
        gatewayXor = new Stencil();
        gatewayXor.id = "Exclusive_Databased_Gateway";
        sequenceFlow = new Stencil();
        sequenceFlow.id = "SequenceFlow";
    }

    /**
     * Builds Doc entity from Camunda JSON
     * @param json
     * @return Doc
     */
    public Doc buildDoc(String json) {
        ObjectMapper objectMapper = new ObjectMapper().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);;
        try {
            bpmn = objectMapper.readValue(json, Root.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        doc.setResourceId(bpmn.bpmnDefinitions.bpmndiBPMNDiagram.id);
        doc.setStencil(this.bpmnDiagram);
        ArrayList<PoolLevel> pools = new ArrayList<>();

        for (BpmnParticipant x : bpmn.bpmnDefinitions.bpmnCollaboration.bpmnParticipant) {
            PoolLevel tmp = new PoolLevel();
            tmp.setResourceId(x.id);
            tmp.setStencil(this.pool);
            tmp.setProps(setPoolProperties(x.name));
            tmp.setChildShapes(buildLanes(x));
            pools.add(tmp);
        }
        return doc;
    }

    private PoolProperties setPoolProperties(String name) {
        PoolProperties props = new PoolProperties();
        props.setName(name);
        return props;
    }

    private LaneProperties setLaneProperties(String name) {
        LaneProperties props = new LaneProperties();
        props.setName(name);
        return props;
    }

    private ElementProperties setElementProperties(String name) {
        ElementProperties props = new ElementProperties();
        props.setName(name);
        return props;
    }

    private ArrayList<LaneLevel> buildLanes(BpmnParticipant participant) {
        ArrayList<LaneLevel> lanes = new ArrayList<>();
        BpmnProcess process = null;
        for (BpmnProcess p : bpmn.bpmnDefinitions.bpmnProcess){
            if(p.id.equals(participant.processRef)) {
                process = p;
                break;
            }
        }
        if(process.bpmnLaneSet != null) {
            for (BpmnLane x : process.bpmnLaneSet.bpmnLane) {
                LaneLevel tmp = new LaneLevel();
                tmp.setResourceId(x.id);
                tmp.setStencil(this.lane);
                tmp.setProps(setLaneProperties(x.name));
                tmp.setChildShapes(getElements(x, process));
                lanes.add(tmp);
            }
        }
        return lanes;
    }

    private ArrayList<ElementLevel> getElements(BpmnLane lane, BpmnProcess process) {
        ArrayList<ElementLevel> elements = new ArrayList<>();
        String[] flowNodeRef = lane.bpmnFlowNodeRef.toString().split(",");
        for (String x : flowNodeRef) {
            ElementLevel tmp = null;
            if (!elements.isEmpty()) {
                tmp = searchForExistingElementObjects(elements, process.bpmnStartEvent.bpmnOutgoing);
            }
            if (tmp != null) {

            } else {
                tmp = new ElementLevel();
                if (x.contains("StartEvent")) {
                    tmp.setResourceId(process.bpmnStartEvent.id);
                    tmp.setStencil(startEvent);
                    tmp.setProps(setElementProperties(""));

                    ArrayList<ElementLevel> outgoing = new ArrayList<>(); //Outgoing needs to be done recursively AND referenced. Look for references Objects in this loop to save Ram
                    ElementLevel searchOG = searchForExistingElementObjects(new ArrayList<ElementLevel>(), process.bpmnStartEvent.bpmnOutgoing);
                    if (searchOG == null) {
                        ElementLevel og = new ElementLevel();
                        og.setResourceId(process.bpmnStartEvent.bpmnOutgoing);
                        if (!elements.isEmpty()) {
                            finishElement(og);
                        }
                        outgoing.add(og);
                    } else {
                        outgoing.add(searchOG);
                    }
                    tmp.setOutgoing(outgoing);
                } else if (x.contains("EndEvent")) {
                    tmp.setResourceId(process.bpmnEndEvent.id);
                    tmp.setStencil(endEvent);
                    tmp.setProps(setElementProperties(""));
                } else if (x.contains("Activity")) {
                    BpmnTask activity = process.bpmnTask.stream().filter(BpmnTask -> x.contains(BpmnTask.id)).findAny().orElse(null);
                    tmp.setResourceId(activity.id);
                    tmp.setStencil(this.activity);
                    tmp.setProps(setElementProperties(activity.name));
                    ArrayList<ElementLevel> outgoing = new ArrayList<>();
                    ElementLevel searchOG = searchForExistingElementObjects(new ArrayList<ElementLevel>(), activity.bpmnOutgoing);
                    if (searchOG == null) {
                        ElementLevel og = new ElementLevel();
                        og.setResourceId(process.bpmnStartEvent.bpmnOutgoing);
                        if (!elements.isEmpty()) {
                            finishElement(og);
                        }
                        outgoing.add(og);
                    } else {
                        outgoing.add(searchOG);
                    }
                    tmp.setOutgoing(outgoing);
                } else if (x.contains("Gateway")) {
                    BpmnExclusiveGateway gw = process.bpmnExclusiveGateway.stream().filter(BpmnExclusiveGateway -> x.contains(BpmnExclusiveGateway.id)).findAny().orElse(null);
                    ArrayList<ElementLevel> outgoing = new ArrayList<>();
                    if (gw != null) {
                        tmp.setResourceId(gw.id);
                        tmp.setStencil(gatewayXor);
                        for (String gwOg : gw.bpmnOutgoing) {
                            ElementLevel searchOG = searchForExistingElementObjects(new ArrayList<ElementLevel>(), gwOg);
                            if (searchOG == null && !outgoing.isEmpty()) {
                                searchOG = searchForExistingElementObjects(outgoing, gwOg);
                            }
                            if (searchOG == null) {
                                this.existingElements.add(tmp);
                                ElementLevel og = new ElementLevel();
                                og.setResourceId(gwOg);
                                if (!elements.isEmpty()) {
                                    finishElement(og);
                                }
                                outgoing.add(og);
                                this.existingElements.add(og);
                            } else {
                                outgoing.add(searchOG);
                            }
                        }
                    } else {
                        BpmnParallelGateway pgw = process.bpmnParallelGateway.stream().filter(bpmnParallelGateway -> x.contains(bpmnParallelGateway.id)).findAny().orElse(null);
                        tmp.setResourceId(pgw.id);
                        tmp.setStencil(gatewayAnd);
                        for (String gwOg : pgw.bpmnOutgoing) {
                            ElementLevel searchOG = searchForExistingElementObjects(new ArrayList<ElementLevel>(), gwOg);
                            if (searchOG == null && !outgoing.isEmpty()) {
                                searchOG = searchForExistingElementObjects(outgoing, gwOg);
                            }
                            if (searchOG == null) {
                                ElementLevel og = new ElementLevel();
                                og.setResourceId(gwOg);
                                if (!elements.isEmpty()) {
                                    finishElement(og);
                                }
                                outgoing.add(og);
                                this.existingElements.add(og);
                            } else {
                                outgoing.add(searchOG);
                            }
                        }
                    }
                    tmp.setProps(setElementProperties(""));

                } else if (x.contains("Flow")) {
                    BpmnSequenceFlow flow = process.bpmnSequenceFlow.stream().filter(BpmnSequenceFlow -> x.contains(BpmnSequenceFlow.id)).findAny().orElse(null);
                    tmp.setResourceId(flow.id);
                    tmp.setStencil(sequenceFlow);
                    if (flow.name != null) {
                        tmp.setProps(setElementProperties(flow.name));
                    } else {
                        tmp.setProps(setElementProperties(""));
                    }
                    ArrayList<ElementLevel> outgoing = new ArrayList<>();
                    ElementLevel searchOG = searchForExistingElementObjects(new ArrayList<ElementLevel>(), flow.targetRef);
                    if (searchOG == null) {
                        ElementLevel og = new ElementLevel();
                        og.setResourceId(flow.targetRef);
                        if (!elements.isEmpty()) {
                            finishElement(og);
                        }
                        outgoing.add(og);
                    } else {
                        outgoing.add(searchOG);
                    }
                    tmp.setOutgoing(outgoing);
                }
            }
            elements.add(tmp);
            if (!this.existingElements.contains(tmp)) {
                this.existingElements.add(tmp);
            }
        }
        return elements;
    }

    private ElementLevel searchForExistingElementObjects(ArrayList<ElementLevel> list, String og) {
        if(list == null){
            list = this.existingElements;
        }
        for (ElementLevel x : list) {
            if (x.resourceId == og) {
                return x;
            }
            ElementLevel tmp = searchForExistingElementObjects(x.outgoing, og);
            if (tmp != null) {
                return tmp;
            }
        }
        return null;
    }

    private void finishElement(ElementLevel element) {
        String x = element.resourceId;
        if (x.contains("EndEvent")) {
            element.setStencil(endEvent);
            element.setProps(setElementProperties(""));
        } else if (x.contains("Activity")) {
            BpmnTask t = null;
            element.setStencil(this.activity);
            for (BpmnProcess p : bpmn.bpmnDefinitions.bpmnProcess) {
                for (BpmnTask y : p.bpmnTask) {
                    if (x.equals(y.id)) {
                        t = y;
                        break;
                    }
                }
            }
            element.setProps(setElementProperties(t.name));
            ArrayList<ElementLevel> outgoing = new ArrayList<>();
            ElementLevel searchOG = searchForExistingElementObjects(new ArrayList<ElementLevel>(), t.bpmnOutgoing);
            if (searchOG == null) {
                ElementLevel og = new ElementLevel();
                og.setResourceId(t.bpmnOutgoing);
                finishElement(og);
                outgoing.add(og);
            } else {
                outgoing.add(searchOG);
            }
            element.setOutgoing(outgoing);
        } else if (x.contains("Gateway")) {
            BpmnExclusiveGateway gw = null;
            for (BpmnProcess p : bpmn.bpmnDefinitions.bpmnProcess) {
                gw = p.bpmnExclusiveGateway.stream().filter(BpmnExclusiveGateway -> x.contains(BpmnExclusiveGateway.id)).findAny().orElse(null);
                if (gw != null)
                    break;
            }
            ArrayList<ElementLevel> outgoing = new ArrayList<>();
            if (gw != null) {
                element.setStencil(gatewayXor);
                for (String gwOg : gw.bpmnOutgoing) {
                    ElementLevel searchOG = searchForExistingElementObjects(new ArrayList<ElementLevel>(), gwOg);
                    if (searchOG == null && !outgoing.isEmpty()) {
                        searchOG = searchForExistingElementObjects(outgoing, gwOg);
                    }
                    if (searchOG == null) {
                        this.existingElements.add(element);
                        ElementLevel og = new ElementLevel();
                        og.setResourceId(gwOg);
                        finishElement(og);
                        outgoing.add(og);
                        this.existingElements.add(og);
                    } else {
                        outgoing.add(searchOG);
                    }
                }
            } else {
                BpmnParallelGateway pgw = null;
                for (BpmnProcess p : bpmn.bpmnDefinitions.bpmnProcess) {
                    pgw = p.bpmnParallelGateway.stream().filter(bpmnParallelGateway -> x.contains(bpmnParallelGateway.id)).findAny().orElse(null);
                    if (pgw != null)
                        break;
                }
                element.setStencil(gatewayAnd);
                for (String gwOg : pgw.bpmnOutgoing) {
                    ElementLevel searchOG = searchForExistingElementObjects(new ArrayList<ElementLevel>(), gwOg);
                    if (searchOG == null && !outgoing.isEmpty()) {
                        searchOG = searchForExistingElementObjects(outgoing, gwOg);
                    }
                    if (searchOG == null) {
                        ElementLevel og = new ElementLevel();
                        og.setResourceId(gwOg);
                        finishElement(og);
                        outgoing.add(og);
                        this.existingElements.add(og);
                    } else {
                        outgoing.add(searchOG);
                    }
                }
            }
            element.setProps(setElementProperties(""));

        } else if (x.contains("Flow")) {
            BpmnSequenceFlow flow = null;
            for (BpmnProcess p : bpmn.bpmnDefinitions.bpmnProcess) {
                flow = p.bpmnSequenceFlow.stream().filter(BpmnSequenceFlow -> x.contains(BpmnSequenceFlow.id)).findAny().orElse(null);
                if (flow != null)
                    break;
            }
            element.setStencil(sequenceFlow);
            if (flow.name != null) {
                element.setProps(setElementProperties(flow.name));
            } else {
                element.setProps(setElementProperties(""));
            }
            ArrayList<ElementLevel> outgoing = new ArrayList<>();
            ElementLevel searchOG = searchForExistingElementObjects(new ArrayList<ElementLevel>(), flow.targetRef);
            if (searchOG == null) {
                ElementLevel og = new ElementLevel();
                og.setResourceId(flow.targetRef);
                finishElement(og);
                outgoing.add(og);
            } else {
                outgoing.add(searchOG);
            }
            element.setOutgoing(outgoing);
        }
        if (!this.existingElements.contains(element)) {
            this.existingElements.add(element);
        }
    }
}


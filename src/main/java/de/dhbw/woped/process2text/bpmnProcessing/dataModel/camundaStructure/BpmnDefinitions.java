package de.dhbw.woped.process2text.bpmnProcessing.dataModel.camundaStructure;
import com.fasterxml.jackson.annotation.JsonProperty; 
import java.util.List; 
public class BpmnDefinitions{
    @JsonProperty("xmlns:bpmndi") 
    public String xmlnsBpmndi;
    @JsonProperty("xmlns:di") 
    public String xmlnsDi;
    @JsonProperty("xmlns:modeler") 
    public String xmlnsModeler;
    public String exporterVersion;
    @JsonProperty("modeler:executionPlatformVersion") 
    public String modelerExecutionPlatformVersion;
    @JsonProperty("modeler:executionPlatform") 
    public String modelerExecutionPlatform;
    @JsonProperty("bpmn:process") 
    public List<BpmnProcess> bpmnProcess;
    public String exporter;
    @JsonProperty("xmlns:bpmn") 
    public String xmlnsBpmn;
    public String targetNamespace;
    @JsonProperty("bpmn:collaboration") 
    public BpmnCollaboration bpmnCollaboration;
    public String id;
    @JsonProperty("bpmndi:BPMNDiagram") 
    public BpmndiBPMNDiagram bpmndiBPMNDiagram;
    @JsonProperty("xmlns:dc") 
    public String xmlnsDc;
}

package de.dhbw.woped.process2text.bpmnProcessing.dataModel.camundaStructure;
import com.fasterxml.jackson.annotation.JsonProperty; 
public class Root{
    @JsonProperty("bpmn:definitions") 
    public BpmnDefinitions bpmnDefinitions;
}

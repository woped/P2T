package de.dhbw.woped.process2text.bpmnProcessing.dataModel.camundaStructure;
import com.fasterxml.jackson.annotation.JsonProperty; 
public class BpmndiBPMNLabel{
    @JsonProperty("dc:Bounds") 
    public DcBounds dcBounds;
}

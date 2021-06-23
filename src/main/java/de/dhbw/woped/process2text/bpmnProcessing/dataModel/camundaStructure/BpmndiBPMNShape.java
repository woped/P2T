package de.dhbw.woped.process2text.bpmnProcessing.dataModel.camundaStructure;
import com.fasterxml.jackson.annotation.JsonProperty; 
public class BpmndiBPMNShape{
    public String bpmnElement;
    public String id;
    @JsonProperty("dc:Bounds") 
    public DcBounds dcBounds;
    public boolean isHorizontal;
    public boolean isMarkerVisible;
}

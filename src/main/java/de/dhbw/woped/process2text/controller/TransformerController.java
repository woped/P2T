package de.dhbw.woped.process2text.controller;

import de.dhbw.woped.process2text.service.TransformerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransformerController {

    @Autowired
    private TransformerService transformerService;

    @PostMapping("/transform")
    public String transform(
            @RequestParam("direction") String direction,
            @RequestBody String bpmnXml) {
        return transformerService.callTransformer(direction, bpmnXml);
    }
}
package de.dhbw.woped.process2text.P2TWebservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

// WebServlet value = URL to call the Webservice from
@RestController
@Slf4j
public class P2TServlet extends HttpServlet{
    private String p2tText = "";

    // Call P2TController's generateText Method
    public String createText(String text) {
        P2TController control = new P2TController();
        //String contextPath = getServletContext().getRealPath("/WEB-INF/classes");
        return control.generateText(text);
    }

    @RequestMapping(value = "/generateText", method = RequestMethod.POST, consumes = "text/plain", produces = "text/plain")
    protected String doPost(@RequestBody String body) {
        String text = body;
        //System.out.println(body);
        p2tText = createText(text);
        return p2tText;
    }
}
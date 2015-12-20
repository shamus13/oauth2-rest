package dk.grixie.oauth2.rest.rest;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Message {
    private String message;

    public Message() {
        this.message = "";
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

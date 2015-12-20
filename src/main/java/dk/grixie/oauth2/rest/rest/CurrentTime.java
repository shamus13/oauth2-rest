package dk.grixie.oauth2.rest.rest;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CurrentTime {
    private String time;

    public CurrentTime() {
    }

    public CurrentTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}

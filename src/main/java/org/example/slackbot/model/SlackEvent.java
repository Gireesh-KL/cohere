package org.example.slackbot.model;

import java.util.Map;

public class SlackEvent {
    private String type;
    private Map<String, Object> event;
    private String challenge;

    // Getters and setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Map<String, Object> getEvent() { return event; }
    public void setEvent(Map<String, Object> event) { this.event = event; }

    public String getChallenge() {
        return challenge;
    }
    public void setChallenge(String challenge) { this.challenge = challenge; }

    @Override
    public String toString() {
        return "SlackEvent{" +
                "type='" + type + '\'' +
                ", event=" + event +
                ", challenge='" + challenge + '\'' +
                '}';
    }
}



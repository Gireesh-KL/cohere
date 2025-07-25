package org.example.slackbot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SlackEvent {
    private String type;
    private InnerEvent event;
    private String challenge;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InnerEvent {
        private String user;
        private String type;
        private String text;
        private String channel;
        private String bot_id;
        private String subtype;
        private List<SlackFile> files;

        public boolean isFromBot() {
            return (bot_id != null && !bot_id.isEmpty()) ||
                    (subtype != null && subtype.equals("bot_message")) ||
                    (user != null && user.equals("U0967ACAU2E"));  // Replace with config.getSlackBotUserId()
        }

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class SlackFile {
            private String id;
            private String name;
            private String url_private;
            private String mimetype;
        }

        public String getSubtype() {
            return subtype;
        }

        public void setSubtype(String subtype) {
            this.subtype = subtype;
        }


        public String getBot_id() {
            return bot_id;
        }

        public void setBot_id(String bot_id) {
            this.bot_id = bot_id;
        }

        public String getUser() { return user; }
        public void setUser(String user) { this.user = user; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public String getChannel() { return channel; }
        public void setChannel(String channel) { this.channel = channel; }

        @Override
        public String toString() {
            return "InnerEvent{" +
                    "user='" + user + '\'' +
                    ", type='" + type + '\'' +
                    ", text='" + text + '\'' +
                    ", channel='" + channel + '\'' +
                    '}';
        }
    }

    // Getters and setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public InnerEvent getEvent() { return event; }
    public void setEvent(InnerEvent event) { this.event = event; }

    public String getChallenge() { return challenge; }
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

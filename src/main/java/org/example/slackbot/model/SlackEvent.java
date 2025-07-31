package org.example.slackbot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SlackEvent {
    private String type;
    private InnerEvent event;
    private String challenge;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InnerEvent {

        public List<Map<String, Object>> getFiles() { return files; }

        public void setFiles(List<Map<String, Object>> files) {
            this.files = files;
        }

        private String user;
        private String type;
        private String text;
        private String channel;
        private String bot_id;
        private String subtype;
        private List<Map<String, Object>> files;
        private String ts;
        private String thread_ts;

        public String getTs() { return ts; }
        public void setTs(String ts) { this.ts = ts; }

        public String getThread_ts() { return thread_ts; }
        public void setThread_ts(String thread_ts) { this.thread_ts = thread_ts; }



        public boolean isFromBot() {
            return (bot_id != null && !bot_id.isEmpty()) ||
                    (subtype != null && "bot_message".equalsIgnoreCase(subtype)) ||
                    (user != null && user.equals("U0967ACAU2E"));  // Replace with config.getSlackBotUserId()
        }

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class SlackFile {
            private String id;
            private String name;
            private String url_private;
            private String mimetype;

            public String getId() { return id; }
            public void setId(String id) { this.id = id; }

            public String getName() { return name; }
            public void setName(String name) { this.name = name; }

            public String getUrl_private() { return url_private; }
            public void setUrl_private(String url_private) { this.url_private = url_private; }

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

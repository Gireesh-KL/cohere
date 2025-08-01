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

        private String user;
        private String type;
        private String text;
        private String channel;
        private String bot_id;
        private String subtype;
        private List<Map<String, Object>> files;
        private String ts;
        private String thread_ts;


        public boolean isFromBot() {
            return (bot_id != null && !bot_id.isEmpty()) ||
                    ("bot_message".equalsIgnoreCase(subtype));
        }

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

    @Override
    public String toString() {
        return "SlackEvent{" +
                "type='" + type + '\'' +
                ", event=" + event +
                ", challenge='" + challenge + '\'' +
                '}';
    }
}

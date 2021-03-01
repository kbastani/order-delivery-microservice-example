package demo.event;

import org.springframework.messaging.MessageChannel;

public class EventSource {

    private MessageChannel channel;

    public EventSource(MessageChannel channel) {
        this.channel = channel;
    }

    public MessageChannel getChannel() {
        return channel;
    }
}

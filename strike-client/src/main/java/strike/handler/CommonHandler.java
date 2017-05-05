package strike.handler;

import com.google.common.eventbus.EventBus;
import org.json.simple.JSONObject;
import strike.model.Message;
import strike.service.ClientState;
import strike.service.ConnectionService;
import strike.service.JSONMessageBuilder;

import java.util.concurrent.BlockingQueue;

public class CommonHandler {

    protected JSONObject jsonMessage;

    protected BlockingQueue<Message> messageQueue;
    protected EventBus eventBus;

    protected ClientState clientState = ClientState.getInstance();
    protected JSONMessageBuilder messageBuilder = JSONMessageBuilder.getInstance();

    public CommonHandler(JSONObject jsonMessage) {
        this.jsonMessage = jsonMessage;
        this.messageQueue = ConnectionService.getInstance().getConnection().getMessageQueue();
        this.eventBus = ConnectionService.getInstance().getEventBus();
    }
}

package strike.handler.send;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import strike.common.model.Protocol;
import strike.handler.CommonHandler;
import strike.handler.IProtocolHandler;
import strike.model.ChatCommand;
import strike.model.Message;
import strike.model.event.CommandInvalidEvent;

import java.util.Arrays;

public class ChatMessageSendHandler extends CommonHandler implements IProtocolHandler {

    public ChatMessageSendHandler(JSONObject jsonMessage) {
        super(jsonMessage);
    }

    private void __send__(String msg) {
        messageQueue.add(new Message(false, msg));
    }

    private void __invalid__(String msg) {
        logger.warn(msg);
        eventBus.post(new CommandInvalidEvent(msg));
    }

    @Override
    public void handle() {

        String CMD_ESC = "#";

        String message = (String) jsonMessage.get(Protocol.content.toString());

        if (!message.startsWith(CMD_ESC)) {

            __send__(jsonMessage.toJSONString());

        } else {

            // chat commands

            String[] cmd = message.split(CMD_ESC)[1].split(" ");

            if (cmd.length == 1) {
                String action = cmd[0];
                if (action.equalsIgnoreCase(ChatCommand.list.toString())) {
                    __send__(messageBuilder.getListRequest());
                }
                else if (action.equalsIgnoreCase(ChatCommand.quit.toString()) ||
                        action.equalsIgnoreCase(ChatCommand.q.toString())) {
                    __send__(messageBuilder.getQuitRequest());
                }
                //else if (action.equalsIgnoreCase(ChatCommand.who.toString())) {
                //    //__send__(messageBuilder.getWhoRequest());
                //    __invalid__("Deprecated command: " + CMD_ESC + action);
                //}
                else if (action.equalsIgnoreCase(ChatCommand.help.toString())) {
                    //__send__(messageBuilder.getWhoRequest());
                    String helpMsg = "Commands are escaped with # hash sign. " +
                            "Available commands: " + Arrays.asList(ChatCommand.values()) +
                            "\nExample: #createroom joke";
                    eventBus.post(new CommandInvalidEvent(helpMsg));
                }
                else {
                    __invalid__("Invalid command!");
                }
            } else if (cmd.length == 2) {
                String action = cmd[0];
                String arg = cmd[1];
                if (action.equalsIgnoreCase(ChatCommand.joinroom.toString())) {
                    __send__(messageBuilder.getJoinRoomRequest(arg));
                }
                else if (action.equalsIgnoreCase(ChatCommand.createroom.toString())) {
                    __send__(messageBuilder.getCreateRoomRequest(arg));
                }
                else if (action.equalsIgnoreCase(ChatCommand.deleteroom.toString())) {
                    __send__(messageBuilder.getDeleteRoomRequest(arg));
                }
                //else if (action.equalsIgnoreCase(ChatCommand.newidentity.toString())) {
                    //__send__(messageBuilder.getNewIdentityRequest(arg).toJSONString());
                //    __invalid__("Deprecated command: " + CMD_ESC + action);
                //}
                else {
                    __invalid__("Invalid command!");
                }
            } else {
                __invalid__("Invalid command!");
            }
        }
    }

    private static final Logger logger = LogManager.getLogger(ChatMessageSendHandler.class);
}

package strike.service;

import org.json.simple.JSONObject;
import strike.common.model.Protocol;

// TODO: Potential merge with server JSONMessageBuilder
public class JSONMessageBuilder {
    private static JSONMessageBuilder instance = null;

    private JSONMessageBuilder() {
    }

    public static synchronized JSONMessageBuilder getInstance() {
        if (instance == null) instance = new JSONMessageBuilder();
        return instance;
    }

    public String createServerListMessage() {
        // {"type":"listserver"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.listserver.toString());
        return jj.toJSONString();
    }

    public JSONObject createAuthMessage(String username, String password, boolean remember) {
        // {"type" : "authenticate", "username" : "ray@example.com", "password":"cheese", "rememberme":"true"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.authenticate.toString());
        jj.put(Protocol.username.toString(), username); // define in shiro.ini
        jj.put(Protocol.password.toString(), password); // define in shiro.ini
        jj.put(Protocol.rememberme.toString(), String.format("%s", remember)); // true or false or not provide
        return jj;
    }

    public JSONObject getNewIdentityRequest(String identity) {
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.newidentity.toString());
        jj.put(Protocol.identity.toString(), identity);
        return jj;
    }

    public JSONObject whoMessage() {
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.who.toString());
        return jj;
    }

    public JSONObject helpMessage() {
        // {"type" : "message", "content" : "#help"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.message.toString());
        jj.put(Protocol.content.toString(), "#help");
        return jj;
    }

    public JSONObject chatMessage(String chat) {
        // {"type" : "message", "content" : "Hi there!"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.message.toString());
        jj.put(Protocol.content.toString(), chat);
        return jj;
    }

    public String getListRequest() {
        JSONObject list = new JSONObject();
        list.put(Protocol.type.toString(), Protocol.list.toString());
        return list.toJSONString();
    }

    public String getQuitRequest() {
        JSONObject quit = new JSONObject();
        quit.put(Protocol.type.toString(), Protocol.quit.toString());
        return quit.toJSONString();
    }

    public String getWhoRequest() {
        JSONObject who = new JSONObject();
        who.put(Protocol.type.toString(), Protocol.who.toString());
        return who.toJSONString();
    }

    public String getJoinRoomRequest(String roomId) {
        JSONObject join = new JSONObject();
        join.put(Protocol.type.toString(), Protocol.join.toString());
        join.put(Protocol.roomid.toString(), roomId);
        return join.toJSONString();
    }

    public String getCreateRoomRequest(String roomId) {
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.createroom.toString());
        jj.put(Protocol.roomid.toString(), roomId);
        return jj.toJSONString();
    }

    public String getDeleteRoomRequest(String roomId) {
        JSONObject delete = new JSONObject();
        delete.put(Protocol.type.toString(), Protocol.deleteroom.toString());
        delete.put(Protocol.roomid.toString(), roomId);
        return delete.toJSONString();
    }

    public JSONObject getMoveJoinRequest(String identity, String former, String roomId, String username, String sessionId, String password) {
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.movejoin.toString());
        jj.put(Protocol.identity.toString(), identity);
        jj.put(Protocol.former.toString(), former);
        jj.put(Protocol.roomid.toString(), roomId);
        jj.put(Protocol.username.toString(), username);
        jj.put(Protocol.sessionid.toString(), sessionId);
        jj.put(Protocol.password.toString(), password);
        return jj;
    }
}

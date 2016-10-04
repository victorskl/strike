package strike.service;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import strike.model.ChatRoomInfo;
import strike.model.LocalChatRoomInfo;
import strike.model.Protocol;
import strike.model.ServerInfo;

import java.util.stream.Collectors;

public class JSONMessageBuilder {

    private static JSONMessageBuilder instance = null;
    private JSONMessageBuilder() {}
    public static synchronized JSONMessageBuilder getInstance() {
        if (instance == null) instance = new JSONMessageBuilder();
        return instance;
    }

    private ServerState serverState = ServerState.getInstance();
    private ServerInfo serverInfo = serverState.getServerInfo();

    public String serverChange(String approved, String serverId) {
        // {"type" : "serverchange", "approved" : "true", "serverid" : "s2"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.serverchange.toString());
        jj.put(Protocol.approved.toString(), approved);
        jj.put(Protocol.serverid.toString(), serverId);
        return jj.toJSONString();
    }

    public String route(String joiningRoomId, String host, Integer port) {
        // {"type" : "route", "roomid" : "jokes", "host" : "122.134.2.4", "port" : "4445"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.route.toString());
        jj.put(Protocol.roomid.toString(), joiningRoomId);
        jj.put(Protocol.host.toString(), host);
        jj.put(Protocol.port.toString(), port.toString());
        return jj.toJSONString();
    }

    public String message(String identity, String content) {
        // {"type" : "message", "identity" : "Adel", "content" : "Hi there!"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.message.toString());
        jj.put(Protocol.identity.toString(), identity);
        jj.put(Protocol.content.toString(), content);
        return jj.toJSONString();
    }

    public String deleteRoom(String roomId, String approved) {
        // {"type" : "deleteroom", "roomid" : "jokes", "approved" : "true"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.deleteroom.toString());
        jj.put(Protocol.roomid.toString(), roomId);
        jj.put(Protocol.approved.toString(), approved);
        return jj.toJSONString();
    }

    public String deleteRoomPeers(String roomId) {
        // {"type" : "deleteroom", "serverid" : "s1", "roomid" : "jokes"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.deleteroom.toString());
        jj.put(Protocol.serverid.toString(), serverInfo.getServerId());
        jj.put(Protocol.roomid.toString(), roomId);
        return jj.toJSONString();
    }

    public String releaseRoom(String roomId, String approved) {
        // "type" : "releaseroomid", "serverid" : "s1", "roomid" : "jokes", "approved":"false"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.releaseroomid.toString());
        jj.put(Protocol.serverid.toString(), serverInfo.getServerId());
        jj.put(Protocol.roomid.toString(), roomId);
        jj.put(Protocol.approved.toString(), approved);
        return jj.toJSONString();
    }

    public String lockRoom(String roomId) {
        //{"type" : "lockroomid", "serverid" : "s1", "roomid" : "jokes"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.lockroomid.toString());
        jj.put(Protocol.serverid.toString(), serverInfo.getServerId());
        jj.put(Protocol.roomid.toString(), roomId);
        return jj.toJSONString();
    }

    public String createRoomResp(String roomId, String approved) {
        //{"type" : "createroom", "roomid" : "jokes", "approved" : "false"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.createroom.toString());
        jj.put(Protocol.roomid.toString(), roomId);
        jj.put(Protocol.approved.toString(), approved);
        return jj.toJSONString();
    }

    public String whoByRoom(String room) {
        JSONObject jj = new JSONObject();
        //{ "type" : "roomcontents", "roomid" : "jokes", "identities" : ["Adel","Chenhao","Maria"], "owner" : "Adel" }
        jj.put(Protocol.type.toString(), Protocol.roomcontents.toString());
        jj.put(Protocol.roomid.toString(), room);
        LocalChatRoomInfo localChatRoomInfo = serverState.getLocalChatRooms().get(room);
        JSONArray ja = new JSONArray();
        ja.addAll(localChatRoomInfo.getMembers());
        jj.put(Protocol.identities.toString(), ja);
        jj.put(Protocol.owner.toString(), localChatRoomInfo.getOwner());
        return jj.toJSONString();
    }

    public String listRooms() {
        //{ "type" : "roomlist", "rooms" : ["MainHall-s1", "MainHall-s2", "jokes"] }
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.roomlist.toString());

        JSONArray ja = serverState.getLocalChatRooms().values().stream()
                .map(ChatRoomInfo::getChatRoomId)
                .collect(Collectors.toCollection(JSONArray::new));

        ja.addAll(serverState.getRemoteChatRooms().values().stream()
                .map(ChatRoomInfo::getChatRoomId)
                .collect(Collectors.toList()));

        jj.put(Protocol.rooms.toString(), ja);

        return jj.toJSONString();
    }

    public String releaseIdentity(String userId) {
        //{"type" : "releaseidentity", "serverid" : "s1", "identity" : "Adel"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.releaseidentity.toString());
        jj.put(Protocol.serverid.toString(), serverInfo.getServerId());
        jj.put(Protocol.identity.toString(), userId);
        return jj.toJSONString();
    }

    public String lockIdentity(String userId) {
        // send peer server {"type" : "lockidentity", "serverid" : "s1", "identity" : "Adel"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.lockidentity.toString());
        jj.put(Protocol.serverid.toString(), serverInfo.getServerId());
        jj.put(Protocol.identity.toString(), userId);
        return jj.toJSONString();
    }

    public String newIdentityResp(String approve) {
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.newidentity.toString());
        jj.put(Protocol.approved.toString(), approve);
        return jj.toJSONString();
    }

    public String roomChange(String former, String roomId, String identity) {
        // {"type" : "roomchange", "identity" : "Maria", "former" : "jokes", "roomid" : "jokes"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.roomchange.toString());
        jj.put(Protocol.identity.toString(), identity);
        jj.put(Protocol.former.toString(), former);
        jj.put(Protocol.roomid.toString(), roomId);
        return jj.toJSONString();
    }

    // Management Protocols

    public String lockIdentity(String serverId, String userId, String locked) {
        // {"type" : "lockidentity", "serverid" : "s2", "identity" : "Adel", "locked" : "true"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.lockidentity.toString());
        jj.put(Protocol.serverid.toString(), serverId);
        jj.put(Protocol.identity.toString(), userId);
        jj.put(Protocol.locked.toString(), locked);
        return jj.toJSONString();
    }

    public String lockRoom(String serverId, String roomId, String locked) {
        //{"type" : "lockroomid", "serverid" : "s2", "roomid" : "jokes", "locked" : "true"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.lockroomid.toString());
        jj.put(Protocol.serverid.toString(), serverId);
        jj.put(Protocol.roomid.toString(), roomId);
        jj.put(Protocol.locked.toString(), locked);
        return jj.toJSONString();
    }

    public String listRoomsClient() {
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.list.toString());
        return jj.toJSONString();
    }
}

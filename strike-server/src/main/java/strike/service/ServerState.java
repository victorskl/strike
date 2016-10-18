package strike.service;

import org.apache.commons.lang3.StringUtils;
import strike.model.*;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerState {

    private static ServerState instance;

    private ConcurrentMap<String, Date> aliveMap;
    private ConcurrentMap<String, UserSession> localUserSessions;
    private ConcurrentMap<String, RemoteUserSession> remoteUserSessions;
    private ConcurrentMap<String, LocalChatRoomInfo> localChatRooms;
    private ConcurrentMap<String, RemoteChatRoomInfo> remoteChatRooms;
    private ConcurrentMap<String, UserInfo> connectedClients;

    private Set<String> lockedIdentities;
    private Set<String> lockedRoomIdentities;

    private List<ServerInfo> serverInfoList;
    private ServerInfo serverInfo;

    private AtomicBoolean stopRunning = new AtomicBoolean(false);

    private ServerState() {
        aliveMap = new ConcurrentHashMap<>();
        localUserSessions = new ConcurrentHashMap<>();
        remoteUserSessions = new ConcurrentHashMap<>();
        connectedClients = new ConcurrentHashMap<>();
        localChatRooms = new ConcurrentHashMap<>();
        remoteChatRooms = new ConcurrentHashMap<>();
        lockedIdentities = new HashSet<>();
        lockedRoomIdentities = new HashSet<>();
    }

    public static synchronized ServerState getInstance() {
        if (instance == null) {
            instance = new ServerState();
        }
        return instance;
    }

    public void initServerState(String serverId) {
        serverInfo = serverInfoList.stream()
                .filter(e -> e.getServerId().equalsIgnoreCase(serverId))
                .findFirst()
                .get();
    }

    public synchronized ServerInfo getServerInfoById(String serverId) {
        return serverInfoList.stream()
                .filter(e -> e.getServerId().equalsIgnoreCase(serverId))
                .findFirst()
                .get();
    }

    public synchronized ServerInfo getServerInfo() {
        return serverInfo;
    }

    public synchronized List<ServerInfo> getServerInfoList() {
        return serverInfoList;
    }

    public synchronized void setServerInfoList(List<ServerInfo> serverInfoList) {
        this.serverInfoList = serverInfoList;
    }

    // thread safe

    public ConcurrentMap<String, Date> getAliveMap() {
        return aliveMap;
    }

    public ConcurrentMap<String, UserSession> getLocalUserSessions() {
        return localUserSessions;
    }

    public ConcurrentMap<String, RemoteUserSession> getRemoteUserSessions() {
        return remoteUserSessions;
    }

    public ConcurrentMap<String, UserInfo> getConnectedClients() {
        return connectedClients;
    }

    public ConcurrentMap<String, LocalChatRoomInfo> getLocalChatRooms() {
        return localChatRooms;
    }

    public ConcurrentMap<String, RemoteChatRoomInfo> getRemoteChatRooms() {
        return remoteChatRooms;
    }

    public boolean isUserExisted(String userId) {
        return connectedClients.containsKey(userId);
    }

    public boolean isRoomExistedGlobally(String roomId) {
        return localChatRooms.containsKey(roomId) || remoteChatRooms.containsKey(roomId);
    }

    public boolean isRoomExistedLocally(String roomId) {
        return localChatRooms.containsKey(roomId);
    }

    public boolean isRoomExistedRemotely(String roomId) {
        return remoteChatRooms.containsKey(roomId);
    }

    public void stopRunning(boolean state) {
        stopRunning.set(state);
    }

    public boolean isStopRunning() {
        return stopRunning.get();
    }

    // synchronized

    public synchronized void lockIdentity(String identity) {
        lockedIdentities.add(identity);
    }

    public synchronized void unlockIdentity(String identity) {
        lockedIdentities.remove(identity);
    }

    public synchronized boolean isIdentityLocked(String identity) {
        return lockedIdentities.contains(identity);
    }

    public synchronized void lockRoomIdentity(String roomId) {
        lockedRoomIdentities.add(roomId);
    }

    public synchronized void unlockRoomIdentity(String roomId) {
        lockedRoomIdentities.remove(roomId);
    }

    public synchronized boolean isRoomIdLocked(String roomId) {
        return lockedRoomIdentities.contains(roomId);
    }

    // Utilities

    public static int MIN_CHAR = 2;
    public static int MAX_CHAR = 17;

    public boolean isIdValid(String id) {
        // The identity must be
        // an alphanumeric string starting with an upper or lower case character.
        // It must be at least 3 characters and no more than 16 characters long
        int length = id.length();
        return (StringUtils.isAlphanumeric(id) && length > MIN_CHAR && length < MAX_CHAR);
    }

    public boolean isOnline(ServerInfo serverInfo) {
        boolean online = true;
        try {
            InetSocketAddress address = new InetSocketAddress(serverInfo.getAddress(), serverInfo.getManagementPort());
            final int timeOut = (int) TimeUnit.SECONDS.toMillis(5);
            SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            final SSLSocket shortKet = (SSLSocket) sslsocketfactory.createSocket();
            shortKet.connect(address, timeOut);
            shortKet.close();
        } catch (IOException e) {
            //e.printStackTrace();
            online = false;
        }
        return online;
    }
}

package strike.common.model;

public enum Protocol {
    type, newidentity, identity, lockidentity,
    serverid, locked, approved, roomchange, former, roomid, releaseidentity,
    list, quit, who, createroom, roomlist, rooms, roomcontents, identities, owner,
    lockroomid, releaseroomid, deleteroom, message, content, join, route, host, port,
    movejoin, serverchange, listserver, serverlist, servers, address,
    authenticate, username, password, rememberme, authresponse, success, reason,
    sessionid, notifyusersession, status, alive
    ;
}

package strike.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListenerAdapter;
import strike.model.UserSession;

/**
 * Experimental
 */
public class UserSessionListener extends SessionListenerAdapter {

    private ServerState serverState = ServerState.getInstance();

    @Override
    public void onExpiration(Session session) {

        String sessionId = (String) session.getId();
        UserSession userSession = serverState.getLocalUserSessions().get(sessionId);

        logger.info(String.format("User [%s] session [%s] has expired.", userSession.getUsername(), sessionId));

    }

    private static final Logger logger = LogManager.getLogger(UserSessionListener.class);
}

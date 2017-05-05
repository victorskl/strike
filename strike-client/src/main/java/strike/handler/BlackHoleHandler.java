package strike.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BlackHoleHandler implements IProtocolHandler {

    @Override
    public void handle() {
        logger.warn("BlackHoleHandler: ");
    }

    private static final Logger logger = LogManager.getLogger(BlackHoleHandler.class);
}

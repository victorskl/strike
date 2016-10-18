package strike.handler.client;

import com.google.common.base.Strings;
import org.apache.shiro.authc.*;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.subject.Subject;
import org.json.simple.JSONObject;
import strike.handler.IProtocolHandler;
import strike.model.Message;
import strike.common.model.Protocol;

public class AuthenticateProtocolHandler extends CommonHandler implements IProtocolHandler {

    public AuthenticateProtocolHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        String username = (String) jsonMessage.get(Protocol.username.toString());
        String password = (String) jsonMessage.get(Protocol.password.toString());
        String rememberme = (String) jsonMessage.get(Protocol.rememberme.toString());

        try {

            Subject currentUser = clientConnection.getCurrentUser();
            String hashPass = new Sha256Hash(password).toHex(); // TODO if we hook to MySQL, hash it
            UsernamePasswordToken token = new UsernamePasswordToken(username, password);

            if (Strings.isNullOrEmpty(rememberme)) {
                token.setRememberMe(false);
            } else {
                token.setRememberMe(Boolean.parseBoolean(rememberme));
            }

            currentUser.login(token);

            messageQueue.add(new Message(false, messageBuilder.authResponse("true", "none")));

        } catch (UnknownAccountException uae) {

            messageQueue.add(new Message(false, messageBuilder.authResponse("false", "UnknownAccountException")));

        } catch (IncorrectCredentialsException ice) {

            messageQueue.add(new Message(false, messageBuilder.authResponse("false", "IncorrectCredentialsException")));

        } catch (LockedAccountException lae) {

            messageQueue.add(new Message(false, messageBuilder.authResponse("false", "LockedAccountException")));

        } catch (AuthenticationException aex) {

            messageQueue.add(new Message(false, messageBuilder.authResponse("false", "AuthenticationException")));
        }
    }
}

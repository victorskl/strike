package strike;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import strike.model.Protocol;
import strike.service.JSONMessageBuilder;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;

public class SSLTest {

    private Configuration systemProperties;
    private SSLSocketFactory sslsocketfactory;
    JSONMessageBuilder messageBuilder = JSONMessageBuilder.getInstance();

    @Before
    public void before() {
        try {
            File systemPropertiesFile = new File("../config/system.properties");
            Configurations configs = new Configurations();
            systemProperties = configs.properties(systemPropertiesFile);
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }

        // MUST BE BEFORE SSLSocketFactory!!!
        System.setProperty("javax.net.ssl.keyStore", "../" + systemProperties.getString("keystore"));
        System.setProperty("javax.net.ssl.keyStorePassword","strikepass");
        System.setProperty("javax.net.ssl.trustStore", "../" + systemProperties.getString("keystore"));
        System.setProperty("javax.net.debug","all");

        sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
    }

    @Test @Ignore
    public void testClient() {
        SSLSocket socket = null;
        try {
            JSONObject jj = new JSONObject();
            jj.put(Protocol.type.toString(), Protocol.lockroomid.toString());
            jj.put(Protocol.serverid.toString(), "s1");
            jj.put(Protocol.roomid.toString(), "TESTROOM");

            JSONObject jj2 = new JSONObject();
            jj2.put(Protocol.type.toString(), Protocol.releaseroomid.toString());
            jj2.put(Protocol.serverid.toString(), "s1");
            jj2.put(Protocol.roomid.toString(), "TESTROOM");
            jj2.put(Protocol.approved.toString(), "true");

            String[] messages = {jj.toJSONString(), jj2.toJSONString()};
            socket = (SSLSocket) sslsocketfactory.createSocket("localhost", 5555);

            for (String s : socket.getSupportedProtocols()) {
                System.out.println(s);
            }
            System.out.println();

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            for (String message : messages) {
                writer.write(message + "\n");
                writer.flush();
                System.out.println("[S2S]Sending  : " + message);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            reader.readLine();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

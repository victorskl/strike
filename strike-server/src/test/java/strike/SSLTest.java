package strike;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import strike.common.model.Protocol;
import strike.service.JSONMessageBuilder;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SSLTest {

    private Configuration systemProperties;
    private SSLSocketFactory sslsocketfactory;
    private JSONMessageBuilder messageBuilder = JSONMessageBuilder.getInstance();
    private List<String> messages;
    private JSONParser parser;
    private SSLSocket socket = null;

    @Before
    public void before() {
        try {
            File systemPropertiesFile = new File("../config/system.properties");
            Configurations configs = new Configurations();
            systemProperties = configs.properties(systemPropertiesFile);
            parser = new JSONParser();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }

        messages = new ArrayList<>();

        // MUST BE BEFORE SSLSocketFactory!!!
        System.setProperty("javax.net.ssl.keyStore", "../" + systemProperties.getString("keystore"));
        System.setProperty("javax.net.ssl.keyStorePassword", "strikepass");
        System.setProperty("javax.net.ssl.trustStore", "../" + systemProperties.getString("keystore"));
        //System.setProperty("javax.net.debug","all");

        sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
    }

    @After
    public void after() throws IOException {
        socket.close();
    }

    @Test @Ignore
    public void testMessage() {
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.lockroomid.toString());
        jj.put(Protocol.serverid.toString(), "s1");
        jj.put(Protocol.roomid.toString(), "TESTROOM");
        messages.add(jj.toJSONString());

        JSONObject jj2 = new JSONObject();
        jj2.put(Protocol.type.toString(), Protocol.releaseroomid.toString());
        jj2.put(Protocol.serverid.toString(), "s1");
        jj2.put(Protocol.roomid.toString(), "TESTROOM");
        jj2.put(Protocol.approved.toString(), "true");
        messages.add(jj2.toJSONString());

        testSSLClient(5555);
    }

    private void makeLoginMessage() {
        // {"type" : "authenticate", "username" : "ray@example.com", "password":"cheese", "rememberme":"true"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.authenticate.toString());
        jj.put(Protocol.username.toString(), "root"); // define in shiro.ini
        jj.put(Protocol.password.toString(), "secret"); // define in shiro.ini
        jj.put(Protocol.rememberme.toString(), "false"); // true or false or not provide
        messages.add(jj.toJSONString());
    }

    @Test @Ignore
    public void loginTest() {
        makeLoginMessage();
        testSSLClient(4444);
    }

    @Test @Ignore
    public void loginFailTest() {

        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.authenticate.toString());
        jj.put(Protocol.username.toString(), "Adel");
        jj.put(Protocol.password.toString(), "xxxxx");
        jj.put(Protocol.rememberme.toString(), "false");
        messages.add(jj.toJSONString());

        testSSLClient(4444);
    }

    private void makeNewIdentityMessage() {
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.newidentity.toString());
        jj.put(Protocol.identity.toString(), "ScreenName");
        messages.add(jj.toJSONString());
    }

    @Test @Ignore
    public void loginBypassTest() {
        makeNewIdentityMessage();
        testSSLClient(4444);
    }

    @Test @Ignore
    public void loginAndNewIdentityTest() {
        makeLoginMessage();
        makeNewIdentityMessage();
        testSSLClient(4444);
    }

    private void testSSLClient(int port) {

        try {
            socket = (SSLSocket) sslsocketfactory.createSocket("localhost", port);

            for (String s : socket.getSupportedProtocols()) {
                System.out.println(s);
            }
            System.out.println();

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            for (String message : messages) {
                writer.write(message + "\n");
                writer.flush();
                System.out.println("Sending : " + message);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

            for (int i = 0; i< messages.size(); i++) {
                String resp = reader.readLine();

                try {
                    JSONObject jj = (JSONObject) parser.parse(resp);
                    System.out.println("Response : " + jj.toJSONString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            writer.close();
            reader.close();
            socket.close();

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

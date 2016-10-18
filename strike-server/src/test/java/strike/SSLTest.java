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

    private void makeLoginMessage(String username, String password) {
        // {"type" : "authenticate", "username" : "ray@example.com", "password":"cheese", "rememberme":"true"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.authenticate.toString());
        jj.put(Protocol.username.toString(), username); // define in shiro.ini
        jj.put(Protocol.password.toString(), password); // define in shiro.ini
        jj.put(Protocol.rememberme.toString(), "false"); // true or false or not provide
        messages.add(jj.toJSONString());
    }

    @Test @Ignore
    public void rootLoginTest() {
        makeLoginMessage("root", "secret");
        testSSLClient(4444);
    }

    @Test @Ignore
    public void guestLoginTest() {
        makeLoginMessage("guest", "guest");
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

    private void makeNewIdentityMessage(String screenName) {
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.newidentity.toString());
        jj.put(Protocol.identity.toString(), screenName);
        messages.add(jj.toJSONString());
    }

    @Test @Ignore
    public void loginBypassTest() {
        makeNewIdentityMessage("Hacker");
        testSSLClient(4444);
    }

    @Test @Ignore
    public void rootLoginAndNewIdentityTest() {
        makeLoginMessage("root", "secret");
        makeNewIdentityMessage("ScreenName8888");
        testSSLClient(4444);
    }

    @Test @Ignore
    public void listServerTest() {
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.listserver.toString());
        messages.clear();
        messages.add(jj.toJSONString());
        testSSLClient(4444);
    }

    private void makeListRoomMessage() {
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.list.toString());
        messages.add(jj.toJSONString());
    }

    @Test @Ignore
    public void routeServerTest() throws ParseException {

        try {
            socket = (SSLSocket) sslsocketfactory.createSocket("localhost", 4444);

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

            /******************
             *  Simulate User Login to Server S1
             */
            makeLoginMessage("root", "secret"); // login
            makeNewIdentityMessage("ScreenName8888"); // acquire screen name
            makeListRoomMessage(); // list rooms

            for (String message : messages) {
                writer.write(message + "\n");
                writer.flush();
                System.out.println("Sending  : " + message);
            }

            for (int i = 0; i < messages.size()+1; i++) {
                String resp = reader.readLine();
                JSONObject jj = (JSONObject) parser.parse(resp);
                System.out.println("Response : " + jj.toJSONString());
            }

            System.out.println("******************");

            /******************
             *  Simulate User Join Room to a chat room on Server S2
             */

            // request join room from another server {"type" : "join", "roomid" : "jokes"}
            messages.clear();
            JSONObject jj = new JSONObject();
            jj.put(Protocol.type.toString(), Protocol.join.toString());
            jj.put(Protocol.roomid.toString(), "MainHall-s2");
            messages.add(jj.toJSONString());

            for (String message : messages) {
                writer.write(message + "\n");
                writer.flush();
                System.out.println("Sending  : " + message);
            }

            // {"type" : "route", "roomid" : "jokes", "host" : "122.134.2.4", "port" : "4445",
            //      "username" : "ray", "sessionid" : "ba64077b-85b4-40f0-a5ac-480ad3e341b3", "password" : "xxxx"}

            String host = null, username = null, sessionId = null, password = null;
            String port = null;
            if (messages.size() != 1) return; // checkpoint just to make sure
            for (int i = 0; i < messages.size(); i++) {
                String resp = reader.readLine();
                JSONObject jx = (JSONObject) parser.parse(resp);
                System.out.println("Response : " + jx.toJSONString());
                host = (String) jx.get(Protocol.host.toString());
                port = (String) jx.get(Protocol.port.toString());
                username = (String) jx.get(Protocol.username.toString());
                sessionId = (String) jx.get(Protocol.sessionid.toString());
                password = (String) jx.get(Protocol.password.toString());
            }

            System.out.println("******************");


            /*****************
             *  Simulate User Move Join - Server switching to S2
             *  based on information from previous route response step
             */

            // {"type" : "movejoin", "former" : "MainHall-s1", "roomid" : "jokes", "identity" : "ScreenName",
            //      "username" : "ray", "sessionid" : "ba64077b-85b4-40f0-a5ac-480ad3e341b3", "password" : "xxxx"}

            messages.clear();
            JSONObject jm = new JSONObject();
            jm.put(Protocol.type.toString(), Protocol.movejoin.toString());
            jm.put(Protocol.former.toString(), "MainHall-s1");
            jm.put(Protocol.roomid.toString(), "MainHall-s2");
            jm.put(Protocol.identity.toString(), "ScreenName8888");
            jm.put(Protocol.username.toString(), username);
            jm.put(Protocol.sessionid.toString(), sessionId);
            jm.put(Protocol.password.toString(), password);
            messages.add(jm.toJSONString());

            // need to create a tcp connection to s2 server - new socket instance
            SSLSocket s2socket = (SSLSocket) sslsocketfactory.createSocket(host, Integer.parseInt(port));
            BufferedWriter s2writer = new BufferedWriter(new OutputStreamWriter(s2socket.getOutputStream(), "UTF-8"));
            BufferedReader s2reader = new BufferedReader(new InputStreamReader(s2socket.getInputStream(), "UTF-8"));

            for (String message : messages) {
                s2writer.write(message + "\n");
                s2writer.flush();
                System.out.println("Sending  : " + message);
            }

            for (int i = 0; i < messages.size(); i++) {
                String resp = s2reader.readLine();
                JSONObject jv = (JSONObject) parser.parse(resp);
                System.out.println("Response : " + jv.toJSONString());
            }

            // finally expect approved true - should be most situation
            // {"type" : "serverchange", "approved" : "true", "serverid" : "s2"}

            // otherwise, if movejoin fail, expect false - this is rare but still expected!
            // {"type" : "serverchange", "approved" : "false", "serverid" : "s2"}

            //while (reader.readLine() != null) {
                // just to hold off the testing
            //}

            // clean up
            writer.close();
            reader.close();
            socket.close();
            s2writer.close();
            s2reader.close();
            s2socket.close();

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

            //while (reader.readLine() != null) {
                // just to hold off the testing
            //}

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

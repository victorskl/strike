package strike;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import strike.model.Protocol;
import strike.model.ServerInfo;

import java.io.*;
import java.net.Socket;
import java.time.Instant;

public class AppTest {

    JSONObject jj = new JSONObject();
    JSONParser parser = new JSONParser();
    ServerInfo s1;

    Socket socket = null;
    BufferedWriter writer;
    BufferedReader reader;

    @Before
    public void before() throws InterruptedException, IOException {
        s1 = new ServerInfo();
        s1.setAddress("localhost");
        s1.setPort(4444);
        s1.setManagementPort(5555);
        s1.setServerId("s1");

        socket = new Socket(s1.getAddress(), s1.getPort());
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
    }

    @After
    public void after() throws IOException {
        if (socket != null)
            socket.close();
        writer.close();
        reader.close();
    }

    private void write(String message) throws IOException {
        writer.write(message + "\n");
        writer.flush();
    }

    @Test @Ignore
    public void testNewIdentity() throws ParseException, IOException {
        jj.put(Protocol.type.toString(), Protocol.newidentity.toString());
        jj.put(Protocol.identity.toString(), "JUnit" + Instant.now().getEpochSecond());
        write(jj.toJSONString());

        String resp; int i = 0;
        while ((resp = reader.readLine()) != null) {
            i++;
            System.out.println(resp);
            //JSONObject jx = (JSONObject) parser.parse(resp);
            //String approved = (String) jx.get("approved");
            //assertEquals(approved, "true");
            if (i == 2) break;
        }
    }
}

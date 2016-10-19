package strike;

import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.util.Factory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import strike.common.model.Protocol;
import strike.heartbeat.AliveJob;
import strike.model.LocalChatRoomInfo;
import strike.model.RemoteChatRoomInfo;
import strike.model.ServerInfo;
import strike.service.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StrikeServer {

    @Option(name = "-n", usage = "n=Server ID")
    private String serverId = "s1";

    @Option(name = "-l", usage = "l=Server Configuration File")
    private String serverConfig = "./config/server.tab";

    @Option(name = "-c", usage = "c=System Properties file")
    private File systemPropertiesFile = new File("./config/system.properties");

    @Option(name = "-d", usage = "d=Debug")
    private boolean debug = false;

    @Option(name = "--trace", usage = "trace=Trace")
    private boolean trace = false;

    private ServerState serverState = ServerState.getInstance();
    private ServerInfo serverInfo;
    private ExecutorService servicePool;
    private String mainHall;

    private Configuration systemProperties;

    public StrikeServer(String[] args) {
        try {
            CmdLineParser cmdLineParser = new CmdLineParser(this);
            logger.info("Parsing args...");
            cmdLineParser.parseArgument(args);

            logger.info("option: -n " + serverId);
            logger.info("option: -l " + serverConfig);
            logger.info("option: -d " + debug);

            logger.info("Reading server config");
            readServerConfiguration();

            logger.info("option: -c " + systemPropertiesFile.toString());
            logger.info("Reading system properties file: " + systemPropertiesFile.toString());
            try {
                Configurations configs = new Configurations();
                systemProperties = configs.properties(systemPropertiesFile);
            } catch (ConfigurationException e) {
                e.printStackTrace();
            }
            logger.info("Setting up SSL system environment...");
            System.setProperty("javax.net.ssl.keyStore", systemProperties.getString("keystore"));
            System.setProperty("javax.net.ssl.keyStorePassword","strikepass");
            System.setProperty("javax.net.ssl.trustStore", systemProperties.getString("keystore")); // needed for PeerClient
            //System.setProperty("javax.net.debug","all"); // uncomment to debug SSL, and comment it back there after

            setupShiro();

            logger.info("Init server state");
            serverState.initServerState(serverId);

            serverInfo = serverState.getServerInfo();

            updateLogger();

            // POST

            mainHall = "MainHall-" + serverInfo.getServerId();
            LocalChatRoomInfo localChatRoomInfo = new LocalChatRoomInfo();
            localChatRoomInfo.setOwner(""); //The owner of the MainHall in each server is "" (empty string)
            localChatRoomInfo.setChatRoomId(mainHall);
            serverState.getLocalChatRooms().put(mainHall, localChatRoomInfo);

            startUpConnections();

            //addMainHallsStatically();
            syncChatRooms();

            startHeartBeat();

            // Shutdown hook
            Runtime.getRuntime().addShutdownHook(new ShutdownService(servicePool));

        } catch (CmdLineException e) {
            logger.trace(e.getMessage());
        }
    }

    private void startHeartBeat() {
        try {

            JobDetail aliveJob = JobBuilder.newJob(AliveJob.class)
                    .withIdentity("AliveJob", "group1").build();

            aliveJob.getJobDataMap().put("aliveErrorFactor", systemProperties.getInt("alive.error.factor"));

            Trigger aliveTrigger = TriggerBuilder
                    .newTrigger()
                    .withIdentity("AliveJobTrigger", "group1")
                    .withSchedule(
                            SimpleScheduleBuilder.simpleSchedule()
                                    .withIntervalInSeconds(systemProperties.getInt("alive.interval")).repeatForever())
                    .build();

            Scheduler scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();
            scheduler.scheduleJob(aliveJob, aliveTrigger);

        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    private void readServerConfiguration() {
        ColumnPositionMappingStrategy<ServerInfo> strategy = new ColumnPositionMappingStrategy<>();
        strategy.setType(ServerInfo.class);
        CsvToBean<ServerInfo> csvToBean = new CsvToBean<>();
        try {
            serverState.setServerInfoList(csvToBean.parse(strategy, new CSVReader(new FileReader(serverConfig), '\t')));
        } catch (FileNotFoundException e) {
            logger.error("Can not load config file from location: " + serverConfig);
            logger.trace(e.getMessage());
        }
    }

    private void updateLogger() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        org.apache.logging.log4j.core.config.Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig("strike");

        if (debug && !trace) {
            loggerConfig.setLevel(Level.DEBUG);
            ctx.updateLoggers();
            logger.debug("Server is running in DEBUG mode");
        }

        if (trace) {
            loggerConfig.setLevel(Level.TRACE);
            ctx.updateLoggers();
            logger.trace("Server is running in TRACE mode");
        }
    }

    private void startUpConnections() {
        servicePool = Executors.newFixedThreadPool(SERVER_SOCKET_POOL);
        try {
            servicePool.execute(new ClientService(serverInfo.getPort(), CLIENT_SOCKET_POOL));
            servicePool.execute(new ManagementService(serverInfo.getManagementPort(), serverState.getServerInfoList().size()));
        } catch (IOException e) {
            logger.trace(e.getMessage());
            servicePool.shutdown();
        }
    }

    private void addMainHallsStatically() {
        for (ServerInfo server : serverState.getServerInfoList()) {
            if (server.equals(serverInfo)) continue;
            String room = "MainHall-" + server.getServerId();
            RemoteChatRoomInfo remoteRoom = new RemoteChatRoomInfo();
            remoteRoom.setChatRoomId(room);
            remoteRoom.setManagingServer(server.getServerId());
            serverState.getRemoteChatRooms().put(room, remoteRoom);
        }
    }

    /**
     * TODO Spec #4 improve server self register into system
     * This is working by utilising the existing protocols, i.e. by calling a few protocols.
     * A better approach might be, to create a new protocol to handle this.
     */
    private void syncChatRooms() {
        PeerClient peerClient = new PeerClient();
        JSONMessageBuilder messageBuilder = JSONMessageBuilder.getInstance();
        JSONParser parser = new JSONParser();

        for (ServerInfo server : serverState.getServerInfoList()) {
            if (server.equals(this.serverInfo)) continue;

            if (serverState.isOnline(server)) {
                // promote my main hall
                peerClient.commPeer(server, messageBuilder.serverUpMessage());
                peerClient.commPeer(server, messageBuilder.lockRoom(this.mainHall));
                peerClient.commPeer(server, messageBuilder.releaseRoom(this.mainHall, "true"));
                //TODO serverUpMessage to send even earlier?
                //String[] messages = {messageBuilder.serverUpMessage(), messageBuilder.lockRoom(this.mainHall), messageBuilder.releaseRoom(this.mainHall, "true")};
                //peerClient.commPeer(server, messages);

                // accept theirs
                String resp = peerClient.commServerSingleResp(server, messageBuilder.listRoomsClient());
                if (resp != null) {
                    try {
                        JSONObject jsonMessage = (JSONObject) parser.parse(resp);
                        logger.trace("syncChatRooms: " + jsonMessage.toJSONString());
                        JSONArray ja = (JSONArray) jsonMessage.get(Protocol.rooms.toString());
                        for (Object o : ja.toArray()) {
                            String room = (String) o;
                            if (serverState.isRoomExistedRemotely(room)) continue;
                            RemoteChatRoomInfo remoteRoom = new RemoteChatRoomInfo();
                            remoteRoom.setChatRoomId(room);
                            String serverId = server.getServerId();
                            if (room.startsWith("MainHall")) { // every server has MainHall-s* duplicated
                                String sid = room.split("-")[1];
                                if (!sid.equalsIgnoreCase(serverId)) {
                                    //serverId = sid; // Or skip
                                    continue;
                                }
                            }
                            remoteRoom.setManagingServer(serverId);
                            serverState.getRemoteChatRooms().put(room, remoteRoom);
                        }
                    } catch (ParseException e) {
                        //e.printStackTrace();
                        logger.trace(e.getMessage());
                    }
                }
            }
        }
    }

    private void setupShiro() {
        Factory<SecurityManager> factory = new IniSecurityManagerFactory(systemProperties.getString("shiro.ini"));
        SecurityManager securityManager = factory.getInstance();
        SecurityUtils.setSecurityManager(securityManager);
    }

    private static final int SERVER_SOCKET_POOL = 2;
    private static final int CLIENT_SOCKET_POOL = 100;
    private static final Logger logger = LogManager.getLogger(StrikeServer.class);
}

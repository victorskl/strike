package strike.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import strike.common.model.ServerInfo;

import java.util.List;

/**
 *
 */
public class BullyElectionManagementService {
    private static final Logger logger = LogManager.getLogger(BullyElectionManagementService.class);
    private final JSONMessageBuilder jsonMessageBuilder;
    private final PeerClient peerClient;

    public BullyElectionManagementService() {
        this.jsonMessageBuilder = JSONMessageBuilder.getInstance();
        peerClient = new PeerClient();
    }

    public void startElection(ServerInfo proposingCoordinator, List<ServerInfo> candidatesList, Long
            electionAnswerTimeout)
            throws SchedulerException {
        logger.debug("Starting election...");
        String proposingCoordinatorServerId = proposingCoordinator.getServerId();
        String proposingCoordinatorAddress = proposingCoordinator.getAddress();
        Long proposingCoordinatorPort = Long.valueOf(proposingCoordinator.getPort());
        Long proposingCoordinatorManagementPort = Long.valueOf(proposingCoordinator.getManagementPort());
        String startElectionMessage = jsonMessageBuilder
                .startElectionMessage(proposingCoordinatorServerId, proposingCoordinatorAddress,
                        proposingCoordinatorPort, proposingCoordinatorManagementPort);
        peerClient.relaySelectedPeers(candidatesList, startElectionMessage);

        // start a timer to wait for the candidate leaders to respond.
        // if they do not respond, the proposing server becomes the leader.
        startWaitingForAnswerMessage(proposingCoordinator, StdSchedulerFactory.getDefaultScheduler(),
                electionAnswerTimeout);
    }

    public void startWaitingTimer(ServerInfo proposingCoordinator, Scheduler scheduler, Long timeout,
                                  JobDetail jobDetail) throws SchedulerException {
        logger.debug("Starting the waiting job : " + jobDetail.getKey().getName());
        SimpleTrigger simpleTrigger =
                (SimpleTrigger) TriggerBuilder.newTrigger()
                        .withIdentity("election_trigger", "group_" + proposingCoordinator.getServerId())
                        .startAt(DateBuilder.futureDate(Math.toIntExact(timeout), DateBuilder.IntervalUnit.SECOND))
                        .build();
        scheduler.scheduleJob(jobDetail, simpleTrigger);
    }

    public void startWaitingForCoordinatorMessage(ServerInfo proposingCoordinator, Scheduler scheduler, Long timeout)
            throws SchedulerException {
        JobDetail coordinatorMsgTimeoutJob =
                JobBuilder.newJob(ElectionCoordinatorMessageTimeoutFinalizer.class).withIdentity
                        ("coordinator_msg_timeout_job", "group_" + proposingCoordinator.getServerId()).build();
        startWaitingTimer(proposingCoordinator, scheduler, timeout, coordinatorMsgTimeoutJob);
    }


    public void startWaitingForAnswerMessage(ServerInfo proposingCoordinator, Scheduler scheduler, Long timeout)
            throws SchedulerException {
        JobDetail answerMsgTimeoutJob =
                JobBuilder.newJob(ElectionAnswerMessageTimeoutFinalizer.class).withIdentity
                        ("answer_msg_timeout_job", "group_" + proposingCoordinator.getServerId()).build();
        startWaitingTimer(proposingCoordinator, scheduler, timeout, answerMsgTimeoutJob);
    }

    public void replyAnswerForElectionMessage(ServerInfo requestingCandidate, ServerInfo me){
        logger.debug("Replying answer for the election start message from : " + requestingCandidate.getServerId());
        peerClient.commPeerOneWay(requestingCandidate, jsonMessageBuilder.electionAnswerMessage(me.getServerId()));
    }

    public void setupNewCoordinator(ServerInfo newCoordinator, ServerState serverState) {
        logger.debug("Informing subordinates about the new coordinator...");
        // inform subordinates about the new coordinator
        String newCoordinatorServerId = newCoordinator.getServerId();
        String newCoordinatorAddress = newCoordinator.getAddress();
        Long newCoordinatorServerPort = Long.valueOf(newCoordinator.getPort());
        Long newCoordinatorServerManagementPort = Long.valueOf(newCoordinator.getManagementPort());
        String setCoordinatorMessage = jsonMessageBuilder
                .setCoordinatorMessage(newCoordinatorServerId, newCoordinatorAddress, newCoordinatorServerPort,
                        newCoordinatorServerManagementPort);
        peerClient.relaySelectedPeers(serverState.getSubordinateServerInfoList(), setCoordinatorMessage);

        // accept the new coordinator
        acceptNewCoordinator(serverState.getServerInfo(), serverState);
    }

    public void acceptNewCoordinator(ServerInfo newCoordinator, ServerState serverState){
        logger.debug("Accepting new coordinator...");
        serverState.setCoordinator(newCoordinator);
    }

    public void stopWaitingTimer(Scheduler scheduler, JobKey jobKey) throws SchedulerException {
        logger.debug("Stopping waiting for : " + jobKey.getName());
        if (scheduler.checkExists(jobKey)) {
            scheduler.interrupt(jobKey);
            scheduler.deleteJob(jobKey);
        }
    }

    public void stopWaitingForCoordinatorMessage(ServerInfo stoppingServer, Scheduler scheduler)
            throws SchedulerException {
        JobKey coordinatorMsgTimeoutJobKey =
                new JobKey("coordinator_msg_timeout_job", "group_" + stoppingServer.getServerId());
        stopWaitingTimer(scheduler, coordinatorMsgTimeoutJobKey);
    }

    public void stopWaitingForAnswerMessage(ServerInfo stoppingServer, Scheduler scheduler)
            throws SchedulerException {
        JobKey answerMsgTimeoutJobKey =
                new JobKey("answer_msg_timeout_job", "group_" + stoppingServer.getServerId());
        stopWaitingTimer(scheduler, answerMsgTimeoutJobKey);
    }

    public void stopElection(ServerInfo stoppingServer, Scheduler scheduler) throws SchedulerException {
        logger.debug("Stopping election...");
        stopWaitingForAnswerMessage(stoppingServer, scheduler);
        stopWaitingForCoordinatorMessage(stoppingServer, scheduler);
    }
}

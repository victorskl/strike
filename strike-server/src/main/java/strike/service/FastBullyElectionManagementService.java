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
public class FastBullyElectionManagementService extends BullyElectionManagementService {
    private static final Logger logger = LogManager.getLogger(FastBullyElectionManagementService.class);

    public void startElection(ServerInfo proposingCoordinator, List<ServerInfo> candidatesList,
                              Long electionAnswerTimeout, ServerState serverState) throws SchedulerException {
        logger.debug("Fast bully...");
        serverState.initializeTemporaryCandidateMap();
        startElection(proposingCoordinator, candidatesList, electionAnswerTimeout);
        startWaitingForFastBullyAnswerMessage(new StdSchedulerFactory().getScheduler(),
                electionAnswerTimeout);
    }

    public void startWaitingForFastBullyAnswerMessage(Scheduler scheduler, Long timeout)
            throws SchedulerException {
        JobDetail answerMsgTimeoutJob =
                JobBuilder.newJob(FastBullyAnswerMessageTimeoutFinalizer.class).withIdentity
                        ("answer_msg_timeout_job", "group_fast_bully").build();
        startWaitingTimer("group_fast_bully", scheduler, timeout, answerMsgTimeoutJob);
    }

    public void setAnswerReceivedFlag(Scheduler scheduler) throws SchedulerException {
        JobKey fastBullyAnswerTimeoutJobKey = new JobKey("answer_msg_timeout_job", "group_fast_bully");
        if (scheduler.checkExists(fastBullyAnswerTimeoutJobKey)) {
            scheduler.interrupt(fastBullyAnswerTimeoutJobKey);
        }
    }

    public void resetWaitingForCoordinatorMessageTimer(JobExecutionContext context, TriggerKey triggerKey,
                                                       Long timeout) throws
            SchedulerException {
        Trigger simpleTrigger = TriggerBuilder.newTrigger()
                .withIdentity("election_trigger", "group_fast_bully")
                .startAt(DateBuilder.futureDate(Math.toIntExact(timeout), DateBuilder.IntervalUnit.SECOND))
                .build();
        context.getScheduler().rescheduleJob(triggerKey, simpleTrigger);
    }

    public void startWaitingForNominationOrCoordinationMessage(Scheduler scheduler, Long timeout) throws
            SchedulerException {
        JobDetail coordinatorMsgTimeoutJob =
                JobBuilder.newJob(FastBullyNominationMessageTimeoutFinalizer.class).withIdentity
                        ("coordinator_or_nomination_msg_timeout_job", "group_fast_bully").build();
        startWaitingTimer("group_fast_bully", scheduler, timeout, coordinatorMsgTimeoutJob);
    }

    public void startWaitingForCoordinatorMessage(Scheduler scheduler, Long timeout)
            throws SchedulerException {
        JobDetail coordinatorMsgTimeoutJob =
                JobBuilder.newJob(FastBullyCoordinatorMessageTimeoutFinalizer.class).withIdentity
                        ("coordinator_msg_timeout_job", "group_fast_bully").build();
        startWaitingTimer("group_fast_bully", scheduler, timeout, coordinatorMsgTimeoutJob);
    }

    public void startWaitingForViewMessage(Scheduler scheduler, Long electionAnswerTimeout) throws SchedulerException {
        JobDetail coordinatorMsgTimeoutJob =
                JobBuilder.newJob(FastBullyViewMessageTimeoutFinalizer.class).withIdentity
                        ("view_msg_timeout_job", "group_fast_bully").build();
        startWaitingTimer("group_fast_bully", scheduler, electionAnswerTimeout, coordinatorMsgTimeoutJob);
    }

    public void stopElection(ServerInfo stoppingServer, Scheduler scheduler, ServerState serverState)
            throws SchedulerException {
        serverState.resetTemporaryCandidateMap();
        stopWaitingForAnswerMessage(scheduler);
        stopWaitingForCoordinatorMessage(scheduler);
        stopWaitingForNominationMessage(scheduler);
        stopWaitingForViewMessage(scheduler);
    }

    public void stopWaitingForAnswerMessage(Scheduler scheduler) throws SchedulerException {
        JobKey answerMsgTimeoutJobKey = new JobKey("answer_msg_timeout_job", "group_fast_bully");
        stopWaitingTimer(scheduler, answerMsgTimeoutJobKey);
    }

    public void stopWaitingForNominationMessage(Scheduler scheduler) throws SchedulerException {
        JobKey answerMsgTimeoutJobKey = new JobKey("coordinator_or_nomination_msg_timeout_job", "group_fast_bully");
        stopWaitingTimer(scheduler, answerMsgTimeoutJobKey);
    }

    public void stopWaitingForCoordinatorMessage(Scheduler scheduler)
            throws SchedulerException {
        JobKey coordinatorMsgTimeoutJobKey = new JobKey("coordinator_msg_timeout_job", "group_fast_bully");
        stopWaitingTimer(scheduler, coordinatorMsgTimeoutJobKey);
    }

    public void stopWaitingForViewMessage(Scheduler scheduler) throws SchedulerException {
        JobKey viewMsgTimeoutJobKey = new JobKey("view_msg_timeout_job", "group_fast_bully");
        stopWaitingTimer(scheduler, viewMsgTimeoutJobKey);
    }

    public void sendIamUpMessage(ServerInfo serverInfo, List<ServerInfo> serverInfoList) {
        String iAmUpMessage = jsonMessageBuilder.iAmUpMessage(serverInfo.getServerId(), serverInfo.getAddress(),
                serverInfo.getPort(), serverInfo.getManagementPort());
        peerClient.relaySelectedPeers(serverInfoList, iAmUpMessage);
    }

    public void sendViewMessage(ServerInfo sender, ServerInfo coordinator) {
        String viewMessage = jsonMessageBuilder.viewMessage(coordinator.getServerId(), coordinator.getAddress(),
                coordinator.getPort(), coordinator.getManagementPort());
        peerClient.commPeerOneWay(sender, viewMessage);
    }

    public void sendNominationMessage(ServerInfo topCandidate) {
        peerClient.commPeerOneWay(topCandidate, jsonMessageBuilder.nominationMessage());
    }

    public void sendCoordinatorMessage(ServerInfo coordinator, List<ServerInfo> subordinateServerInfoList) {
        String coordinatorMessage = jsonMessageBuilder.setCoordinatorMessage(coordinator
                .getServerId(), coordinator.getAddress(), coordinator.getPort(), coordinator.getManagementPort());
        peerClient.relaySelectedPeers(subordinateServerInfoList, coordinatorMessage);
    }

}

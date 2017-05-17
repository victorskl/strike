package strike.service.election;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.*;
import strike.common.model.ServerInfo;
import strike.service.election.timeout.FastBullyAnswerMessageTimeoutFinalizer;
import strike.service.election.timeout.FastBullyCoordinatorMessageTimeoutFinalizer;
import strike.service.election.timeout.FastBullyNominationMessageTimeoutFinalizer;
import strike.service.election.timeout.FastBullyViewMessageTimeoutFinalizer;

import java.util.List;

public class FastBullyElectionManagementService extends BullyElectionManagementService {

    public void startElection(ServerInfo proposingCoordinator, List<ServerInfo> candidatesList, Long electionAnswerTimeout) {

        // logger.debug("Starting Fast-Bully Election...");

        serverState.initializeTemporaryCandidateMap();
        serverState.setAnswerMessageReceived(false);
        serverState.setOngoingElection(true);

        super.startElection(proposingCoordinator, candidatesList, electionAnswerTimeout);

        startWaitingForFastBullyAnswerMessage(electionAnswerTimeout);
    }

    public void startWaitingForFastBullyAnswerMessage(Long timeout) {
        JobDetail answerMsgTimeoutJob =
                JobBuilder.newJob(FastBullyAnswerMessageTimeoutFinalizer.class).withIdentity
                        ("answer_msg_timeout_job", "group_fast_bully").build();
        startWaitingTimer("group_fast_bully", timeout, answerMsgTimeoutJob);
    }

    public void setAnswerReceivedFlag() {
        try {
            JobKey fastBullyAnswerTimeoutJobKey = new JobKey("answer_msg_timeout_job", "group_fast_bully");
            if (scheduler.checkExists(fastBullyAnswerTimeoutJobKey)) {
                scheduler.interrupt(fastBullyAnswerTimeoutJobKey);
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public void resetWaitingForCoordinatorMessageTimer(JobExecutionContext context, TriggerKey triggerKey, Long timeout) {
        try {
            JobDetail jobDetail = context.getJobDetail();
            if (scheduler.checkExists(jobDetail.getKey())) {

                logger.debug(String.format("Job get trigger again [%s]", jobDetail.getKey().getName()));
                scheduler.triggerJob(jobDetail.getKey());

            } else {

                Trigger simpleTrigger = TriggerBuilder.newTrigger()
                        .withIdentity("election_trigger", "group_fast_bully")
                        .startAt(DateBuilder.futureDate(Math.toIntExact(timeout), DateBuilder.IntervalUnit.SECOND))
                        .build();
                context.getScheduler().rescheduleJob(triggerKey, simpleTrigger);
            }

        } catch (ObjectAlreadyExistsException oe) {
            logger.debug(oe.getLocalizedMessage());

            try {

                JobDetail jobDetail = context.getJobDetail();
                logger.debug(String.format("Job get trigger again [%s]", jobDetail.getKey().getName()));

                scheduler.triggerJob(jobDetail.getKey());

            } catch (SchedulerException e) {
                e.printStackTrace();
            }

        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public void startWaitingForNominationOrCoordinationMessage(Long timeout) {
        JobDetail coordinatorMsgTimeoutJob =
                JobBuilder.newJob(FastBullyNominationMessageTimeoutFinalizer.class).withIdentity
                        ("coordinator_or_nomination_msg_timeout_job", "group_fast_bully").build();
        startWaitingTimer("group_fast_bully", timeout, coordinatorMsgTimeoutJob);
    }

    public void startWaitingForCoordinatorMessage(Long timeout) {
        JobDetail coordinatorMsgTimeoutJob =
                JobBuilder.newJob(FastBullyCoordinatorMessageTimeoutFinalizer.class).withIdentity
                        ("coordinator_msg_timeout_job", "group_fast_bully").build();
        startWaitingTimer("group_fast_bully", timeout, coordinatorMsgTimeoutJob);
    }

    /**
     * This is Boot time only election timer job.
     */
    public void startWaitingForViewMessage(Long electionAnswerTimeout) throws SchedulerException {
        JobDetail coordinatorMsgTimeoutJob =
                JobBuilder.newJob(FastBullyViewMessageTimeoutFinalizer.class).withIdentity
                        ("view_msg_timeout_job", "group_fast_bully").build();
        startWaitingTimer("group_fast_bully", electionAnswerTimeout, coordinatorMsgTimeoutJob);
    }

    public void stopElection(ServerInfo stoppingServer) {

        serverState.resetTemporaryCandidateMap();
        serverState.setOngoingElection(false);

        stopWaitingForAnswerMessage();
        stopWaitingForCoordinatorMessage();
        stopWaitingForNominationMessage();
        stopWaitingForViewMessage();
    }

    public void stopWaitingForAnswerMessage() {
        JobKey answerMsgTimeoutJobKey = new JobKey("answer_msg_timeout_job", "group_fast_bully");
        stopWaitingTimer(answerMsgTimeoutJobKey);
    }

    public void stopWaitingForNominationMessage() {
        JobKey answerMsgTimeoutJobKey = new JobKey("coordinator_or_nomination_msg_timeout_job", "group_fast_bully");
        stopWaitingTimer(answerMsgTimeoutJobKey);
    }

    public void stopWaitingForCoordinatorMessage() {
        JobKey coordinatorMsgTimeoutJobKey = new JobKey("coordinator_msg_timeout_job", "group_fast_bully");
        stopWaitingTimer(coordinatorMsgTimeoutJobKey);
    }

    public void stopWaitingForViewMessage() {
        JobKey viewMsgTimeoutJobKey = new JobKey("view_msg_timeout_job", "group_fast_bully");
        stopWaitingTimer(viewMsgTimeoutJobKey);
    }

    public void sendIamUpMessage(ServerInfo serverInfo, List<ServerInfo> serverInfoList) {
        String iAmUpMessage = jsonMessageBuilder.iAmUpMessage(serverInfo.getServerId(), serverInfo.getAddress(),
                serverInfo.getPort(), serverInfo.getManagementPort());
        peerClient.relaySelectedPeers(serverInfoList, iAmUpMessage);
    }

    public void sendViewMessage(ServerInfo sender, ServerInfo coordinator) {
        if (null == coordinator) {
            // in the beginning coordinator could be null
            coordinator = sender;
        }
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

    private static final Logger logger = LogManager.getLogger(FastBullyElectionManagementService.class);
}

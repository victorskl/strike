package strike.model;

/**
 * Rules of thumb :
 *  - do not abuse the static
 *  - create constant as it make sense i.e. do not do it just for the sake of global variable
 *  - never initialize this class i.e. no "new Constant"
 *  - be neat and principal
 *  - think twice before adding here
 *
 *
 * Check also enum Lingo if it is a specific terms in context,
 * rather than being plain String static constant.
 *
 * :0)
 */
public abstract class Constant {

    private Constant() {} // no init

    public static final String ALIVE_JOB = "AliveJob".toUpperCase();
    public static final String ALIVE_JOB_TRIGGER = "AliveJobTrigger".toUpperCase();
    public static final String CONSENSUS_JOB = "ConsensusJob".toUpperCase();
    public static final String CONSENSUS_JOB_TRIGGER = "ConsensusJobTrigger".toUpperCase();
    public static final String GOSSIP_JOB = "GossipJob".toUpperCase();
    public static final String GOSSIP_JOB_TRIGGER = "GossipJobTrigger".toUpperCase();
    public static final String ELECTION_TRIGGER = "election_trigger".toUpperCase();

}

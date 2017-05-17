package strike.service;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

public class Quartz {

    private static Quartz instance;
    private static Scheduler scheduler;

    private Quartz() {
    }

    public static synchronized Quartz getInstance() {
        if (instance == null) {
            instance = new Quartz();
        }
        return instance;
    }

    public synchronized Scheduler getScheduler() {
        if (scheduler == null) {
            try {
                scheduler = StdSchedulerFactory.getDefaultScheduler();
                // new StdSchedulerFactory().getScheduler()  this is the same thing
                // as long as we do not call initialize(my_custom_scheduler.properties)
                // unless we need multiple schedulers
                // http://www.quartz-scheduler.org/documentation/quartz-2.x/cookbook/MultipleSchedulers.html
                // http://www.quartz-scheduler.org/documentation/quartz-2.x/cookbook/CreateScheduler.html
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        }
        return scheduler;
    }
}

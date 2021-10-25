package grabber;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;

/**
 * @author Ajiekcander
 */

public interface Grab {
    void init(Parse parse, Store store, Scheduler scheduler) throws SchedulerException;
}
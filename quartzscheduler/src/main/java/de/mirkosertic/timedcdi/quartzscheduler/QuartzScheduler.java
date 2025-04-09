package de.mirkosertic.timedcdi.quartzscheduler;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import de.mirkosertic.cdicron.api.JobScheduler;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * An implementation of {@link JobScheduler} using Quartz
 * 
 */
@ApplicationScoped
public class QuartzScheduler implements JobScheduler {

    public static class RunnableWrapperJob implements Job {

        public static final String KEY = "runnable";

        @Override
        public void execute(JobExecutionContext aContext) throws JobExecutionException {
            JobDataMap theDataMap = aContext.getJobDetail().getJobDataMap();
            Runnable theRunnable = (Runnable) theDataMap.get(KEY);
            theRunnable.run();
        }
    }

    private final Scheduler scheduler;

    public QuartzScheduler() throws SchedulerException {
        SchedulerFactory theFactory = new StdSchedulerFactory();
        scheduler = theFactory.getScheduler();
        scheduler.start();
    }

    @Override
    public void schedule(String aCronExpression, Runnable aRunnable) {
        JobDataMap theJobDataMap = new JobDataMap();
        theJobDataMap.put(RunnableWrapperJob.KEY, aRunnable);
        JobDetail theDetails = newJob(RunnableWrapperJob.class).setJobData(theJobDataMap).build();
        Trigger theTrigger = newTrigger().withSchedule(cronSchedule(aCronExpression)).build();
        try {
            scheduler.scheduleJob(theDetails, theTrigger);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Shutdown the scheduler when the application server stops
     */
    @PreDestroy
    public void shutdown() {
        try {
            scheduler.shutdown();
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }
}
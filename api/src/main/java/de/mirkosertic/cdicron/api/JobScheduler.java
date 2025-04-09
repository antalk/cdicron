package de.mirkosertic.cdicron.api;

import jakarta.enterprise.inject.spi.BeanManager;

/**
 * Base class for all Job Scheduler Implementations.
 *
 * The {@link CDICronExtension} will search for an implementinng Singleton in the
 * aktive CDI {@link BeanManager} and register it.
 */
public interface JobScheduler {

    /**
     * Schedule a Task.
     *
     * @param aCronExpression a cron expression to use
     * @param aRunnable the task to schedule
     */
    void schedule(String aCronExpression, Runnable aRunnable);
}

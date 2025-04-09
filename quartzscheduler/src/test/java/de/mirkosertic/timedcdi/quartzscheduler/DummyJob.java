package de.mirkosertic.timedcdi.quartzscheduler;

import de.mirkosertic.cdicron.api.Cron;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.concurrent.atomic.AtomicLong;

@ApplicationScoped
public class DummyJob {

    public static final AtomicLong COUNTER = new AtomicLong(0);

    @Inject
    private DummyBean dummyBean;

    @Cron(cronExpression = "0/2 * * * * ?")
    public void testTimed() {
        dummyBean.doSomething();
        COUNTER.incrementAndGet();
    }
}

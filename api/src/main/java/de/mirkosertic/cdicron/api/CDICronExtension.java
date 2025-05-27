package de.mirkosertic.cdicron.api;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessManagedBean;

public class CDICronExtension implements Extension {


    private final List<JobInfo> jobsToRegister;
    private Bean<?> jobScheduler;

    public CDICronExtension() {
        jobsToRegister = new ArrayList<>();
    }

    public void onProcessBean(@Observes ProcessManagedBean aEvent, BeanManager aBeanManager) {
        final Class<?> theBeanType = aEvent.getBean().getBeanClass();
        if (JobScheduler.class.isAssignableFrom(theBeanType)) {
            jobScheduler = aEvent.getBean();
        }
        for (Method theMethod : theBeanType.getDeclaredMethods()) {
            if (theMethod.isAnnotationPresent(Cron.class)) {
                Cron theTimed = theMethod.getAnnotation(Cron.class);
                BeanMethodInvocationRunnable theRunnable = new BeanMethodInvocationRunnable(aEvent.getBean(), aBeanManager, theMethod);
                jobsToRegister.add(new JobInfo(theTimed, theRunnable));
            }
        }
    }

    public void onAfterDeploymentValidation(@Observes AfterDeploymentValidation aEvent, BeanManager aBeanManager) {
        if (jobScheduler == null) {
            throw new IllegalStateException("No JobScheduler Implementation found in managed scope!");
        }
        final CreationalContext<?> theContext = aBeanManager.createCreationalContext(jobScheduler);
        try {
            JobScheduler theScheduler = (JobScheduler) aBeanManager.getReference(jobScheduler, jobScheduler.getBeanClass(), theContext);
            for (JobInfo t : jobsToRegister) {
                theScheduler.schedule(t.aTimed().cronExpression(), t.aRunnable());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

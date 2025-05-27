package de.mirkosertic.cdicron.api;



import java.lang.reflect.Method;

import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;

class BeanMethodInvocationRunnable implements Runnable {
    
	final Bean<?> bean;
    final BeanManager beanManager;
    final Method method;

    boolean firstInit;
    private Object instance;

    public BeanMethodInvocationRunnable(Bean<?> aBean, BeanManager aBeanManager, Method aMethod) {
        bean = aBean;
        beanManager = aBeanManager;
        method = aMethod;
        firstInit = true;
    }

    @Override
    public void run() {
        if (firstInit) {
            Context theContext = beanManager.getContext(bean.getScope());
            instance = theContext.get(bean);
            if (instance == null) {
                final CreationalContext<?> theCreational = beanManager.createCreationalContext(bean);
                instance = beanManager.getReference(bean, bean.getBeanClass(), theCreational);
            }
            firstInit = false;
        }
        try {
            method.invoke(instance, new Object[0]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

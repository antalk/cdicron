package de.mirkosertic.cdicron.api;

record JobInfo(Cron aTimed, BeanMethodInvocationRunnable aRunnable) {
}

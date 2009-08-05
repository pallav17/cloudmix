/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.agent.mop;

import org.fusesource.cloudmix.common.dto.ProvisioningAction;
import org.fusesource.mop.MOP;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.StringTokenizer;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.Lists;

/**
 * @version $Revision: 1.1 $
 */
public class MopProcess {
    private static final transient Log LOG = LogFactory.getLog(MopProcess.class);

    private ProvisioningAction action;
    private String credentials;
    private String commandLine;
    private MOP mop = new MOP();
    private int statusCode = -1;
    private Thread thread;
    private AtomicBoolean completed = new AtomicBoolean(false);

    public MopProcess(ProvisioningAction action, String credentials, String commandLine) {
        this.action = action;
        this.credentials = credentials;
        this.commandLine = commandLine;
    }

    public String getId() {
        return action.getFeature();
    }

    public ProvisioningAction getAction() {
        return action;
    }

    public String getCommandLine() {
        return commandLine;
    }

    public String getCredentials() {
        return credentials;
    }

    public void start() throws Exception {
        final List<String> argList = Lists.newArrayList();
        if (commandLine != null) {
            StringTokenizer iter = new StringTokenizer(commandLine);
            while (iter.hasMoreTokens()) {
                argList.add(iter.nextToken());
            }
        }

        // lets run in a background thread
        thread = new Thread("Feature: " + getId() + "MOP " + argList) {
            @Override
            public void run() {
                LOG.info("Starting feature: " + getId() + " via MOP: " + argList);
                String[] args = argList.toArray(new String[argList.size()]);
                try {
                    statusCode = mop.execute(args);
                    LOG.info("Stopped feature: " + getId() + " with status code: " + statusCode);
                } catch (Exception e) {
                    LOG.error("Failed running feature: " + getId() + ". Reason: " + e, e);
                }
                finally {
                    completed.set(true);
                }
            }
        };
        thread.start();
    }

    public void stop() throws Exception {
        // TODO we've currently no easy way to kill a process!
        // for exec we should be able to kill the pid; for embedded, all we can do is remove references and hope!
        mop = null;
        thread = null;
    }
}

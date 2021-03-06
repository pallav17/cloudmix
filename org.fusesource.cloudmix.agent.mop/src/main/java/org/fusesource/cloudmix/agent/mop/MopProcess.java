/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudmix.agent.mop;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.Lists;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.cloudmix.common.GridClient;
import org.fusesource.cloudmix.common.dto.ProvisioningAction;
import org.fusesource.cloudmix.common.util.FileUtils;
import org.fusesource.cloudmix.common.util.Strings;
import org.fusesource.mop.MOP;
import org.fusesource.mop.ProcessRunner;

/**
 * @version $Revision: 1.1 $
 */
public class MopProcess {
    private static final transient Log LOG = LogFactory.getLog(MopProcess.class);

    private MopAgent mopAgent;
    private ProvisioningAction action;
    private String credentials;
    private String commandLine;
    private ClassLoader mopClassLoader;
    private MOP mop = new MOP();
    private int statusCode = -1;
    private Thread thread;
    private AtomicBoolean completed = new AtomicBoolean(false);
    private File workDirectory;
    private String featureId;

    public MopProcess(MopAgent mopAgent, ProvisioningAction action, String credentials, String commandLine, ClassLoader mopClassLoader) {
        this.mopAgent = mopAgent;
        this.action = action;
        this.featureId = action.getFeature();
        this.credentials = credentials;
        this.commandLine = commandLine;
        this.mopClassLoader = mopClassLoader;
        this.workDirectory = mopAgent.createProcessDirectory(action);
    }

    public String getFeatureId() {
        return featureId;
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

    public File getWorkDirectory() {
        return workDirectory;
    }

    public void start() throws Exception {
        final List<String> argList = Lists.newArrayList();
        if (commandLine != null) {
            StringTokenizer iter = new StringTokenizer(commandLine);
            while (iter.hasMoreTokens()) {
                argList.add(iter.nextToken());
            }
        }
        if (argList.isEmpty()) {
            throw new IllegalArgumentException("No arguments specified");
        }

        // Propagate the profile properties to the forked process...
        String profile = mopAgent.getProfileFor(action);
        if (profile == null) {
            LOG.warn("Could not find profile ID for action " + action);
        } else {
            GridClient gridClient = mopAgent.getClient();
            Properties properties = gridClient.getProperties(profile);
            if (properties == null) {
                LOG.warn("No properties available for profile: " + profile);
            } else {
                for (Entry<Object, Object> entry : properties.entrySet()) {
                    mop.setSystemProperty(Strings.asString(entry.getKey()), Strings.asString(entry.getValue()));
                }
            }
        }


        // Propagate repository props to the forked process:
        for (Entry<String, String> entry : mop.getRepository().getRepositorySystemProps().entrySet()) {
            mop.setSystemProperty(entry.getKey(), entry.getValue());
        }
        
        // lets ensure the command will spin off to another process:
        if (!(argList.contains("fork") || argList.contains("exec"))) {
            LOG.info("Adding Fork To MopProcess: " + argList);
            argList.add(0, "fork");
        }
        
        // lets transform the class loader to exclude the parent (to avoid maven & jetty plugin dependencies)
        // lets run in a background thread
        thread = new Thread("Feature: " + getId() + "MOP " + argList) {
            @Override
            public void run() {
                LOG.debug("Using class loader: " + mopClassLoader + " of type: " + mopClassLoader.getClass());
                dumpClassLoader(mopClassLoader);

                File dir = getWorkDirectory();
                FileUtils.createDirectory(dir);
                try {
                    LOG.info("Created working directory " + dir.getCanonicalPath() + " for feature " + getId());
                } catch (IOException e1) {
                    LOG.error("Problem with working directory for " + dir + " for feature " + getId());
                }

                mop.setWorkingDirectory(dir);

                Thread.currentThread().setContextClassLoader(mopClassLoader);

                LOG.info("Starting feature: " + getId() + " via MOP: " + argList);
                String[] args = argList.toArray(new String[argList.size()]);
                try {
                    statusCode = mop.executeAndWait(args);
                    LOG.info("Stopped feature: " + getId() + " with status code: " + statusCode);
                } catch (Exception e) {
                    LOG.error("Failed running feature: " + getId() + ". Reason: " + e, e);
                } finally {
                    clear();
                }
            }
        };
        thread.setContextClassLoader(mopClassLoader);
        thread.start();
    }

    void dumpClassLoader(ClassLoader cl) {
        if (LOG.isDebugEnabled()) {
            if (cl instanceof URLClassLoader) {
                URLClassLoader urlClassLoader = (URLClassLoader) cl;
                URL[] urls = urlClassLoader.getURLs();
                for (URL url : urls) {
                    LOG.debug("ClassLoader URL: " + url);
                }
            }
            ClassLoader parent = cl.getParent();
            if (parent != null) {
                LOG.debug("Parent Class Loader: " + parent);
                dumpClassLoader(parent);
            }
        }
    }

    public void stop() throws Exception {
        if (mop != null) {
            ProcessRunner processRunner = mop.getProcessRunner();
            if (processRunner != null) {
                processRunner.kill();
            }
        }
        clear();
    }

    public boolean isCompleted() {
        return completed.get();
    }

    private void clear() {
        completed.set(true);
        mop = null;
        thread = null;
    }
}

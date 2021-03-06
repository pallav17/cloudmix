/**
 *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  The software in this package is published under the terms of the AGPL license
 *  a copy of which has been included with this distribution in the license.txt file.
 */
package org.fusesource.cloudmix.agent.security;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/*
 * Password provider that reads the password from a file.  The password is the first non empty line read
 * with line endings removed.
 */
public class FilePasswordProvider implements PasswordProvider {

    private static final transient Log LOG = LogFactory.getLog(FilePasswordProvider.class);

    private String passwordFile;
    private boolean loggedError;

    public void setPasswordFile(String f) {
        passwordFile = f;
    }

    public char[] getPassword() {
        if (passwordFile == null) {
            logError("no password file specified");
            return null;
        }
        File file = new File(passwordFile);
        if (!file.exists()) {
            logError("password file " + passwordFile + " does not exist");
            return null;
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String pw = null;
            do {
                pw = reader.readLine();
                if (pw != null && !"".equals(pw)) {
                    reader.close();
                    return pw.toCharArray();
                }

            } while (pw != null);
            logError("No password found in file " + file);

        } catch (IOException e) {
            LOG.error("Error reading password from file " + passwordFile + ", exception " + e);
        }
        return null;
    }

    private void logError(String message) {
        if (!loggedError) {
            loggedError = true;
            LOG.error(message);
        }

    }

}

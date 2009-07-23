package org.fusesource.testrunner.rmi;

import java.io.*;
import java.util.StringTokenizer;

import org.fusesource.testrunner.rmi.BasicProcessWrapper.IProcessOutputListener;

class TRLaunchHelper {
    private LaunchCommand _lcommand = null;
    private String _rawLaunchString = null;
    private TRLaunchDescr _bootstrapLD;
    private String _bootstrapString;
    public static final String NL = System.getProperty("line.separator");

    
    
    public TRLaunchHelper(String bootstrapString, TRLaunchDescr bootstrapLD) throws Exception {
        _bootstrapString = bootstrapString;
        _bootstrapLD = bootstrapLD;
        _rawLaunchString = extractLaunchCommandLine(generateLaunchScript());
        // _bootstrapLD.getRunNameGen() should be contained within _rawLaunchString
        _lcommand = new LaunchCommand(_rawLaunchString, extractGeneratedProcessName());
    }

    public String getTRLaunchString() throws Exception {
        // Get the real launch string -- this is essentially the rawLaunchString,
        //  but with jvm and jvm args before the bootclasspath replaced

        /*
         * String modified_command_line = lc.getVMExecutable() + " " +
         * jvm64BitArg + " " + restrictOsSigArg + " " + debugArgs + " " +
         * lc.getVMArgs() + " " + m_jvmArgs + " " + lc.getLaunchClassname() +
         * " " + lc.getLaunchClassArgs();
         */

        // java exe is the first token of the _bootstrapString
        String str = new String(_bootstrapString);
        str = stripQuotes(str);

        StringTokenizer st = new StringTokenizer(str);
        String javaExe = st.nextToken();

        // jvm args -- use args up to -Xbootclasspath from  _bootstrapString
        String vmargs1 = "";
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.indexOf("bootclasspath") != -1) {
                // found
                break;
            }
            vmargs1 = vmargs1 + token + " ";
        }

        // for the rest of the vm args, use everything starting with -Xbootclasspath from the vm args
        //   of the new generated launch string
        String vmargs2 = "";
        String rawVMArgs = _lcommand.getVMArgs();
        st = new StringTokenizer(rawVMArgs);
        boolean skip = true;
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.indexOf("bootclasspath") != -1) {
                skip = false;
            }
            if (!skip)
                vmargs2 = vmargs2 + token + " ";
        }

        String adjustedLaunchString = javaExe + " " + vmargs1 + " " + vmargs2 + " " + _lcommand.getLaunchClassname() + " " + _lcommand.getLaunchClassArgs();

        return adjustedLaunchString;
    }

    /**
     * This is the first phase of the container launch sequence, aka the
     * bootstrap phase. In this phase, a script is generated by the management
     * framework. It is this script that contains the information to launch the
     * "real" container.
     * 
     * @return File containing the generated launch code
     * @throws IOException
     */
    private File generateLaunchScript() throws Exception {
        //String workingDir = _bootstrapLD.getWorkingDir();
        //Process m_process = null;
        BasicProcessWrapper bpw = new BasicProcessWrapper();

        bpw.setCommandLine(_bootstrapString);
        bpw.setDirectory(_bootstrapLD.getWorkingDir());
        bpw.setEnv(_bootstrapLD.getEnvParams());

        BootstrapOutputListener bol = new BootstrapOutputListener();

        bpw.addListener(bol);

        bpw.launch();

        if (bpw.getExitStatus() != 0) {
            throw new IOException("The bootstrap container launch command did not exit normally: " + NL + bol.getOutput().toString());
        }

        String launchscript = getLaunchScriptName();

        File lfile = new File(_bootstrapLD.getWorkingDir(), launchscript);

        return lfile;
    }

    // A hack: assumes the first server parameter is the file name
    private String getLaunchScriptName() throws Exception {
        if (_bootstrapLD.getArgs() == null)
            throw new Exception("Unable to find  boot file name; args is null");

        StringTokenizer st = new StringTokenizer(_bootstrapLD.getArgs());

        String fname = st.nextToken();

        if (fname == null)
            throw new Exception("Unable to find  boot file name  in args, " + _bootstrapLD.getArgs());

        fname = stripQuotes(fname).trim();

        return fname + (File.separatorChar == '\\' ? ".bat" : ".sh");

    }

    /**
     * Go through the generated launch script looking for the telltale java
     * command line.
     * 
     * @return
     * @throws IOException
     */
    private String extractLaunchCommandLine(File launchScriptFile) throws IOException {
        BufferedReader br = null;
        String commandline = null;

        try {
            br = new BufferedReader(new FileReader(launchScriptFile));
            String line = null;
            while (true) {
                line = br.readLine();
                if (line == null) {
                    break;
                }
                if (line.indexOf("java") > -1) {
                    commandline = line;
                    break;
                }
            }
        } finally {
            if (br != null) {
                br.close();
            }
        }

        return commandline;
    }

    private String extractGeneratedProcessName() throws Exception {
        StringTokenizer st = new StringTokenizer(_bootstrapLD.getRunName());

        //SString rname1 = st.nextToken();

        if (!st.hasMoreTokens())
            throw new Exception("incorrect runname; launch class is missing " + _bootstrapLD.getRunName());
        String rname2 = st.nextToken();

        return stripQuotes(rname2).trim();
    }

    private String stripQuotes(String input) {
        return input.replaceAll("\"", "");
    }

    /**
     * Listener specializing is capturing the output associated with the initial
     * bootstrap container launch sequence. The output should only be useful in
     * the event of a non-zero exit status from the virtual machine used in
     * generating the real container launch command.
     * 
     * @author jdeignan
     * 
     */
    private class BootstrapOutputListener implements IProcessOutputListener {
        StringBuffer _output = null;

        public StringBuffer getOutput() {
            if (_output == null) {
                _output = new StringBuffer();
            }

            return _output;
        }

        public void processOutput(String s) {
            getOutput().append(s + TRLaunchHelper.NL);
        }
    }

    private class LaunchCommand {
        //private String _vmexe = "";
        //private String _vmargs = "";
        //private String _classname = "";
        //private String _classargs = "";
        private String _rawline = "";
        private String _launchClass;
        private String JAVA = "java";

        LaunchCommand(String raw_line, String launchclass) throws IOException {
            if (raw_line == null || raw_line.length() == 0) {
                throw new IOException("Unable to parse null or zero length string");
            }
            _rawline = raw_line;

            if (launchclass == null || launchclass.length() == 0) {
                throw new IOException("Launch class not specified");
            }
            _launchClass = launchclass;
        }

        /**
         * The java executable is the first token in the command line
         * 
         * @return
         * @throws IOException
         */
        String getVMExecutable() throws IOException {
            StringTokenizer st = new StringTokenizer(_rawline);

            String vmexe = st.nextToken();

            if (vmexe.indexOf(JAVA) == -1) {
                throw new IOException("Unable to find " + JAVA + " in command line, " + _rawline);
            }

            return stripQuotes(vmexe).trim();
        }

        /**
         * VM args are all tokens between the first token (the vm exe) and the
         * container launch class name.
         * 
         * @return
         * @throws IOException
         */
        String getVMArgs() throws IOException {
            int classposition = _rawline.indexOf(_launchClass);
            if (classposition == -1) {
                throw new IOException("Unable to find container launch class, " + _launchClass + " in command line, " + _rawline);
            }
            return stripQuotes(_rawline.substring((this.getVMExecutable().length() + 1), (classposition - 1))).trim();
        }

        /**
         * Return the name of the class used in the second phase of the launch
         * sequence.
         * 
         * @return
         */
        String getLaunchClassname() {
            return _launchClass;
        }

        /**
         * Return the portion of the command line that includes the arguments
         * passed into the main method of the launch class.
         * 
         * @return
         * @throws IOException
         */
        String getLaunchClassArgs() throws IOException {
            int classposition = _rawline.indexOf(_launchClass);
            if (classposition == -1) {
                throw new IOException("Unable to find container launch class, " + _launchClass + " in command line, " + _rawline);
            }
            return stripQuotes(_rawline.substring(classposition + _launchClass.length() + 1)).trim();
        }

        /**
         * Return a string that has all quotes stripped away/removed. This is
         * more or less a workaround to a problem we have with launching
         * containers on UNIX platforms. For some reason, the quoting of various
         * command line constituents is causing grief during the exec. We use
         * this command for now to simply remove the quotes from a given string.
         * See Sonic25480 for more details.
         * 
         * @param input
         * @return
         */
        private String stripQuotes(String input) {
            return input.replaceAll("\"", "");
        }

        public String toString() {
            String str = "";
            try {
                StringBuffer sb = new StringBuffer();
                sb.append("VMEXE=" + this.getVMExecutable() + NL);
                sb.append("VMARGS=" + this.getVMArgs() + NL);
                sb.append("LaunchClass=" + this.getLaunchClassname() + NL);
                sb.append("LaunchClassArgs=" + this.getLaunchClassArgs());
                str = sb.toString();
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }

            return str;
        }
    }
}

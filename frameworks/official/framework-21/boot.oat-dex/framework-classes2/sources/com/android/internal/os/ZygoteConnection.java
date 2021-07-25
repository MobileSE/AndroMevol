package com.android.internal.os;

import android.net.Credentials;
import android.net.LocalSocket;
import android.os.Process;
import android.os.SELinux;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import com.android.internal.os.ZygoteInit;
import dalvik.system.PathClassLoader;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import libcore.io.IoUtils;

/* access modifiers changed from: package-private */
public class ZygoteConnection {
    private static final int CONNECTION_TIMEOUT_MILLIS = 1000;
    private static final int MAX_ZYGOTE_ARGC = 1024;
    private static final String TAG = "Zygote";
    private static final int[][] intArray2d = ((int[][]) Array.newInstance(Integer.TYPE, 0, 0));
    private final String abiList;
    private final LocalSocket mSocket;
    private final DataOutputStream mSocketOutStream;
    private final BufferedReader mSocketReader;
    private final Credentials peer;
    private final String peerSecurityContext;

    ZygoteConnection(LocalSocket socket, String abiList2) throws IOException {
        this.mSocket = socket;
        this.abiList = abiList2;
        this.mSocketOutStream = new DataOutputStream(socket.getOutputStream());
        this.mSocketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()), 256);
        this.mSocket.setSoTimeout(1000);
        try {
            this.peer = this.mSocket.getPeerCredentials();
            this.peerSecurityContext = SELinux.getPeerContext(this.mSocket.getFileDescriptor());
        } catch (IOException ex) {
            Log.e(TAG, "Cannot read peer credentials", ex);
            throw ex;
        }
    }

    private void checkTime(long startTime, String where) {
        long now = SystemClock.elapsedRealtime();
        if (now - startTime > 1000) {
            Slog.w(TAG, "Slow operation: " + (now - startTime) + "ms so far, now at " + where);
        }
    }

    /* access modifiers changed from: package-private */
    public FileDescriptor getFileDescriptor() {
        return this.mSocket.getFileDescriptor();
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00ce A[SYNTHETIC, Splitter:B:31:0x00ce] */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x01f0  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean runOnce() throws com.android.internal.os.ZygoteInit.MethodAndArgsCaller {
        /*
        // Method dump skipped, instructions count: 554
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.ZygoteConnection.runOnce():boolean");
    }

    private boolean handleAbiListQuery() {
        try {
            byte[] abiListBytes = this.abiList.getBytes(StandardCharsets.US_ASCII);
            this.mSocketOutStream.writeInt(abiListBytes.length);
            this.mSocketOutStream.write(abiListBytes);
            return false;
        } catch (IOException ioe) {
            Log.e(TAG, "Error writing to command socket", ioe);
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public void closeSocket() {
        try {
            this.mSocket.close();
        } catch (IOException ex) {
            Log.e(TAG, "Exception while closing command socket in parent", ex);
        }
    }

    /* access modifiers changed from: package-private */
    public static class Arguments {
        boolean abiListQuery;
        String appDataDir;
        boolean capabilitiesSpecified;
        String classpath;
        int debugFlags;
        long effectiveCapabilities;
        int gid = 0;
        boolean gidSpecified;
        int[] gids;
        String instructionSet;
        String invokeWith;
        int mountExternal = 0;
        String niceName;
        long permittedCapabilities;
        String[] remainingArgs;
        ArrayList<int[]> rlimits;
        boolean runtimeInit;
        String seInfo;
        boolean seInfoSpecified;
        int targetSdkVersion;
        boolean targetSdkVersionSpecified;
        int uid = 0;
        boolean uidSpecified;

        Arguments(String[] args) throws IllegalArgumentException {
            parseArgs(args);
        }

        private void parseArgs(String[] args) throws IllegalArgumentException {
            int curArg = 0;
            while (true) {
                if (curArg >= args.length) {
                    break;
                }
                String arg = args[curArg];
                if (arg.equals("--")) {
                    curArg++;
                    break;
                }
                if (!arg.startsWith("--setuid=")) {
                    if (!arg.startsWith("--setgid=")) {
                        if (!arg.startsWith("--target-sdk-version=")) {
                            if (arg.equals("--enable-debugger")) {
                                this.debugFlags |= 1;
                            } else if (arg.equals("--enable-safemode")) {
                                this.debugFlags |= 8;
                            } else if (arg.equals("--enable-checkjni")) {
                                this.debugFlags |= 2;
                            } else if (arg.equals("--enable-jni-logging")) {
                                this.debugFlags |= 16;
                            } else if (arg.equals("--enable-assert")) {
                                this.debugFlags |= 4;
                            } else if (arg.equals("--runtime-init")) {
                                this.runtimeInit = true;
                            } else if (!arg.startsWith("--seinfo=")) {
                                if (!arg.startsWith("--capabilities=")) {
                                    if (arg.startsWith("--rlimit=")) {
                                        String[] limitStrings = arg.substring(arg.indexOf(61) + 1).split(",");
                                        if (limitStrings.length != 3) {
                                            throw new IllegalArgumentException("--rlimit= should have 3 comma-delimited ints");
                                        }
                                        int[] rlimitTuple = new int[limitStrings.length];
                                        for (int i = 0; i < limitStrings.length; i++) {
                                            rlimitTuple[i] = Integer.parseInt(limitStrings[i]);
                                        }
                                        if (this.rlimits == null) {
                                            this.rlimits = new ArrayList<>();
                                        }
                                        this.rlimits.add(rlimitTuple);
                                    } else if (!arg.equals("-classpath")) {
                                        if (!arg.startsWith("--setgroups=")) {
                                            if (!arg.equals("--invoke-with")) {
                                                if (!arg.startsWith("--nice-name=")) {
                                                    if (arg.equals("--mount-external-multiuser")) {
                                                        this.mountExternal = 2;
                                                    } else if (arg.equals("--mount-external-multiuser-all")) {
                                                        this.mountExternal = 3;
                                                    } else if (arg.equals("--query-abi-list")) {
                                                        this.abiListQuery = true;
                                                    } else if (!arg.startsWith("--instruction-set=")) {
                                                        if (!arg.startsWith("--app-data-dir=")) {
                                                            break;
                                                        }
                                                        this.appDataDir = arg.substring(arg.indexOf(61) + 1);
                                                    } else {
                                                        this.instructionSet = arg.substring(arg.indexOf(61) + 1);
                                                    }
                                                } else if (this.niceName != null) {
                                                    throw new IllegalArgumentException("Duplicate arg specified");
                                                } else {
                                                    this.niceName = arg.substring(arg.indexOf(61) + 1);
                                                }
                                            } else if (this.invokeWith != null) {
                                                throw new IllegalArgumentException("Duplicate arg specified");
                                            } else {
                                                curArg++;
                                                try {
                                                    this.invokeWith = args[curArg];
                                                } catch (IndexOutOfBoundsException e) {
                                                    throw new IllegalArgumentException("--invoke-with requires argument");
                                                }
                                            }
                                        } else if (this.gids != null) {
                                            throw new IllegalArgumentException("Duplicate arg specified");
                                        } else {
                                            String[] params = arg.substring(arg.indexOf(61) + 1).split(",");
                                            this.gids = new int[params.length];
                                            for (int i2 = params.length - 1; i2 >= 0; i2--) {
                                                this.gids[i2] = Integer.parseInt(params[i2]);
                                            }
                                        }
                                    } else if (this.classpath != null) {
                                        throw new IllegalArgumentException("Duplicate arg specified");
                                    } else {
                                        curArg++;
                                        try {
                                            this.classpath = args[curArg];
                                        } catch (IndexOutOfBoundsException e2) {
                                            throw new IllegalArgumentException("-classpath requires argument");
                                        }
                                    }
                                } else if (this.capabilitiesSpecified) {
                                    throw new IllegalArgumentException("Duplicate arg specified");
                                } else {
                                    this.capabilitiesSpecified = true;
                                    String[] capStrings = arg.substring(arg.indexOf(61) + 1).split(",", 2);
                                    if (capStrings.length == 1) {
                                        this.effectiveCapabilities = Long.decode(capStrings[0]).longValue();
                                        this.permittedCapabilities = this.effectiveCapabilities;
                                    } else {
                                        this.permittedCapabilities = Long.decode(capStrings[0]).longValue();
                                        this.effectiveCapabilities = Long.decode(capStrings[1]).longValue();
                                    }
                                }
                            } else if (this.seInfoSpecified) {
                                throw new IllegalArgumentException("Duplicate arg specified");
                            } else {
                                this.seInfoSpecified = true;
                                this.seInfo = arg.substring(arg.indexOf(61) + 1);
                            }
                        } else if (this.targetSdkVersionSpecified) {
                            throw new IllegalArgumentException("Duplicate target-sdk-version specified");
                        } else {
                            this.targetSdkVersionSpecified = true;
                            this.targetSdkVersion = Integer.parseInt(arg.substring(arg.indexOf(61) + 1));
                        }
                    } else if (this.gidSpecified) {
                        throw new IllegalArgumentException("Duplicate arg specified");
                    } else {
                        this.gidSpecified = true;
                        this.gid = Integer.parseInt(arg.substring(arg.indexOf(61) + 1));
                    }
                } else if (this.uidSpecified) {
                    throw new IllegalArgumentException("Duplicate arg specified");
                } else {
                    this.uidSpecified = true;
                    this.uid = Integer.parseInt(arg.substring(arg.indexOf(61) + 1));
                }
                curArg++;
            }
            if (!this.runtimeInit || this.classpath == null) {
                this.remainingArgs = new String[(args.length - curArg)];
                System.arraycopy(args, curArg, this.remainingArgs, 0, this.remainingArgs.length);
                return;
            }
            throw new IllegalArgumentException("--runtime-init and -classpath are incompatible");
        }
    }

    private String[] readArgumentList() throws IOException {
        try {
            String s = this.mSocketReader.readLine();
            if (s == null) {
                return null;
            }
            int argc = Integer.parseInt(s);
            if (argc > 1024) {
                throw new IOException("max arg count exceeded");
            }
            String[] result = new String[argc];
            for (int i = 0; i < argc; i++) {
                result[i] = this.mSocketReader.readLine();
                if (result[i] == null) {
                    throw new IOException("truncated request");
                }
            }
            return result;
        } catch (NumberFormatException e) {
            Log.e(TAG, "invalid Zygote wire format: non-int at argc");
            throw new IOException("invalid wire format");
        }
    }

    private static void applyUidSecurityPolicy(Arguments args, Credentials peer2, String peerSecurityContext2) throws ZygoteSecurityException {
        int peerUid = peer2.getUid();
        if (peerUid != 0) {
            if (peerUid == 1000) {
                String factoryTest = SystemProperties.get("ro.factorytest");
                if ((!factoryTest.equals("1") && !factoryTest.equals("2")) && args.uidSpecified && args.uid < 1000) {
                    throw new ZygoteSecurityException("System UID may not launch process with UID < 1000");
                }
            } else if (args.uidSpecified || args.gidSpecified || args.gids != null) {
                throw new ZygoteSecurityException("App UIDs may not specify uid's or gid's");
            }
        }
        if ((args.uidSpecified || args.gidSpecified || args.gids != null) && !SELinux.checkSELinuxAccess(peerSecurityContext2, peerSecurityContext2, "zygote", "specifyids")) {
            throw new ZygoteSecurityException("Peer may not specify uid's or gid's");
        }
        if (!args.uidSpecified) {
            args.uid = peer2.getUid();
            args.uidSpecified = true;
        }
        if (!args.gidSpecified) {
            args.gid = peer2.getGid();
            args.gidSpecified = true;
        }
    }

    public static void applyDebuggerSystemProperty(Arguments args) {
        if ("1".equals(SystemProperties.get("ro.debuggable"))) {
            args.debugFlags |= 1;
        }
    }

    private static void applyRlimitSecurityPolicy(Arguments args, Credentials peer2, String peerSecurityContext2) throws ZygoteSecurityException {
        int peerUid = peer2.getUid();
        if (peerUid != 0 && peerUid != 1000 && args.rlimits != null) {
            throw new ZygoteSecurityException("This UID may not specify rlimits.");
        } else if (args.rlimits != null && !SELinux.checkSELinuxAccess(peerSecurityContext2, peerSecurityContext2, "zygote", "specifyrlimits")) {
            throw new ZygoteSecurityException("Peer may not specify rlimits");
        }
    }

    private static void applyInvokeWithSecurityPolicy(Arguments args, Credentials peer2, String peerSecurityContext2) throws ZygoteSecurityException {
        int peerUid = peer2.getUid();
        if (args.invokeWith != null && peerUid != 0) {
            throw new ZygoteSecurityException("Peer is not permitted to specify an explicit invoke-with wrapper command");
        } else if (args.invokeWith != null && !SELinux.checkSELinuxAccess(peerSecurityContext2, peerSecurityContext2, "zygote", "specifyinvokewith")) {
            throw new ZygoteSecurityException("Peer is not permitted to specify an explicit invoke-with wrapper command");
        }
    }

    private static void applyseInfoSecurityPolicy(Arguments args, Credentials peer2, String peerSecurityContext2) throws ZygoteSecurityException {
        int peerUid = peer2.getUid();
        if (args.seInfo != null) {
            if (peerUid != 0 && peerUid != 1000) {
                throw new ZygoteSecurityException("This UID may not specify SELinux info.");
            } else if (!SELinux.checkSELinuxAccess(peerSecurityContext2, peerSecurityContext2, "zygote", "specifyseinfo")) {
                throw new ZygoteSecurityException("Peer may not specify SELinux info");
            }
        }
    }

    public static void applyInvokeWithSystemProperty(Arguments args) {
        if (args.invokeWith == null && args.niceName != null && args.niceName != null) {
            String property = "wrap." + args.niceName;
            if (property.length() > 31) {
                property = property.substring(0, 31);
            }
            args.invokeWith = SystemProperties.get(property);
            if (args.invokeWith != null && args.invokeWith.length() == 0) {
                args.invokeWith = null;
            }
        }
    }

    private void handleChildProc(Arguments parsedArgs, FileDescriptor[] descriptors, FileDescriptor pipeFd, PrintStream newStderr) throws ZygoteInit.MethodAndArgsCaller {
        ClassLoader cloader;
        closeSocket();
        ZygoteInit.closeServerSocket();
        if (descriptors != null) {
            try {
                ZygoteInit.reopenStdio(descriptors[0], descriptors[1], descriptors[2]);
                for (FileDescriptor fd : descriptors) {
                    IoUtils.closeQuietly(fd);
                }
                newStderr = System.err;
            } catch (IOException ex) {
                Log.e(TAG, "Error reopening stdio", ex);
            }
        }
        if (parsedArgs.niceName != null) {
            Process.setArgV0(parsedArgs.niceName);
        }
        if (!parsedArgs.runtimeInit) {
            try {
                String className = parsedArgs.remainingArgs[0];
                String[] mainArgs = new String[(parsedArgs.remainingArgs.length - 1)];
                System.arraycopy(parsedArgs.remainingArgs, 1, mainArgs, 0, mainArgs.length);
                if (parsedArgs.invokeWith != null) {
                    WrapperInit.execStandalone(parsedArgs.invokeWith, parsedArgs.classpath, className, mainArgs);
                    return;
                }
                if (parsedArgs.classpath != null) {
                    cloader = new PathClassLoader(parsedArgs.classpath, ClassLoader.getSystemClassLoader());
                } else {
                    cloader = ClassLoader.getSystemClassLoader();
                }
                try {
                    ZygoteInit.invokeStaticMain(cloader, className, mainArgs);
                } catch (RuntimeException ex2) {
                    logAndPrintError(newStderr, "Error starting.", ex2);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                logAndPrintError(newStderr, "Missing required class name argument", null);
            }
        } else if (parsedArgs.invokeWith != null) {
            WrapperInit.execApplication(parsedArgs.invokeWith, parsedArgs.niceName, parsedArgs.targetSdkVersion, pipeFd, parsedArgs.remainingArgs);
        } else {
            RuntimeInit.zygoteInit(parsedArgs.targetSdkVersion, parsedArgs.remainingArgs, null);
        }
    }

    private boolean handleParentProc(int pid, FileDescriptor[] descriptors, FileDescriptor pipeFd, Arguments parsedArgs) {
        if (pid > 0) {
            setChildPgid(pid);
        }
        if (descriptors != null) {
            for (FileDescriptor fd : descriptors) {
                IoUtils.closeQuietly(fd);
            }
        }
        boolean usingWrapper = false;
        if (pipeFd != null && pid > 0) {
            DataInputStream is = new DataInputStream(new FileInputStream(pipeFd));
            int innerPid = -1;
            try {
                innerPid = is.readInt();
                try {
                    is.close();
                } catch (IOException e) {
                }
            } catch (IOException ex) {
                Log.w(TAG, "Error reading pid from wrapped process, child may have died", ex);
                try {
                    is.close();
                } catch (IOException e2) {
                }
            } catch (Throwable th) {
                try {
                    is.close();
                } catch (IOException e3) {
                }
                throw th;
            }
            if (innerPid > 0) {
                int parentPid = innerPid;
                while (parentPid > 0 && parentPid != pid) {
                    parentPid = Process.getParentPid(parentPid);
                }
                if (parentPid > 0) {
                    Log.i(TAG, "Wrapped process has pid " + innerPid);
                    pid = innerPid;
                    usingWrapper = true;
                } else {
                    Log.w(TAG, "Wrapped process reported a pid that is not a child of the process that we forked: childPid=" + pid + " innerPid=" + innerPid);
                }
            }
        }
        try {
            this.mSocketOutStream.writeInt(pid);
            this.mSocketOutStream.writeBoolean(usingWrapper);
            return false;
        } catch (IOException ex2) {
            Log.e(TAG, "Error writing to command socket", ex2);
            return true;
        }
    }

    private void setChildPgid(int pid) {
        try {
            ZygoteInit.setpgid(pid, ZygoteInit.getpgid(this.peer.getPid()));
        } catch (IOException e) {
            Log.i(TAG, "Zygote: setpgid failed. This is normal if peer is not in our session");
        }
    }

    private static void logAndPrintError(PrintStream newStderr, String message, Throwable ex) {
        Log.e(TAG, message, ex);
        if (newStderr != null) {
            StringBuilder append = new StringBuilder().append(message);
            Object obj = ex;
            if (ex == null) {
                obj = "";
            }
            newStderr.println(append.append(obj).toString());
        }
    }
}

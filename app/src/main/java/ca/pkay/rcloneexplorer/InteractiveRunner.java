package ca.pkay.rcloneexplorer;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import ca.pkay.rcloneexplorer.util.FLog;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Run a process using interactive interfaces
 */
public class InteractiveRunner {

    private static final String TAG = "InteractiveRunner";
    private Process process;
    private Thread runner;

    public InteractiveRunner(Step startingStep, ErrorHandler errorHandler, Process process) {
        this.process = process;
        runner = new StepRunner(process, startingStep, errorHandler);
    }

    public void runSteps() {
        runner.start();
    }

    public void forceStop() {
        runner.interrupt();
        process.destroy();
    }

    /**
     * A single interaction. Consists of one trigger, one action and may
     * contain multiple following steps. The interaction sequence should in
     * general follow a tree-like structure. If there are multiple following
     * steps, the order of iteration is not guaranteed.
     * <br><br>
     * You can loop around by adding the same step node as a following node,
     * but you may want to add a max loop condition to your step action.
     * To halt the the execution, you can also throw any {@link RuntimeException}.
     */
    public static class Step {

        @Retention(RetentionPolicy.SOURCE)
        @IntDef({CONTAINS, ENDS_WITH})
        public @interface TriggerType {
        }

        /**
         * The string match must contain the trigger word. Note that you must
         * ensure that the trigger is unique, or else it may match on
         * transcript that is already in the transcript buffer.
         */
        public static final int CONTAINS = 0;
        /**
         * The string match must end with this string. Recommended trigger type
         * because it will match as soon as possible.
         */
        public static final int ENDS_WITH = 1;

        @Retention(RetentionPolicy.SOURCE)
        @IntDef({STDOUT, STDERR, INTERLEAVED})
        public @interface StreamType {
        }

        /**
         * Match if token appears on standard output. Only effective if all
         * Steps at the current level are also STDOUT, otherwise INTERLEAVED.
         */
        public static final int STDOUT = 1;
        /**
         * Match if token appears on standard error. Only effective if all
         * Steps at the current level are also STDERR, otherwise INTERLEAVED.
         */
        public static final int STDERR = 2;
        /**
         * Match if token appears in any output stream. This will first query
         * STDERR for available characters, otherwise switch immedeately to
         * STDOUT and wait for the trigger or timeout.
         */
        public static final int INTERLEAVED = 3;

        private String trigger;
        @TriggerType
        private int matchType;
        @StreamType
        private int streamType;
        private Action action;
        private List<Step> followingSteps;

        /**
         * Create a Step with default settings
         * @param trigger String to trigger on
         * @param action Action to execute on trigger
         */
        public Step(String trigger, Action action) {
            this(trigger, ENDS_WITH, INTERLEAVED, action);
        }

        /**
         * Create a Step with default settings
         * @param trigger String to trigger on
         * @param action String to print on trigger
         */
        public Step(String trigger, String action) {
            this(trigger, ENDS_WITH, INTERLEAVED, new StringAction(action));
        }

        /**
         * Create a step
         * @param trigger String to trigger on
         * @param matchType Location of the trigger String in the process' output
         * @param streamType If the trigger should match on stdout, stderr or character-interleaved
         * @param action {@link Action} to execute when the trigger matches
         */
        public Step(String trigger, @TriggerType int matchType, @StreamType int streamType, Action action) {
            this.trigger = trigger;
            this.matchType = matchType;
            this.streamType = streamType;
            this.action = action;
            followingSteps = new LinkedList<>();
        }

        public Step addFollowing(Step step) {
            followingSteps.add(step);
            return step;
        }

        public Step addFollowing(String trigger, String action) {
            return addFollowing(new Step(trigger, new StringAction(action)));
        }

        public Step addFollowing(String trigger, String action, @StreamType int streamType) {
            return addFollowing(new Step(trigger, ENDS_WITH, streamType, new StringAction(action)));
        }

        public List<Step> getFollowing() {
            return Collections.unmodifiableList(followingSteps);
        }

        public String getTrigger() {
            return trigger;
        }

        public int getMatchType() {
            return matchType;
        }

        public Action getAction() {
            return action;
        }

        public int getStreamType() {
            return streamType;
        }

        /**
         * Wait 10 seconds before timeout
         * @return timeout value
         */
        public long getTimeout() {
            return 10000L;
        }
    }

    /**
     * A action that is taken on a specific CLI input
     */
    public interface Action {
        /**
         * Invoked when the cli encounters the trigger
         * @param cliBuffer the current cli buffer
         */
        void onTrigger(String cliBuffer);

        /**
         * Send this string to the application as an cli input
         * @return
         */
        String getInput();
    }

    /**
     * A simple {@link Action} that prints the specified String to the
     * processes STDIN followed by a newline character.
     */
    public static class StringAction implements Action {
        private String action;

        /**
         * Create a StringAction
         * @param action The string to print
         */
        public StringAction(String action) {
            this.action = action;
        }

        @Override
        public void onTrigger(String cliBuffer) {
            // do nothing
        }

        @Override
        public String getInput() {
            return action;
        }
    }

    /**
     * A error handler
     */
    public interface ErrorHandler {
        /**
         * Called if the recipe could not be executed successfully.
         * @param exception An {@link IOException} if one of the pipes breaks,
         *                  a {@link TimeoutException} if the runner timed out
         *                  waiting for a trigger or any other
         *                  {@link RuntimeException}.
         */
        void onError(Exception exception);
    }

    static class StepRunner extends Thread {
        private final Process process;
        private final Step firstStep;
        private final ErrorHandler errorHandler;

        public StepRunner(@NonNull Process process, @NonNull Step firstStep, @NonNull ErrorHandler errorHandler) {
            this.process = process;
            this.firstStep = firstStep;
            this.errorHandler = errorHandler;
        }

        /**
         * Wait for a trigger to appear and then input the specified sequence.
         * Note that if the trigger does not appear, the thread will timeout
         * after 10 seconds (or other, if the Step defines a custom timeout).
         */
        @Override
        public void run() {
            // kept at top scope to enable debug logging
            String bufferContent = null;
            List<? extends Step> currentSteps = Collections.singletonList(firstStep);

            try (InputStreamReader stdout = new InputStreamReader(process.getInputStream());
                 InputStreamReader stderr = new InputStreamReader(process.getErrorStream());
                 PrintWriter stdin = new PrintWriter(new OutputStreamWriter(process.getOutputStream()))) {
                // Store the last 256 characters for matching on triggers
                char[] buffer = new char[256];
                int bufPos = 0;

                while (!isInterrupted()) {
                    // Set to interleaved if there are multiple nodes at the same
                    // level with different stream types
                    int streamType = currentSteps.get(0).getStreamType();
                    for (Step step : currentSteps) {
                        if (step.streamType != streamType) {
                            streamType = Step.INTERLEAVED;
                            break;
                        }
                    }

                    // Wait if required (e.g. OAuth)
                    long timeout = currentSteps.get(0).getTimeout();

                    // Read the next character and build an in-order string representation for triggers
                    readChar(buffer, bufPos, streamType, timeout, stdout, stderr);
                    bufPos++;
                    if (bufPos != buffer.length) {
                        bufferContent = new String(buffer, bufPos, buffer.length - bufPos);
                        bufferContent += new String(buffer, 0, bufPos);
                    } else {
                        bufferContent = new String(buffer);
                    }

                    if (bufferContent.endsWith("\n")) {
                        int index = bufferContent.lastIndexOf('\n', bufferContent.length() - 2);
                        if (index < 0) {
                            index = 0;
                        }
                        FLog.d(TAG, "rclone: %s", bufferContent.substring(index));
                    }

                    // Test if any of the current steps have matching triggers
                    Step matchedStep = matchToTriggers(bufferContent, currentSteps);

                    if (null != matchedStep) {
                        FLog.d(TAG, "run: Match for Step on '%s'", matchedStep.trigger);
                        Action action = matchedStep.getAction();
                        action.onTrigger(bufferContent);
                        FLog.d(TAG, "run: entering '%s'", action.getInput());
                        stdin.println(action.getInput());
                        stdin.flush();

                        // copy to buffer (full transcript for debugging)
                        char[] input = action.getInput().toCharArray();
                        char[] inputTranscript = new char[input.length + 1];
                        inputTranscript[inputTranscript.length - 1] = '\n';
                        System.arraycopy(input, 0, inputTranscript, 0, input.length);
                        for (char c : inputTranscript) {
                            if (bufPos >= buffer.length) {
                                bufPos = 0;
                            }
                            buffer[bufPos] = c;
                            bufPos++;
                        }
                        // Promote the current step node as main path
                        currentSteps = matchedStep.getFollowing();
                        if (null == currentSteps || 1 > currentSteps.size()) {
                            FLog.d(TAG, "Run script finished");
                            break;
                        }
                    }
                    if (bufPos >= buffer.length) {
                        bufPos = 0;
                    }
                }
            } catch (IOException | TimeoutException | RuntimeException e) {
                makeDebugReport(bufferContent, currentSteps, e);
                errorHandler.onError(e);
            }
        }

        Step matchToTriggers(String bufferContent, List<? extends Step> currentSteps) {
            boolean isMatch = false;
            Step matchedStep = null;
            for (Step step : currentSteps) {
                switch (step.getMatchType()) {
                    case Step.ENDS_WITH:
                        isMatch = bufferContent.endsWith(step.getTrigger());
                        break;
                    case Step.CONTAINS:
                        isMatch = bufferContent.contains(step.getTrigger());
                        break;
                }
                if (isMatch) {
                    matchedStep = step;
                    break;
                }
            }
            return matchedStep;
        }

        private void makeDebugReport(String bufferContent, List<? extends Step> currentSteps, Exception e) {
            if (null != bufferContent) {
                StringBuilder sb = new StringBuilder();
                if (null != currentSteps) {
                    for (Step step : currentSteps) {
                        sb.append(" - '").append(step.trigger).append('\'');
                    }
                } else {
                    sb.append("(none)");
                }
                FLog.e(TAG, "run: transcript: \n%s\n active triggers:\n%s", e, bufferContent, sb);
            } else {
                FLog.e(TAG, "run: (no transcript)", e);
            }
        }

        private void readChar(char[] buffer, int pos, @Step.StreamType int streamType, long timeout,
                              InputStreamReader stdout, InputStreamReader stderr) throws IOException, TimeoutException {
            switch (streamType) {
                case Step.STDOUT:
                    buffer[pos] = readWithTimeout(stdout, timeout);
                    break;
                case Step.STDERR:
                    buffer[pos] = readWithTimeout(stderr, timeout);
                    break;
                case Step.INTERLEAVED:
                    // Disable timeout for stderr to avoid delays in case when only stdout is used
                    buffer[pos] = stderr.ready() ? readWithTimeout(stderr, timeout) : readWithTimeout(stdout, timeout);
                    break;
            }
        }

        /**
         * Timeout if the specified interface is not ready within 10 seconds.
         * @param reader
         * @throws IOException
         */
        private char readWithTimeout(InputStreamReader reader, long timeout) throws IOException, TimeoutException {
            if (reader.ready()) {
                return (char) reader.read();
            } else {
                while (timeout > 0L) {
                    long startedAt = System.currentTimeMillis();
                    try {
                        Thread.sleep(100L);
                        if (reader.ready()) {
                            return (char) reader.read();
                        }
                    } catch (InterruptedException e) {
                        throw new IOException();
                    }
                    long now = System.currentTimeMillis();
                    timeout -= now - startedAt;
                }
                throw new TimeoutException("Could not read char from CLI");
            }
        }
    }

}

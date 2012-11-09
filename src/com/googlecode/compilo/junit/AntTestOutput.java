package com.googlecode.compilo.junit;

import com.googlecode.totallylazy.Files;
import com.googlecode.totallylazy.Strings;
import com.googlecode.totallylazy.time.Clock;
import com.googlecode.totallylazy.time.Dates;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.googlecode.totallylazy.Files.file;
import static com.googlecode.totallylazy.collections.ImmutableSortedMap.constructors.sortedMap;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class AntTestOutput extends RunListener {
    private static DateFormat ANT_ISO8601_DATETIME = Dates.format("yyyy-MM-dd'T'HH:mm:ss");
    private final File directory;
    private final Properties properties;
    private final Clock clock;
    private File testFile;
    private String testName;
    private Date started;
    private final Map<String, Map<Field, Object>> testsCases = new HashMap<String, Map<Field, Object>>();

    public AntTestOutput(File directory, Properties properties, Clock clock) {
        super();
        this.directory = directory;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public void testRunStarted(Description description) throws Exception {
        started = clock.now();
        testName = description.getChildren().get(0).getDisplayName();
        testFile = file(directory, String.format("TEST-%s.xml", testName));
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        String xml = applyTemplate("report",
                result.getFailureCount(),
                result.getFailureCount(),
                hostName(),
                testName,
                result.getRunCount(),
                MILLISECONDS.toSeconds(result.getRunTime()),
                ANT_ISO8601_DATETIME.format(started),
                properties(),
                testsCases(),
                LocalPrintStream.reset());

        Files.write(Strings.bytes(xml), testFile);
    }

    @Override
    public void testStarted(Description description) throws Exception {
        testsCases.put(description.getMethodName(), new HashMap<Field, Object>() {{put(Field.Start, clock.now());}});
    }

    @Override
    public void testFinished(Description description) throws Exception {
        testsCases.get(description.getMethodName()).put(Field.End, clock.now());
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        super.testFailure(failure);
    }

    @Override
    public void testAssumptionFailure(Failure failure) {
        super.testAssumptionFailure(failure);
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        super.testIgnored(description);
    }

    private String applyTemplate(String name, Object... arguments) {
        return format(Strings.toString(getClass().getResourceAsStream(name + ".template")), arguments);
    }

    private static String hostName() throws UnknownHostException {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }

    private String properties() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Object, Object> nameValue : properties.entrySet()) {
            builder.append(applyTemplate("property", nameValue.getKey(), nameValue.getValue()));
        }
        return builder.toString();
    }

    private String testsCases() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Map<Field, Object>> nameValues : testsCases.entrySet()) {
            builder.append(applyTemplate("testcase", testName, nameValues.getKey(), calculateTime(nameValues), ""));
        }
        return builder.toString();
    }

    private Object calculateTime(Map.Entry<String, Map<Field, Object>> nameValues) {
        Date start = (Date) nameValues.getValue().get(Field.Start);
        Date end = (Date) nameValues.getValue().get(Field.End);
        return (end.getTime() - start.getTime()) / 1000f;
    }

    private static enum Field {
        Class,
        Method,
        Start,
        End
    }
}

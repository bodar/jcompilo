package com.googlecode.jcompilo.tests.junit;

import com.googlecode.jcompilo.tests.LocalPrintStream;
import com.googlecode.totallylazy.Files;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.Strings;
import com.googlecode.totallylazy.Xml;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.googlecode.totallylazy.Callables.first;
import static com.googlecode.totallylazy.Files.file;
import static com.googlecode.totallylazy.Maps.pairs;
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
    private final List<Failure> errors = new ArrayList<Failure>();
    private final List<Failure> failures = new ArrayList<Failure>();

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
        testFile = file(directory, filename());
    }

    private String filename() {
        return String.format("TEST-%s.xml", testName);
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        String xml = applyTemplate("report",
                errors.size(),
                failures.size(),
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
        testsCases.put(description.getMethodName(), new HashMap<Field, Object>() {{
            put(Field.Start, clock.now());
        }});
    }

    @Override
    public void testFinished(Description description) throws Exception {
        testsCases.get(description.getMethodName()).put(Field.End, clock.now());
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        errors.add(failure);
        add(failure, Type.Error, failure.getException().getMessage());
    }

    @Override
    public void testAssumptionFailure(Failure failure) {
        errors.add(failure);
        add(failure, Type.Failure, failure.getMessage());
    }

    private void add(Failure failure, Type type, String message) {
        Map<Field, Object> data = testsCases.get(failure.getDescription().getMethodName());
        data.put(Field.Type, type);
        data.put(Field.Message, message);
        data.put(Field.ExceptionType, failure.getException());
        data.put(Field.FullMessage, failure.getTrace());
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
        for (Pair<String, String> nameValue : pairs(properties).<Pair<String, String>>unsafeCast().sortBy(first(String.class))) {
            builder.append(applyTemplate("property", nameValue.first(), Xml.escape(nameValue.second())));
        }
        return builder.toString();
    }

    private String testsCases() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Map<Field, Object>> entry : testsCases.entrySet()) {
            Map<Field, Object> fields = entry.getValue();
            builder.append(applyTemplate("testcase", testName, entry.getKey(), calculateTime(fields), handleFailure(fields)));
        }
        return builder.toString();
    }

    private String handleFailure(Map<Field, Object> fields) {
        if (!fields.containsKey(Field.Type)) return "";
        return applyTemplate(fields.get(Field.Type).toString().toLowerCase(), new Object[]{Xml.escape(fields.get(Field.Message)), fields.get(Field.ExceptionType), Xml.escape(fields.get(Field.FullMessage))});
    }

    private Object calculateTime(Map<Field, Object> fields) {
        Date start = (Date) fields.get(Field.Start);
        Date end = (Date) fields.get(Field.End);
        return (end.getTime() - start.getTime()) / 1000f;
    }

    private static enum Field {
        Start,
        End,
        Type,
        Message,
        ExceptionType,
        FullMessage
    }

    private static enum Type {
        Error,
        Failure
    }
}

package com.googlecode.jcompilo.convention;

import com.googlecode.jcompilo.Processes;
import com.googlecode.totallylazy.functions.Block;
import com.googlecode.totallylazy.Pair;
import com.googlecode.totallylazy.collections.PersistentMap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.googlecode.totallylazy.Closeables.using;
import static com.googlecode.totallylazy.collections.PersistentSortedMap.constructors.sortedMap;

public class LastCommit {
    public static final PersistentMap<String, String> commitCommands = sortedMap(
            ".hg", "hg log -l 1",
            ".git", "git log -n 1 --pretty=format:\"user:%an%ndate:%aD%nsummary:%s%nchangeset:%H\""
    );

    public static Properties lastCommitData(File root) throws IOException {
        final Properties properties = new Properties();
        for (Pair<String, String> command : commitCommands) {
            if (new File(root, command.first()).exists()) {
                using(Processes.inputStream(command.second(), root), (Block<InputStream>) properties::load);
            }
        }
        return properties;
    }
}

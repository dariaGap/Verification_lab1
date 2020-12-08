package model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Versions {
    private static final Logger log = LoggerFactory.getLogger(Versions.class);

    private Map<String,Integer> lastVersion;

    public Versions() {
        lastVersion = new HashMap<>();
    }

    public Variable getVersion(Map<String, Set<Integer>> versionsCollection,
                             String variable) {
        Set<Integer> variableVersions = versionsCollection.get(variable);
        if (variableVersions == null) {
            log.error("model.Variable '" + variable + "' have not been initialized.");
            throw new IllegalArgumentException("model.Variable '" + variable + "' have not been initialized.");
        } else {
            return new Variable(variable,variableVersions);
        }
    }

    public Variable setVersion(Map<String, Set<Integer>> versionsCollection,
                           String variable, boolean isCounter) {
        Set<Integer> versions;
        Integer version = lastVersion.get(variable);
        versions = new HashSet<>();
        if (version == null) {
            versions.add(1);
            lastVersion.put(variable,1);
            versionsCollection.put(variable,versions);
            return new Variable(variable,1, Variable.Type.DECLARATOR);
        } else {
            version++;
            lastVersion.put(variable,version);
            versions.add(version);
            if (isCounter) {
                versions.addAll(versionsCollection.get(variable));
            }
            versionsCollection.put(variable,versions);
            return new Variable(variable,version, Variable.Type.DECLARATOR);
        }
    }

    public Integer getLastVersion(String var) {
        return lastVersion.get(var);
    }
}

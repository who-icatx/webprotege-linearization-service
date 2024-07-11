package edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.LinearizationRevision;

import java.util.*;
import java.util.stream.Collectors;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils.LinearizationEventHelper.getRandomLinearizationEvents;

public class LinearizationRevisionHelper {

    //Revisions must be sorted by timestamp
    public static Set<LinearizationRevision> getLinearizationRevisions(int numberOfRevisions) {
        Set<LinearizationRevision> listOfRevisions = new HashSet<>();
        int iteration = 0;
        while (numberOfRevisions > iteration) {
            long timestamp = iteration;
            listOfRevisions.add(new LinearizationRevision(timestamp, "user" + numberOfRevisions, getRandomLinearizationEvents()));
            iteration++;
        }

        var sortedRevisions = listOfRevisions.stream()
                .sorted(Comparator.comparingLong(LinearizationRevision::timestamp))
                .collect(Collectors.toCollection(TreeSet::new));

        return sortedRevisions;
    }
}

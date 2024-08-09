package edu.stanford.protege.webprotege.linearizationservice.testUtils;

import edu.stanford.protege.webprotege.common.UserId;
import edu.stanford.protege.webprotege.linearizationservice.model.LinearizationRevision;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static edu.stanford.protege.webprotege.linearizationservice.testUtils.LinearizationEventHelper.getRandomLinearizationEvents;

public class LinearizationRevisionHelper {

    //Revisions must be sorted by timestamp
    public static Set<LinearizationRevision> getLinearizationRevisions(int numberOfRevisions) {
        Set<LinearizationRevision> listOfRevisions = new HashSet<>();
        while (numberOfRevisions > 0) {
            listOfRevisions.add(getLinearizationRevision());
            numberOfRevisions--;
        }

        var sortedRevisions = listOfRevisions.stream()
                .sorted(Comparator.comparingLong(LinearizationRevision::timestamp))
                .collect(Collectors.toCollection(TreeSet::new));

        return sortedRevisions;
    }

    public static LinearizationRevision getLinearizationRevision() {
        UserId userId = UserId.valueOf("user" + Instant.now());
        return new LinearizationRevision(Instant.now().toEpochMilli(), userId, getRandomLinearizationEvents());
    }
}

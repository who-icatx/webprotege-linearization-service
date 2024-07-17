package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.EntityLinearizationHistory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.Utils.isNotEquals;
import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils.EntityLinearizationHistoryHelper.getEntityLinearizationHistory;
import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils.RandomHelper.getRandomString;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(MockitoJUnitRunner.class)
public class UtilsTest {
    @Test
    public void GIVEN_twoObjects_WHEN_oneIsNullAndTheOtherIsNot_THEN_isNotEqualReturnsTrue() {
        var string1 = getRandomString();
        String stringNull = null;

        var stringList = List.of(string1, getRandomString(), getRandomString());
        List<String> nullList = null;

        var history1 = getEntityLinearizationHistory(ProjectId.generate(), 3);
        EntityLinearizationHistory history2 = null;

        assertTrue(isNotEquals(string1, stringNull));
        assertTrue(isNotEquals(stringNull, string1));

        assertTrue(isNotEquals(stringList, nullList));
        assertTrue(isNotEquals(nullList, stringList));


        assertFalse(isNotEquals(null, null));
        assertFalse(isNotEquals(string1, string1));

        assertTrue(isNotEquals(history1, history2));
        assertTrue(isNotEquals(history2, history1));

        assertFalse(isNotEquals(history1, history1));
    }

    @Test
    public void GIVEN_twoObjects_WHEN_theObjectsAreNotEqual_THEN_isNotEqualReturnsTrue() {
        String string1 = getRandomString();
        String string2 = getRandomString();

        var stringList1 = List.of("someString", getRandomString(), getRandomString());
        var stringList2 = List.of("someOtherString", getRandomString(), getRandomString());

        var history1 = getEntityLinearizationHistory(ProjectId.generate(), 3);
        var history2 = getEntityLinearizationHistory(ProjectId.generate(), 3);


        assertTrue(isNotEquals(string1, string2));
        assertTrue(isNotEquals(string2, string1));

        assertTrue(isNotEquals(stringList1, stringList2));
        assertTrue(isNotEquals(stringList2, stringList1));

        assertTrue(isNotEquals(history1, history2));
        assertTrue(isNotEquals(history2, history1));
    }

    @Test
    public void GIVEN_twoObjects_WHEN_theObjectsAreEqual_THEN_isNotEqualReturnsFalse() {
        String string1 = getRandomString();
        String string2 = getRandomString();
        String string3 = getRandomString();

        var stringList1 = List.of(string1, string2, string3);
        var stringList2 = List.of(string1, string2, string3);

        var history1 = getEntityLinearizationHistory(ProjectId.generate(), 3);
        var history2 = history1;


        assertFalse(isNotEquals(string1, string1));

        assertFalse(isNotEquals(stringList1, stringList2));
        assertFalse(isNotEquals(stringList2, stringList1));

        assertFalse(isNotEquals(history1, history2));
        assertFalse(isNotEquals(history2, history1));
    }
}
package edu.stanford.protege.webprotege.initialrevisionhistoryservice.repositories.history;

import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.services.LinearizationHistoryService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils.EntityLinearizationHistoryHelper.getEntityLinearizationHistory;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Import({WebprotegeLinearizationServiceServiceApplication.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@RunWith(SpringRunner.class)
public class RandomTest extends IntegrationTest {
    @Autowired
    private LinearizationHistoryService linearizationHistoryService;

    @Test
    public void someTest(){
        var projectId = ProjectId.generate();
        var newHistory = getEntityLinearizationHistory(projectId, 2);
        linearizationHistoryService.saveMultipleEntityLinearizationHistories(Set.of(newHistory));

        var savedHistory = linearizationHistoryService.getExistingHistoryOrderedByRevision(newHistory.getWhoficEntityIri(),projectId);

        assertEquals(newHistory.getWhoficEntityIri(),savedHistory.getWhoficEntityIri());
    }

    @Test
    public void someTest2(){
        var projectId = ProjectId.generate();
        var newHistory = getEntityLinearizationHistory(projectId, 2);
        linearizationHistoryService.saveMultipleEntityLinearizationHistories(Set.of(newHistory));

        var savedHistory = linearizationHistoryService.getExistingHistoryOrderedByRevision(newHistory.getWhoficEntityIri(),projectId);

        assertEquals(newHistory.getWhoficEntityIri(),savedHistory.getWhoficEntityIri());
    }
}

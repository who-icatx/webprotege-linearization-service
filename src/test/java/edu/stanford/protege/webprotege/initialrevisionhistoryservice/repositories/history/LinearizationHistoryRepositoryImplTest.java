package edu.stanford.protege.webprotege.initialrevisionhistoryservice.repositories.history;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.InsertOneModel;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.*;
import org.bson.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils.EntityLinearizationHistoryHelper.getEntityLinearizationHistory;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@Import({WebprotegeLinearizationServiceServiceApplication.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@RunWith(SpringRunner.class)
public class LinearizationHistoryRepositoryImplTest extends IntegrationTest {

    @Autowired
    private LinearizationHistoryRepository linearizationHistoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void GIVEN_newLinearizationHistory_WHEN_historyIsSaved_THEN_weGetBackSavedHistory() {
        var projectId = ProjectId.generate();
        var newHistory = getEntityLinearizationHistory(projectId, 2);
        var savedHistory = linearizationHistoryRepository.saveLinearizationHistory(newHistory);
        assertEquals(newHistory,savedHistory);

        savedHistory = linearizationHistoryRepository.findHistoryByEntityIriAndProjectId(newHistory.getWhoficEntityIri(),projectId);

//        var getWithSpring = linearizationHistoryRepository.findWithSpringData(newHistory.getWhoficEntityIri(),projectId);

        assertEquals(newHistory,savedHistory);
//        assertEquals(newHistory,getWithSpring);
    }

    @Test
    public void test2() {
        var projectId = ProjectId.generate();
        var newHistory = getEntityLinearizationHistory(projectId, 2);
        var insertOne =  new InsertOneModel<>(objectMapper.convertValue(newHistory, Document.class));
        linearizationHistoryRepository.writeSingleHistory(newHistory);

//        var savedHistory = linearizationHistoryRepository.findHistoryByEntityIriAndProjectId(newHistory.getWhoficEntityIri(),projectId);
        var getWithSpring = linearizationHistoryRepository.findWithSpringData(newHistory.getWhoficEntityIri(),projectId);
        System.out.println(getWithSpring);

//        assertEquals(newHistory,savedHistory);

//        assertEquals(savedHistory,getWithSpring);
    }
}
package edu.stanford.protege.webprotege.initialrevisionhistoryservice;


import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.common.UserId;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.EntityLinearizationHistory;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.LinearizationRevision;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.internal.verification.Times;
import org.mockito.verification.VerificationMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.EntityLinearizationHistory.PROJECT_ID;
import static junit.framework.TestCase.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@SpringBootTest
@Import({WebprotegeLinearizationServiceServiceApplication.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@RunWith(SpringRunner.class)
public class UploadLinearizationCommandHandlerTest extends IntegrationTest {


    @MockBean
    private MinioLinearizationDocumentLoader minioLinearizationDocumentLoader;

    @SpyBean
    private MongoTemplate mongoTemplate;

    @Autowired
    UploadLinearizationCommandHandler commandHandler;

    @Before
    public void setUp() throws FileNotFoundException {
        File initialFile = new File("src/test/resources/TestLinearization.json");
        InputStream targetStream = new FileInputStream(initialFile);
        when(minioLinearizationDocumentLoader.fetchLinearizationDocument(any())).thenReturn(targetStream);
    }


    @Test
    public void GIVEN_largeLinearization_WHEN_savingTheInitialRevisions_THEN_paginationWorks() {
        commandHandler.handleRequest(new UploadLinearizationRequest("testDocumentLocation",
                ProjectId.valueOf("ecc61e85-bdb6-47f6-9bb1-664b64558f2b")),
                new ExecutionContext(UserId.valueOf("alexsilaghi"),"" ));

        verify(mongoTemplate, times(2)).getCollection(any());
    }

    @Test
    public void GIVEN_largeLinearization_WHEN_savingTheInitialRevision_THEN_eventsAreSavedIntoDatabase(){
        commandHandler.handleRequest(new UploadLinearizationRequest("testDocumentLocation",  ProjectId.valueOf("ecc61e85-bdb6-47f6-9bb1-664b64558f2b")),
                new ExecutionContext(UserId.valueOf("alexsilaghi"),"" ));
        var query = query(where(PROJECT_ID).is("ecc61e85-bdb6-47f6-9bb1-664b64558f2b"));

        List<EntityLinearizationHistory> histories = mongoTemplate.find(query, EntityLinearizationHistory.class);
        assertNotNull(histories);
        assertEquals(1, histories.size());

        EntityLinearizationHistory savedHistory = histories.get(0);

        assertEquals("http://id.who.int/icd/entity/979278646", savedHistory.whoficEntityIri().toString());
        assertEquals(1 , savedHistory.linearizationRevisions().size());
        LinearizationRevision revision = (LinearizationRevision) savedHistory.linearizationRevisions().toArray()[0];
        assertEquals("alexsilaghi", revision.userId());
        assertEquals(86, revision.linearizationEvents().size());
        assertTrue(revision.linearizationEvents().stream()
                .noneMatch(linearizationEvent -> linearizationEvent.getType() == null && linearizationEvent.getType().isEmpty()));

    }
 }

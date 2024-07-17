package edu.stanford.protege.webprotege.initialrevisionhistoryservice;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import edu.stanford.protege.webprotege.change.OntologyDocumentId;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.common.UserId;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.config.ApplicationBeans;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.EntityLinearizationHistory;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.LinearizationRevision;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.jackson.WebProtegeJacksonApplication;
import jakarta.ws.rs.core.Application;
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
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;
import static junit.framework.TestCase.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws FileNotFoundException {
        File initialFile = new File("src/test/resources/TestLinearization.json");
        InputStream targetStream = new FileInputStream(initialFile);
        when(minioLinearizationDocumentLoader.fetchLinearizationDocument(any())).thenReturn(targetStream);
        objectMapper = new WebProtegeJacksonApplication().objectMapper(new OWLDataFactoryImpl());
    }


    @Test
    public void GIVEN_largeLinearization_WHEN_savingTheInitialRevisions_THEN_paginationWorks() {
        commandHandler.handleRequest(new UploadLinearizationRequest(new OntologyDocumentId("testDocumentLocation"),
                ProjectId.valueOf("ecc61e85-bdb6-47f6-9bb1-664b64558f2b")),
                new ExecutionContext(UserId.valueOf("alexsilaghi"),"" ));

        verify(mongoTemplate, times(2)).getCollection(any());
    }

    @Test
    public void GIVEN_largeLinearization_WHEN_savingTheInitialRevision_THEN_eventsAreSavedIntoDatabase() throws JsonProcessingException {
        
        commandHandler.handleRequest(new UploadLinearizationRequest(new OntologyDocumentId("testDocumentLocation"),  ProjectId.valueOf("ecc61e85-bdb6-47f6-9bb1-664b64558f2b")),
                new ExecutionContext(UserId.valueOf("alexsilaghi"),"" ));

        Document filter = new Document("whoficEntityIri", "http://id.who.int/icd/entity/979278646");
        FindIterable<Document> documents = mongoTemplate.getCollection("EntityLinearizationHistories").find(filter);
        Stream<Document> docs = stream(documents.spliterator(), false);

        Document doc = docs.findFirst().get();
        EntityLinearizationHistory savedHistory = objectMapper.readValue(doc.toJson(), EntityLinearizationHistory.class);

        assertNotNull(savedHistory);
        assertEquals("http://id.who.int/icd/entity/979278646", savedHistory.getWhoficEntityIri().toString());
        assertEquals(1 , savedHistory.getLinearizationRevisions().size());
        LinearizationRevision revision = (LinearizationRevision) savedHistory.getLinearizationRevisions().toArray()[0];
        assertEquals("alexsilaghi", revision.userId());
        assertEquals(86, revision.linearizationEvents().size());
        assertTrue(revision.linearizationEvents().stream()
                .noneMatch(linearizationEvent -> linearizationEvent.getType() == null && linearizationEvent.getType().isEmpty()));

    }
 }

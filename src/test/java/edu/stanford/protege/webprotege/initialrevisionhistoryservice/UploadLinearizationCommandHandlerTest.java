package edu.stanford.protege.webprotege.initialrevisionhistoryservice;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.jackson.WebProtegeJacksonApplication;
import org.bson.Document;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.io.*;
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
        commandHandler.handleRequest(new UploadLinearizationRequest("testDocumentLocation",
                        ProjectId.valueOf("ecc61e85-bdb6-47f6-9bb1-664b64558f2b")),
                new ExecutionContext(UserId.valueOf("alexsilaghi"), ""));

        verify(mongoTemplate, times(2)).getCollection(any());
    }

    @Test
    public void GIVEN_largeLinearization_WHEN_savingTheInitialRevision_THEN_eventsAreSavedIntoDatabase() throws JsonProcessingException {
        UserId userId = UserId.valueOf("alexsilaghi");
        commandHandler.handleRequest(new UploadLinearizationRequest("testDocumentLocation", ProjectId.valueOf("ecc61e85-bdb6-47f6-9bb1-664b64558f2b")),
                new ExecutionContext(userId, ""));

        Document filter = new Document("whoficEntityIri", "http://id.who.int/icd/entity/979278646");
        FindIterable<Document> documents = mongoTemplate.getCollection("EntityLinearizationHistories").find(filter);
        Stream<Document> docs = stream(documents.spliterator(), false);

        Document doc = docs.findFirst().get();
        EntityLinearizationHistory savedHistory = objectMapper.readValue(doc.toJson(), EntityLinearizationHistory.class);

        assertNotNull(savedHistory);
        assertEquals("http://id.who.int/icd/entity/979278646", savedHistory.getWhoficEntityIri().toString());
        assertEquals(1, savedHistory.getLinearizationRevisions().size());
        LinearizationRevision revision = (LinearizationRevision) savedHistory.getLinearizationRevisions().toArray()[0];
        assertEquals(userId, revision.userId());
        assertEquals(86, revision.linearizationEvents().size());
        assertTrue(revision.linearizationEvents().stream()
                .noneMatch(linearizationEvent -> linearizationEvent.getType() == null && linearizationEvent.getType().isEmpty()));

    }
}

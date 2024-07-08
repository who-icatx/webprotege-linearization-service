package edu.stanford.protege.webprotege.initialrevisionhistoryservice;


import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.common.UserId;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Import({WebprotegeLinearizationServiceServiceApplication.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@RunWith(SpringRunner.class)
public class UploadLinearizationCommandHandlerTest extends IntegrationTest {


    @MockBean
    private MinioLinearizationDocumentLoader minioLinearizationDocumentLoader;

    @Autowired
    UploadLinearizationCommandHandler commandHandler;

    @Before
    public void setUp() throws FileNotFoundException {
        File initialFile = new File("src/test/resources/TestLinearization.json");
        InputStream targetStream = new FileInputStream(initialFile);
        when(minioLinearizationDocumentLoader.fetchLinearizationDocument(any())).thenReturn(targetStream);
    }


    @Test
    public void Test() {
        commandHandler.handleRequest(new UploadLinearizationRequest("testDocumentLocation", ProjectId.generate()), new ExecutionContext(UserId.valueOf("alexsilaghi"),"" ));
    }
 }

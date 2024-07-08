package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import edu.stanford.protege.webprotege.common.BlobLocation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
public class LinearizationRepositoryTest {

    @Mock
    private MinioLinearizationDocumentLoader minioLinearizationDocumentLoader;

    private LinearizationDocumentRepository linearizationRepository;

    @Before
    public void setUp(){
        when(minioLinearizationDocumentLoader.fetchLinearizationDocument(any())).thenReturn(new ByteArrayInputStream(LINEARIZATION_SPECIFICATIONS.getBytes(StandardCharsets.UTF_8)));
        linearizationRepository = new LinearizationDocumentRepository(minioLinearizationDocumentLoader, new ApplicationBeans().objectMapper());
    }

    @Test
    public void test(){
       var response = linearizationRepository.fetchFromDocument(new BlobLocation("asdasd", "asdasd")).collect(Collectors.toSet());
        System.out.println(response);
    }

    private final String LINEARIZATION_SPECIFICATIONS = """
            [
              {
                "whoficEntityLinearizationSpecification": {
                  "whoficEntityIri": "http://id.who.int/icd/entity/1912315151",
                  "linearizationSpecifications": [
                    {
                      "isAuxiliaryAxisChild": "false",
                      "isGrouping": "false",
                      "isIncludedInLinearization": "unknown",
                      "linearizationParent": "http://id.who.int/icd/entity/2221313",
                      "linearizationView": "http://id.who.int/icd/entity/5513123"
                    },
                    {
                      "isAuxiliaryAxisChild": "true",
                      "isGrouping": "unknown",
                      "isIncludedInLinearization": "unknown",
                      "linearizationParent": "http://id.who.int/icd/entity/11355",
                      "linearizationView": "http://id.who.int/icd/entity/77712312"
                    }
                  ]
                }
              },
              {
                "whoficEntityLinearizationSpecification": {
                  "whoficEntityIri": "http://id.who.int/icd/entity/9989888",
                  "linearizationSpecifications": [
                    {
                      "isAuxiliaryAxisChild": "true",
                      "isGrouping": "true",
                      "isIncludedInLinearization": "true",
                      "linearizationParent": "http://id.who.int/icd/entity/6123123",
                      "linearizationView": "http://id.who.int/icd/entity/1231236771"
                    },
                    {
                      "isAuxiliaryAxisChild": "unknown",
                      "isGrouping": "unknown",
                      "isIncludedInLinearization": "unknown",
                      "linearizationView": "http://id.who.int/icd/entity/77712312"
                    }
                  ]
                }
              }
            ]""";

}

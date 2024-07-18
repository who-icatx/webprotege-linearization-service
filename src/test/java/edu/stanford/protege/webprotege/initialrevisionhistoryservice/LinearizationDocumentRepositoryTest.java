package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import edu.stanford.protege.webprotege.common.BlobLocation;
import edu.stanford.protege.webprotege.jackson.WebProtegeJacksonApplication;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class LinearizationDocumentRepositoryTest {

    @Mock
    private MinioLinearizationDocumentLoader minioLinearizationDocumentLoader;

    private LinearizationDocumentRepository linearizationDocumentRepo;

    @BeforeEach
    public void setUp() {
        when(minioLinearizationDocumentLoader.fetchLinearizationDocument(any())).thenReturn(new ByteArrayInputStream(LINEARIZATION_SPECIFICATIONS.getBytes(StandardCharsets.UTF_8)));
        linearizationDocumentRepo = new LinearizationDocumentRepository(minioLinearizationDocumentLoader, new WebProtegeJacksonApplication().objectMapper(new OWLDataFactoryImpl()));
    }

    @Test
    public void test() {
        var response = linearizationDocumentRepo.fetchFromDocument(new BlobLocation("asdasd", "asdasd")).collect(Collectors.toSet());
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

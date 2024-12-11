package edu.stanford.protege.webprotege.linearizationservice.repositories.document;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import edu.stanford.protege.webprotege.linearizationservice.MinioLinearizationDocumentLoader;
import edu.stanford.protege.webprotege.linearizationservice.model.WhoficEntityLinearizationSpecification;
import org.slf4j.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.*;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2024-05-03
 */
@Service
public class LinearizationDocumentRepository {

    private final Logger logger = LoggerFactory.getLogger(LinearizationDocumentRepository.class);

    private final MinioLinearizationDocumentLoader minioLinearizationDocumentLoader;

    private final ObjectMapper objectMapper;


    public LinearizationDocumentRepository(MinioLinearizationDocumentLoader minioLinearizationDocumentLoader, ObjectMapper objectMapper) {
        this.minioLinearizationDocumentLoader = minioLinearizationDocumentLoader;
        this.objectMapper = objectMapper;
    }


    public Stream<WhoficEntityLinearizationSpecification> fetchFromDocument(String location) {

        try {
            JsonFactory jsonFactory = new JsonFactory();
            JsonParser jsonParser = jsonFactory.createParser(minioLinearizationDocumentLoader.fetchLinearizationDocument(location));

            if (jsonParser.nextToken() == JsonToken.START_ARRAY) {
                throw new IllegalStateException("Unexpected array");
            }

            jsonParser.nextToken();

            if (!jsonParser.getCurrentName().equals("whoficEntityLinearizationSpecification") && jsonParser.nextToken() != JsonToken.START_ARRAY) {
                throw new IllegalStateException("Expected the array of linearization specificaitons");
            }

            jsonParser.nextToken();

            return StreamSupport.stream(
                    new Spliterators.AbstractSpliterator<>(Long.MAX_VALUE, Spliterator.ORDERED) {
                        @Override
                        public boolean tryAdvance(Consumer<? super WhoficEntityLinearizationSpecification> action) {
                            try {

                                if (jsonParser.nextToken() == JsonToken.END_ARRAY) {
                                    return false;
                                }

                                JsonNode node = objectMapper.readTree(jsonParser);
                                WhoficEntityLinearizationSpecification person = objectMapper.treeToValue(node, WhoficEntityLinearizationSpecification.class);
                                action.accept(person);
                                return true;
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        }
                    }, false);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}

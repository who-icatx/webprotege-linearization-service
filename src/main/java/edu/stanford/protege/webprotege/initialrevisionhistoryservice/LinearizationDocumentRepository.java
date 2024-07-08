package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.webprotege.common.BlobLocation;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.WhoficEntityLinearizationSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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


    public Stream<WhoficEntityLinearizationSpecification> fetchFromDocument(BlobLocation location) {

        try {
            JsonFactory jsonFactory = new JsonFactory();
            JsonParser jsonParser = jsonFactory.createParser(minioLinearizationDocumentLoader.fetchLinearizationDocument(location));

            Iterator<WhoficEntityLinearizationSpecification> iterator = new Iterator<>() {
                @Override
                public boolean hasNext() {
                   return jsonParser.currentToken() != null && jsonParser.currentToken() != JsonToken.END_ARRAY;
                }

                @Override
                public WhoficEntityLinearizationSpecification next() {
                    try {
                        JsonNode node = objectMapper.readTree(jsonParser);
                        return objectMapper.treeToValue(node.get("whoficEntityLinearizationSpecification"), WhoficEntityLinearizationSpecification.class);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            };

            if (jsonParser.nextToken() != JsonToken.START_ARRAY) {
                throw new RuntimeException("Expected start of array");
            }

            Spliterator<WhoficEntityLinearizationSpecification> spliterator = Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED);
            return StreamSupport.stream(spliterator, false).onClose(() -> {
                try {
                    jsonParser.close();
                } catch (IOException e) {
                    logger.error("Error parsing the linearization document",e);
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}

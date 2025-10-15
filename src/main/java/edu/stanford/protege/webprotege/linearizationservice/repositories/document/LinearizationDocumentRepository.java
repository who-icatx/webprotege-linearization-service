package edu.stanford.protege.webprotege.linearizationservice.repositories.document;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ProtocolException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.stanford.protege.webprotege.linearizationservice.MinioLinearizationDocumentLoader;
import edu.stanford.protege.webprotege.linearizationservice.model.WhoficEntityLinearizationSpecification;

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
                                logger.error("Error in stream processing for document: {}", location, e);
                                
                                // Check if it's a connection issue that might be retryable
                                if (e instanceof ProtocolException || e.getMessage().contains("unexpected end of stream")) {
                                    logger.warn("Connection interrupted during stream processing, this might be retryable: {}", e.getMessage());
                                }
                                
                                throw new UncheckedIOException(e);
                            }
                        }
                    }, false);


        } catch (IOException e) {
            logger.error("Error in fetching the document: {}", location, e);
            
            // Provide more specific error information
            if (e instanceof ProtocolException || e.getMessage().contains("unexpected end of stream")) {
                logger.error("Connection was interrupted while fetching document from MinIO. This might be due to network issues or MinIO server problems.");
            }

            throw new RuntimeException("Failed to fetch linearization document: " + location, e);
        }
    }

    /**
     * Procesează un fișier local descărcat din MinIO
     * @param localFile calea către fișierul local
     * @return Stream de WhoficEntityLinearizationSpecification
     */
    public Stream<WhoficEntityLinearizationSpecification> fetchFromLocalFile(Path localFile) {
        try {
            logger.info("Processing local file: {}", localFile);
            
            JsonFactory jsonFactory = new JsonFactory();
            JsonParser jsonParser = jsonFactory.createParser(Files.newInputStream(localFile));

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
                                logger.error("Error in stream processing for local file: {}", localFile, e);
                                
                                if (e instanceof ProtocolException || e.getMessage().contains("unexpected end of stream")) {
                                    logger.warn("Connection interrupted during stream processing, this might be retryable: {}", e.getMessage());
                                }
                                
                                throw new UncheckedIOException(e);
                            }
                        }
                    }, false);

        } catch (IOException e) {
            logger.error("Error in processing local file: {}", localFile, e);
            
            if (e instanceof ProtocolException || e.getMessage().contains("unexpected end of stream")) {
                logger.error("Connection was interrupted while processing local file. This should not happen with local files.");
            }

            throw new RuntimeException("Failed to process local linearization document: " + localFile, e);
        }
    }
}

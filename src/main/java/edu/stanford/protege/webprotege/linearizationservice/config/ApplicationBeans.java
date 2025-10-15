package edu.stanford.protege.webprotege.linearizationservice.config;


import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.semanticweb.owlapi.model.IRI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import edu.stanford.protege.webprotege.authorization.GetAuthorizedCapabilitiesRequest;
import edu.stanford.protege.webprotege.authorization.GetAuthorizedCapabilitiesResponse;
import edu.stanford.protege.webprotege.common.UserId;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import edu.stanford.protege.webprotege.ipc.impl.CommandExecutorImpl;
import edu.stanford.protege.webprotege.jackson.WebProtegeJacksonApplication;
import edu.stanford.protege.webprotege.linearizationservice.config.serialization.IriDeserializer;
import edu.stanford.protege.webprotege.linearizationservice.config.serialization.IriSerializer;
import edu.stanford.protege.webprotege.linearizationservice.config.serialization.LinearizationCellStateSerializer;
import edu.stanford.protege.webprotege.linearizationservice.config.serialization.LinearizationDefinitionAccessibilityDeserializer;
import edu.stanford.protege.webprotege.linearizationservice.config.serialization.LinearizationDefinitionAccessibilitySerializer;
import edu.stanford.protege.webprotege.linearizationservice.config.serialization.ThreeStateBooleanDeserializer;
import edu.stanford.protege.webprotege.linearizationservice.config.serialization.UserIdDeserializer;
import edu.stanford.protege.webprotege.linearizationservice.config.serialization.UserIdSerializer;
import edu.stanford.protege.webprotege.linearizationservice.handlers.GetMatchingCriteriaRequest;
import edu.stanford.protege.webprotege.linearizationservice.handlers.GetMatchingCriteriaResponse;
import edu.stanford.protege.webprotege.linearizationservice.model.LinearizationDefinitionAccessibility;
import edu.stanford.protege.webprotege.linearizationservice.model.LinearizationStateCell;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

@Configuration
public class ApplicationBeans {



    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new WebProtegeJacksonApplication().objectMapper(new OWLDataFactoryImpl());
        SimpleModule module = new SimpleModule("linearizationModule");
        module.addDeserializer(LinearizationStateCell.class, new ThreeStateBooleanDeserializer());
        module.addSerializer(LinearizationStateCell.class, new LinearizationCellStateSerializer());
        module.addDeserializer(IRI.class, new IriDeserializer());
        module.addSerializer(IRI.class, new IriSerializer());
        module.addDeserializer(UserId.class, new UserIdDeserializer());
        module.addSerializer(UserId.class, new UserIdSerializer());
        module.addSerializer(LinearizationDefinitionAccessibility.class,
                new LinearizationDefinitionAccessibilitySerializer());
        module.addDeserializer(LinearizationDefinitionAccessibility.class,
                new LinearizationDefinitionAccessibilityDeserializer());
        objectMapper.registerModule(module);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        return objectMapper;
    }

    @Bean
    public MongoCustomConversions customConversions(ObjectMapper objectMapper) {
        return new MongoCustomConversions(
                List.of(
                        new LinearizationEventReadingConverter(objectMapper),
                        new LinearizationEventWritingConverter(objectMapper)
                )
        );
    }


    @Bean
    CommandExecutor<GetAuthorizedCapabilitiesRequest, GetAuthorizedCapabilitiesResponse> getAuthorizedActionsExecutorCommand() {
        return new CommandExecutorImpl<>(GetAuthorizedCapabilitiesResponse.class);
    }

    @Bean
    CommandExecutor<GetMatchingCriteriaRequest, GetMatchingCriteriaResponse> getMatchingCriteriaExecutorCommand() {
        return new CommandExecutorImpl<>(GetMatchingCriteriaResponse.class);
    }


    @Bean
    public ReadWriteLock readWriteLock() {
        return new ReentrantReadWriteLock(true);
    }

    @Bean("taskExecutor")
    public Executor taskExecutor() {
        return new ThreadPoolExecutor(
            2, // core pool size
            10, // maximum pool size
            60L, TimeUnit.SECONDS, // keep alive time
            new LinkedBlockingQueue<>(100), // queue capacity
            r -> {
                Thread t = new Thread(r, "linearization-task-" + System.currentTimeMillis());
                t.setDaemon(true);
                return t;
            }
        );
    }

}

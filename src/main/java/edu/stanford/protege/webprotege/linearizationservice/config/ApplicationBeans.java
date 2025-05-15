package edu.stanford.protege.webprotege.linearizationservice.config;


import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import edu.stanford.protege.webprotege.authorization.GetAuthorizedCapabilitiesRequest;
import edu.stanford.protege.webprotege.authorization.GetAuthorizedCapabilitiesResponse;
import edu.stanford.protege.webprotege.authorization.GetProjectRoleDefinitionsRequest;
import edu.stanford.protege.webprotege.authorization.GetProjectRoleDefinitionsResponse;
import edu.stanford.protege.webprotege.common.UserId;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import edu.stanford.protege.webprotege.ipc.impl.CommandExecutorImpl;
import edu.stanford.protege.webprotege.jackson.WebProtegeJacksonApplication;
import edu.stanford.protege.webprotege.linearizationservice.*;
import edu.stanford.protege.webprotege.linearizationservice.handlers.GetMatchingCriteriaRequest;
import edu.stanford.protege.webprotege.linearizationservice.handlers.GetMatchingCriteriaResponse;
import edu.stanford.protege.webprotege.linearizationservice.model.ThreeStateBoolean;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.context.annotation.*;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.List;
import java.util.concurrent.locks.*;

@Configuration
public class ApplicationBeans {


    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new WebProtegeJacksonApplication().objectMapper(new OWLDataFactoryImpl());
        SimpleModule module = new SimpleModule("linearizationModule");
        module.addDeserializer(ThreeStateBoolean.class, new ThreeStateBooleanDeserializer());
        module.addSerializer(ThreeStateBoolean.class, new ThreeStateBooleanSerializer());
        module.addDeserializer(IRI.class, new IriDeserializer());
        module.addSerializer(IRI.class, new IriSerializer());
        module.addDeserializer(UserId.class, new UserIdDeserializer());
        module.addSerializer(UserId.class, new UserIdSerializer());
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
}

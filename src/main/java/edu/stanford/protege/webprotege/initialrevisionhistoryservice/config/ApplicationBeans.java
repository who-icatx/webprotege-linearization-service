package edu.stanford.protege.webprotege.initialrevisionhistoryservice.config;


import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.ThreeStateBoolean;
import edu.stanford.protege.webprotege.jackson.WebProtegeJacksonApplication;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.context.annotation.*;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

@Configuration
public class ApplicationBeans {


    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new WebProtegeJacksonApplication().objectMapper(new OWLDataFactoryImpl());
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ThreeStateBoolean.class, new ThreeStateBooleanDeserializer());
        module.addDeserializer(IRI.class, new IriDeserializer());
        objectMapper.registerModule(module);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        return objectMapper;
    }


}

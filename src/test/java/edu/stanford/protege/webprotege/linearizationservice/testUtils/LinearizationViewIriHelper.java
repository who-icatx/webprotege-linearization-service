package edu.stanford.protege.webprotege.linearizationservice.testUtils;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.webprotege.jackson.WebProtegeJacksonApplication;
import edu.stanford.protege.webprotege.linearizationservice.model.LinearizationDefinition;
import org.semanticweb.owlapi.model.IRI;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class LinearizationViewIriHelper {



    public static List<IRI> getLinearizationViewIris() {
        ObjectMapper objectMapper = new WebProtegeJacksonApplication().objectMapper(new OWLDataFactoryImpl());
        try {
            FileInputStream fileInputStream = new FileInputStream("src/test/resources/LinearizationDefinitions.json");
            List<LinearizationDefinition> definitions = objectMapper.readValue(fileInputStream, new TypeReference<>() {
            });
            return definitions.stream().map(def -> IRI.create(def.getLinearizationUri())).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

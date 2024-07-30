package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;


import com.fasterxml.jackson.annotation.*;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;


@BsonDiscriminator(key = "type")
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME
)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = SetAuxiliaryAxisChild.class, name = SetAuxiliaryAxisChild.CLASS_TYPE),
        @JsonSubTypes.Type(value = SetCodingNote.class, name = SetCodingNote.CLASS_TYPE),
        @JsonSubTypes.Type(value = SetGrouping.class, name = SetGrouping.CLASS_TYPE),
        @JsonSubTypes.Type(value = SetIncludedInLinearization.class, name = SetIncludedInLinearization.CLASS_TYPE),
        @JsonSubTypes.Type(value = SetLinearizationParent.class, name = SetLinearizationParent.CLASS_TYPE),
        @JsonSubTypes.Type(value = SetSuppressedSpecifiedResidual.class, name = SetSuppressedSpecifiedResidual.CLASS_TYPE),
        @JsonSubTypes.Type(value = SetUnspecifiedResidualTitle.class, name = SetUnspecifiedResidualTitle.CLASS_TYPE),
})

public interface LinearizationEvent {

    @JsonProperty("@type")
    String getType();

    EventProcesableParameter applyEvent(EventProcesableParameter input);

    String getValue();
}

package edu.stanford.protege.webprotege.linearizationservice.events;


import com.fasterxml.jackson.annotation.*;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;


@BsonDiscriminator(key = "type")
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME
)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = SetAuxiliaryAxisChild.class, name = SetAuxiliaryAxisChild.DISCRIMINATOR),
        @JsonSubTypes.Type(value = SetCodingNote.class, name = SetCodingNote.DISCRIMINATOR),
        @JsonSubTypes.Type(value = SetGrouping.class, name = SetGrouping.DISCRIMINATOR),
        @JsonSubTypes.Type(value = SetIncludedInLinearization.class, name = SetIncludedInLinearization.DISCRIMINATOR),
        @JsonSubTypes.Type(value = SetLinearizationParent.class, name = SetLinearizationParent.DISCRIMINATOR),
        @JsonSubTypes.Type(value = SetSuppressedOtherSpecifiedResidual.class, name = SetSuppressedOtherSpecifiedResidual.DISCRIMINATOR),
        @JsonSubTypes.Type(value = SetSuppressedUnspecifiedResiduals.class, name = SetSuppressedUnspecifiedResiduals.DISCRIMINATOR),
        @JsonSubTypes.Type(value = SetOtherSpecifiedResidualTitle.class, name = SetOtherSpecifiedResidualTitle.DISCRIMINATOR),
        @JsonSubTypes.Type(value = SetUnspecifiedResidualTitle.class, name = SetUnspecifiedResidualTitle.DISCRIMINATOR),
})

public interface LinearizationEvent {

    @JsonProperty("@type")
    String getType();

    EventProcesableParameter applyEvent(EventProcesableParameter input);

    String getValue();
}

package edu.stanford.protege.webprotege.linearizationservice.events;


import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.changes.LinearizationChangeVisitor;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

import javax.annotation.Nonnull;


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
        @JsonSubTypes.Type(value = SetSuppressedOtherSpecifiedResidual.class, name = SetSuppressedOtherSpecifiedResidual.CLASS_TYPE),
        @JsonSubTypes.Type(value = SetSuppressedUnspecifiedResiduals.class, name = SetSuppressedUnspecifiedResiduals.CLASS_TYPE),
        @JsonSubTypes.Type(value = SetOtherSpecifiedResidualTitle.class, name = SetOtherSpecifiedResidualTitle.CLASS_TYPE),
        @JsonSubTypes.Type(value = SetUnspecifiedResidualTitle.class, name = SetUnspecifiedResidualTitle.CLASS_TYPE),
})

public interface LinearizationEvent {

    @JsonProperty("@type")
    String getType();

    EventProcesableParameter applyEvent(EventProcesableParameter input);

    String getValue();

    <R> R accept(@Nonnull LinearizationChangeVisitor<R> visitor);

    default String getUiDisplayName() {
        var eventNameWithSpaces = this.getClass().getSimpleName().replaceAll("([a-z])([A-Z]+)", "$1 $2");
        eventNameWithSpaces = eventNameWithSpaces.replaceFirst("^Set", "");
        return eventNameWithSpaces.toLowerCase();
    }
}

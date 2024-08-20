package edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.diff;

import edu.stanford.protege.webprotege.entity.EntityNode;
import edu.stanford.protege.webprotege.linearizationservice.events.*;
import edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.changes.*;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.List;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 26/02/15
 */
public class DiffElementRenderer<S extends Serializable> {

    private final LinearizationChangeVisitor<String> visitor;

    public DiffElementRenderer(List<EntityNode> renderedEntities) {
        visitor = new LinearizationChangeVisitor<>() {
            @Override
            public String visit(@NotNull SetAuxiliaryAxisChild setAuxiliaryAxisChild) {
                return renderHtmlForElement(setAuxiliaryAxisChild.getUiDisplayName(), setAuxiliaryAxisChild.getValue().toLowerCase());
            }

            @Override
            public String visit(SetCodingNote setCodingNote) {
                return renderHtmlForElement(setCodingNote.getUiDisplayName(), setCodingNote.getValue());
            }

            @Override
            public String visit(SetGrouping setGrouping) {
                return renderHtmlForElement(setGrouping.getUiDisplayName(), setGrouping.getValue().toLowerCase());
            }

            @Override
            public String visit(SetIncludedInLinearization setIncludedInLinearization) {
                return renderHtmlForElement(setIncludedInLinearization.getUiDisplayName(), setIncludedInLinearization.getValue().toLowerCase());
            }

            @Override
            public String visit(SetLinearizationParent setLinearizationParent) {
                if (renderedEntities == null) {
                    return renderHtmlForElement(setLinearizationParent.getUiDisplayName(), setLinearizationParent.getValue());
                }
                var renderedLinParentOptional = renderedEntities.stream()
                        .filter(renderedEntity -> renderedEntity.getEntity().getIRI().toString().equals(setLinearizationParent.getValue()))
                        .findFirst();

                return renderedLinParentOptional
                        .map(entityNode -> renderHtmlForElement(setLinearizationParent.getUiDisplayName(), entityNode.getBrowserText()))
                        .orElseGet(() -> renderHtmlForElement(setLinearizationParent.getUiDisplayName(), setLinearizationParent.getValue()));
            }

            @Override
            public String visit(SetOtherSpecifiedResidualTitle setOtherSpecifiedResidualTitle) {
                return renderHtmlForElement(setOtherSpecifiedResidualTitle.getUiDisplayName(), setOtherSpecifiedResidualTitle.getValue());
            }

            @Override
            public String visit(SetSuppressedOtherSpecifiedResidual setSuppressedOtherSpecifiedResidual) {
                return renderHtmlForElement(setSuppressedOtherSpecifiedResidual.getUiDisplayName(), setSuppressedOtherSpecifiedResidual.getValue().toLowerCase());
            }

            @Override
            public String visit(SetSuppressedUnspecifiedResiduals setSuppressedUnspecifiedResiduals) {
                return renderHtmlForElement(setSuppressedUnspecifiedResiduals.getUiDisplayName(), setSuppressedUnspecifiedResiduals.getValue().toLowerCase());
            }

            @Override
            public String visit(SetUnspecifiedResidualTitle unspecifiedResidualTitle) {
                return renderHtmlForElement(unspecifiedResidualTitle.getUiDisplayName(), unspecifiedResidualTitle.getValue());
            }

            @Override
            public String getDefaultReturnValue() {
                throw new RuntimeException();
            }
        };
    }


    public DiffElement<String, String> render(DiffElement<LinearizationDocumentChange, LinearizationEventsForView> element) {
        var eventsByViews = element.getLineElement();
        var rederedLine = renderLine(eventsByViews);
        var source = element.getSourceDocument();
        var renderedSource = renderSource(source);
        rederedLine = rederedLine != null ? rederedLine : "no value";
        return new DiffElement<>(
                element.getDiffOperation(),
                renderedSource,
                rederedLine
        );
    }

    private String renderSource(LinearizationDocumentChange source) {
        final StringBuilder stringBuilder = new StringBuilder();

        var displayLabel = source.getLinearizationViewName() != null ? source.getLinearizationViewName() : source.getLinearizationViewId();

        stringBuilder.append("<span class=\"ms-quantifier-kw\">");
        stringBuilder.append(displayLabel);
        stringBuilder.append("</span>");

        return stringBuilder.toString();
    }

    public String renderLine(LinearizationEventsForView change) {
        final StringBuilder stringBuilder = new StringBuilder();
        change.getLinearizationEvents().sort(OrderedEventClasses.getEventClassComparator());
        change.getLinearizationEvents()
                .forEach(event -> stringBuilder.append(event.accept(visitor)));

        return stringBuilder.toString();
    }

    private String renderHtmlForElement(String elementName, String elementValue) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("&nbsp;<span>");
        stringBuilder.append("Set the ");
        stringBuilder.append(elementName);
        stringBuilder.append(" value to ");
        stringBuilder.append("<span class=\"ms-literal\">\"");
        stringBuilder.append(elementValue);
        stringBuilder.append("\"</span>");
        stringBuilder.append("</span>;&nbsp;");

        return stringBuilder.toString();
    }
}

package edu.stanford.protege.webprotege.initialrevisionhistoryservice.uiHistoryConcern.diff;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.uiHistoryConcern.changes.*;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 26/02/15
 */
public class DiffElementRenderer<S extends Serializable> {

    private final LinearizationChangeVisitor<String> visitor;

    public DiffElementRenderer() {
        visitor = new LinearizationChangeVisitor<>() {
            @Override
            public String visit(@NotNull SetAuxiliaryAxisChild setAuxiliaryAxisChild) {
                return renderHtmlForElement(setAuxiliaryAxisChild.getClass().getSimpleName(), setAuxiliaryAxisChild.getValue());
            }

            @Override
            public String visit(SetCodingNote setCodingNote) {
                return renderHtmlForElement(setCodingNote.getClass().getSimpleName(), setCodingNote.getValue());

            }

            @Override
            public String visit(SetGrouping setGrouping) {
                return renderHtmlForElement(setGrouping.getClass().getSimpleName(), setGrouping.getValue());

            }

            @Override
            public String visit(SetIncludedInLinearization setIncludedInLinearization) {
                return renderHtmlForElement(setIncludedInLinearization.getClass().getSimpleName(), setIncludedInLinearization.getValue());

            }

            @Override
            public String visit(SetLinearizationParent setLinearizationParent) {
                return renderHtmlForElement(setLinearizationParent.getClass().getSimpleName(), setLinearizationParent.getValue());

            }

            @Override
            public String visit(SetOtherSpecifiedResidualTitle setOtherSpecifiedResidualTitle) {
                return renderHtmlForElement(setOtherSpecifiedResidualTitle.getClass().getSimpleName(), setOtherSpecifiedResidualTitle.getValue());

            }

            @Override
            public String visit(SetSuppressedOtherSpecifiedResidual setSuppressedOtherSpecifiedResidual) {
                return renderHtmlForElement(setSuppressedOtherSpecifiedResidual.getClass().getSimpleName(), setSuppressedOtherSpecifiedResidual.getValue());

            }

            @Override
            public String visit(SetSuppressedUnspecifiedResiduals setSuppressedUnspecifiedResiduals) {
                return renderHtmlForElement(setSuppressedUnspecifiedResiduals.getClass().getSimpleName(), setSuppressedUnspecifiedResiduals.getValue());

            }

            @Override
            public String visit(SetUnspecifiedResidualTitle unspecifiedResidualTitle) {
                return renderHtmlForElement(unspecifiedResidualTitle.getClass().getSimpleName(), unspecifiedResidualTitle.getValue());

            }

            @Override
            public String getDefaultReturnValue() {
                throw  new RuntimeException();
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

        var displayLabel = source.getLinearizationViewName()!=null?source.getLinearizationViewName():source.getLinearizationViewId();

        renderPlainLink(source.getLinearizationViewIri(), displayLabel, stringBuilder);

        return stringBuilder.toString();
    }

    public String renderLine(LinearizationEventsForView change) {
        final StringBuilder stringBuilder = new StringBuilder();
        change.getLinearizationEvents()
                .forEach(event -> stringBuilder.append(event.accept(visitor)));

        return stringBuilder.toString();
    }

    private String renderHtmlForElement(String elementName, String elementValue) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<br><span>");
        stringBuilder.append(elementName);
        stringBuilder.append(" - ");
        stringBuilder.append("<span class=\"ms-literal\">\"");
        stringBuilder.append(elementValue);
        stringBuilder.append("\"</span>");
        stringBuilder.append("</span><br>");

        return stringBuilder.toString();
    }

    private void renderPlainLink(String link, String display, StringBuilder builder) {
        builder.append("<a target=\"_blank\" href=\"");
        builder.append(link);
        builder.append("\">");
        builder.append("<span class=\"iri\">");
        builder.append(display);
        builder.append("</span>");
        builder.append("</a>");
    }
}

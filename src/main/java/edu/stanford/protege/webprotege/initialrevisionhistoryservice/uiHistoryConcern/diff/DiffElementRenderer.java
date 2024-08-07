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


    public DiffElement<S, String> render(DiffElement<S, LinearizationEventsForView> element) {
        var eventsByViews = element.getLineElement();
        var rederedLine = renderData(eventsByViews);
        rederedLine = rederedLine != null ? rederedLine : "no value";
        return new DiffElement<>(
                element.getDiffOperation(),
                element.getSourceDocument(),
                rederedLine
        );
    }

    public String renderData(LinearizationEventsForView change) {
        final StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("'");
        stringBuilder.append("<span title=\"").append("CLASS").append(": ");
        stringBuilder.append(change.getViewName());
        stringBuilder.append("\" class=\"").append("highlight").append("\">");
        stringBuilder.append(change.getViewName().replace(" ", "&nbsp;"));
        stringBuilder.append("</span>");
        stringBuilder.append("'");

        change.getLinearizationEvents()
                .forEach(event -> stringBuilder.append(event.accept(visitor)));

        return stringBuilder.toString();
    }

    private String renderHtmlForElement(String elementName, String elementValue) {

        String htmlElement = "<br>'" +
                "<span>" +
                elementName +
                " - " +
                elementValue +
                "</span>" +
                "'<br>";

        return htmlElement;
    }
}

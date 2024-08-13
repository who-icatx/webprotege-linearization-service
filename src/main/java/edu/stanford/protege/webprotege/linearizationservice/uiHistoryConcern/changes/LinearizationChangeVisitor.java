package edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.changes;

import javax.annotation.Nonnull;

public interface LinearizationChangeVisitor<R> {
    R visit(@Nonnull SetAuxiliaryAxisChild setAuxiliaryAxisChild);

    R visit(SetCodingNote setCodingNote);

    R visit(SetGrouping setGrouping);

    R visit(SetIncludedInLinearization setIncludedInLinearization);

    R visit(SetLinearizationParent setLinearizationParent);

    R visit(SetOtherSpecifiedResidualTitle setOtherSpecifiedResidualTitle);

    R visit(SetSuppressedOtherSpecifiedResidual setSuppressedOtherSpecifiedResidual);

    R visit(SetSuppressedUnspecifiedResiduals setSuppressedUnspecifiedResiduals);

    R visit(SetUnspecifiedResidualTitle unspecifiedResidualTitle);

    R getDefaultReturnValue();
}

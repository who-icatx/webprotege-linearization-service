package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;

import javax.annotation.Nonnull;

public interface EventChangeVisitor {

    default void visit(@Nonnull SetAuxiliaryAxisChild setAuxiliaryAxisChildChange) {
    }

    default void visit(@Nonnull SetCodingNote setCodingNoteChange) {
    }

    default void visit(@Nonnull SetGrouping setGroupingChange) {
    }

    default void visit(@Nonnull SetIncludedInLinearization setIncludedInLinearizationChange) {
    }

    default void visit(@Nonnull SetLinearizationParent setLinearizationParentChange) {
    }


    default void visit(SetUnspecifiedResidualTitle setUnspecifiedResidualTitle) {

    }


    default void visit(SetSuppressedSpecifiedResidual setSuppressedSpecifiedResidual) {

    }

}

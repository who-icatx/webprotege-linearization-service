package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;

import edu.stanford.protege.webprotege.change.*;

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
}

package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;


public interface EventVisitor {

    default void visit(SetAuxiliaryAxisChild setAuxiliaryAxisChildChange) {
    }

    default void visit(SetCodingNote setCodingNoteChange) {
    }

    default void visit(SetGrouping setGroupingChange) {
    }

    default void visit(SetIncludedInLinearization setIncludedInLinearizationChange) {
    }

    default void visit(SetLinearizationParent setLinearizationParentChange) {
    }


    default void visit(SetUnspecifiedResidualTitle setUnspecifiedResidualTitle) {

    }


    default void visit(SetSuppressedSpecifiedResidual setSuppressedSpecifiedResidual) {

    }

}

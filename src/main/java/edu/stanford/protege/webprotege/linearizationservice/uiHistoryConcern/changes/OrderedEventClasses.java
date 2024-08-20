package edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.changes;

import edu.stanford.protege.webprotege.linearizationservice.events.*;

import java.util.*;

public class OrderedEventClasses {

    //Something done fast. if the Linearization table columns change order then we also have to change the order here.
    //This was done because it seems that we want to display the order of the events in the revision for a specific
    //linearization view(e.g. MMS view) in the same order we display them in the Linearization form/tab/uiView
    private static List<Class<? extends LinearizationEvent>> getListOfOrderForLinearizationEventClasses(){

        return List.of(
                SetIncludedInLinearization.class,
                SetGrouping.class,
                SetAuxiliaryAxisChild.class,
                SetLinearizationParent.class,
                SetCodingNote.class,
                SetSuppressedOtherSpecifiedResidual.class,
                SetSuppressedUnspecifiedResiduals.class,
                SetOtherSpecifiedResidualTitle.class,
                SetUnspecifiedResidualTitle.class
        );
    }

    public static Comparator<LinearizationEvent> getEventClassComparator(){
        return Comparator.comparingInt(event -> {
            int index = getListOfOrderForLinearizationEventClasses().indexOf(event.getClass());
            return index == -1 ? Integer.MAX_VALUE : index; // If class is not found, place it at the end
        });
    }

}

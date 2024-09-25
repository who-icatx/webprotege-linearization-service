package edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.changes;

import edu.stanford.protege.webprotege.change.ProjectChange;
import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotege.diff.DiffElement;
import edu.stanford.protege.webprotege.entity.EntityNode;
import edu.stanford.protege.webprotege.linearizationservice.model.*;
import edu.stanford.protege.webprotege.linearizationservice.repositories.definitions.LinearizationDefinitionRepository;
import edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.diff.*;
import edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.nodeRendering.EntityRendererManager;
import edu.stanford.protege.webprotege.revision.RevisionNumber;
import org.slf4j.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.*;

import static edu.stanford.protege.webprotege.linearizationservice.mappers.LinearizationEventMapper.groupEventsByViews;


@Component
public class ProjectChangesManager {

    private static final Logger logger = LoggerFactory.getLogger(ProjectChangesManager.class);

    private final Revision2DiffElementsTranslator revision2DiffElementsTranslator;

    private final LinearizationDefinitionRepository definitionRepository;

    private final EntityRendererManager entityRendererManager;

    public ProjectChangesManager(
            Revision2DiffElementsTranslator revision2DiffElementsTranslator,
            LinearizationDefinitionRepository definitionRepository,
            EntityRendererManager entityRendererManager) {
        this.revision2DiffElementsTranslator = revision2DiffElementsTranslator;
        this.definitionRepository = definitionRepository;
        this.entityRendererManager = entityRendererManager;
    }


    public Set<ProjectChangeForEntity> getProjectChangesForHistories(ProjectId projectId, List<EntityLinearizationHistory> entityLinearizationHistories) {
        Map<String, String> entityIrisAndNames = new HashMap<>();
        Set<LinearizationRevisionWithEntity> linRevisions = entityLinearizationHistories.stream()
                .flatMap(history ->
                        history.getLinearizationRevisions()
                                .stream()
                                .map(revision -> new LinearizationRevisionWithEntity(revision, history.getWhoficEntityIri()))
                )
                .sorted(Comparator.comparing(LinearizationRevisionWithEntity::getRevision))
                .peek(revisionWithEntity -> entityIrisAndNames.put(revisionWithEntity.getWhoficEntityIri(), revisionWithEntity.getWhoficEntityIri()))
                .collect(Collectors.toSet());

        List<EntityNode> renderedEntitiesList = entityRendererManager.getRenderedEntities(entityIrisAndNames.keySet(), projectId);
        var linearizationDefinitions = definitionRepository.getLinearizationDefinitions();

        linRevisions.forEach(revisionWithEntity -> {
            var entityTextOptional = renderedEntitiesList
                    .stream()
                    .filter(entityNode -> entityNode.getEntity().getIRI().toString().equals(revisionWithEntity.getWhoficEntityIri()))
                    .map(EntityNode::getBrowserText)
                    .findFirst();
            entityTextOptional.ifPresent(browserText -> entityIrisAndNames.put(revisionWithEntity.getWhoficEntityIri(), browserText));
        });

        Set<ProjectChangeForEntity> projectChangeForEntityList = linRevisions.stream()
                .flatMap(revisionWithEntity -> {
                            ProjectChange projectChange = getProjectChangesForRevision(
                                    revisionWithEntity.getRevision(),
                                    entityIrisAndNames.get(revisionWithEntity.getWhoficEntityIri()),
                                    renderedEntitiesList,
                                    linearizationDefinitions
                            );
                            ProjectChangeForEntity projectChangeForEntity = ProjectChangeForEntity.create(
                                    revisionWithEntity.getWhoficEntityIri(),
                                    projectChange
                            );
                            return Stream.of(projectChangeForEntity);
                        }
                )
                .collect(Collectors.toSet());

        return projectChangeForEntityList;
    }

    public ProjectChangeForEntity getProjectChangesForRevision(ProjectId projectId, String whoficEntityIri, LinearizationRevision linearizationRevision) {
        Map<String, String> entityIrisAndNames = new HashMap<>();
        entityIrisAndNames.put(whoficEntityIri, whoficEntityIri);
        List<EntityNode> renderedEntitiesList = entityRendererManager.getRenderedEntities(Set.of(whoficEntityIri), projectId);
        var linearizationDefinitions = definitionRepository.getLinearizationDefinitions();

        var entityTextOptional = renderedEntitiesList
                .stream()
                .filter(entityNode -> entityNode.getEntity().getIRI().toString().equals(whoficEntityIri))
                .map(EntityNode::getBrowserText)
                .findFirst();
        entityTextOptional.ifPresent(browserText -> entityIrisAndNames.put(whoficEntityIri, browserText));

        ProjectChange projectChange = getProjectChangesForRevision(
                linearizationRevision,
                entityIrisAndNames.get(whoficEntityIri),
                renderedEntitiesList,
                linearizationDefinitions
        );

        ProjectChangeForEntity projectChangeForEntity = ProjectChangeForEntity.create(
                whoficEntityIri,
                projectChange
        );

        return projectChangeForEntity;
    }

    private ProjectChange getProjectChangesForRevision(LinearizationRevision revision,
                                                       String subjectName,
                                                       List<EntityNode> renderedEntities,
                                                       List<LinearizationDefinition> linearizationDefinitions) {
        final int totalChanges;
        var changesByView = groupEventsByViews(revision.linearizationEvents().stream().toList());
        totalChanges = changesByView.size();

        List<DiffElement<LinearizationDocumentChange, LinearizationEventsForView>> diffElements = revision2DiffElementsTranslator.getDiffElementsFromRevision(changesByView, linearizationDefinitions);
        List<DiffElement<LinearizationDocumentChange, LinearizationEventsForView>> mutableDiffElements = new ArrayList<>(diffElements);
        mutableDiffElements.sort(Comparator.comparing(diffElement -> diffElement.getSourceDocument().getSortingCode()));

        List<DiffElement<String, String>> renderedDiffElements = renderDiffElements(mutableDiffElements, renderedEntities);
        int pageElements = renderedDiffElements.size();
        int pageCount;
        if (pageElements == 0) {
            pageCount = 1;
        } else {
            pageCount = totalChanges / pageElements + (totalChanges % pageElements);
        }
        Page<DiffElement<String, String>> page = Page.create(
                1,
                pageCount,
                renderedDiffElements,
                totalChanges
        );
        ProjectChange projectChange = ProjectChange.get(
                RevisionNumber.valueOf("0"),
                revision.userId(),
                revision.timestamp(),
                "Edited Linearization for Entity: " + subjectName,
                totalChanges,
                page);
        return projectChange;
    }

    private List<DiffElement<String, String>> renderDiffElements(List<DiffElement<LinearizationDocumentChange, LinearizationEventsForView>> diffElements, List<EntityNode> renderedEntities) {

        List<DiffElement<String, String>> renderedDiffElements = new ArrayList<>();
        DiffElementRenderer<String> renderer = new DiffElementRenderer<>(renderedEntities);
        for (DiffElement<LinearizationDocumentChange, LinearizationEventsForView> diffElement : diffElements) {
            renderedDiffElements.add(renderer.render(diffElement));
        }
        return renderedDiffElements;
    }
}

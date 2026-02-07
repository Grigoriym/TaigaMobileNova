# Kanban Filters Design

## Overview

Kanban board displays user stories grouped by status columns. Stories can optionally be grouped by swimlanes. Users can filter the visible stories using criteria: assignees, created by, tags, epics, roles.

## Filtering Logic

- Selecting a filter shows only stories matching that filter
- Multiple filter categories combine with AND (must match assignee AND tag AND ...)
- Multiple selections within one category combine with OR (assignee Alice OR Bob)
- Removing all filters shows all stories

## Swimlane Modes

There are three distinct swimlane states. The `Swimlane` model has a special "Unclassified" instance (`id = -1L`, `isUnclassified = true`) for stories with no swimlane assigned.

### 1. No swimlanes in project

`selectedSwimlane == null`, `swimlanes` list is empty.

- All stories are shown (no swimlane grouping)
- Filter picker shows **all** project-wide options from the API
- This is the simple case

### 2. Specific swimlane selected

`selectedSwimlane != null`, `selectedSwimlane.isUnclassified == false`.

- Only stories where `story.swimlane == selectedSwimlane.id` are shown
- Filter picker shows only options relevant to this swimlane's stories

### 3. "Unclassified" swimlane selected

`selectedSwimlane != null`, `selectedSwimlane.isUnclassified == true`.

- Only stories where `story.swimlane == null` are shown (stories not assigned to any swimlane)
- Filter picker shows only options relevant to these unclassified stories
- This is a synthetic swimlane added by `buildSwimlanesWithUnclassified` when there are stories with no swimlane AND the project has swimlanes configured

## Decisions

### Available filter options: Swimlane-scoped

When viewing a swimlane, the filter picker shows only assignees/tags/epics/roles that exist in that swimlane's stories. This prevents selecting a filter that would always return zero results.

When no swimlane is active (mode 1), all project-wide options are shown.

### Filter state: Independent per swimlane

Each swimlane has its own filter selections. Switching from Swimlane A to B does not affect A's filters. Going back to A restores A's selections.

Stored in `filtersBySwimlane: Map<Long?, FiltersData>` keyed by swimlane ID.

### Default for new swimlane: No filters

When visiting a swimlane for the first time, no filters are active. All stories in that swimlane are shown.

### Filter counts: Compute locally

The API returns project-wide counts per filter option. When scoped to a swimlane, these counts are misleading. Counts should be recomputed locally from the swimlane's stories. If that adds too much complexity, hide counts instead.

### Session persistence: Deferred

Not in scope for now. Filters start fresh on each session. Will revisit later.

## Implementation Notes

### Story filtering by swimlane must use 3-way logic

The same logic as `GetKanbanDataUseCase.filterStoriesBySwimlane`:

```
when {
    swimlane == null       -> all stories (no swimlanes)
    swimlane.isUnclassified -> stories where story.swimlane == null
    else                    -> stories where story.swimlane == swimlane.id
}
```

### Race condition between getKanbanData and loadFiltersData

Both launch in `init` concurrently. Scoped filter computation needs both stories (from `getKanbanData`) and project-wide filter options (from `loadFiltersData`). Whichever finishes first cannot compute correct scoped filters alone.

Solution: each function should attempt to compute scoped filters if the other's data is available. If not, scoped filters will be computed when the second one completes.

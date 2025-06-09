package io.eugenethedev.taigamobile.core.nav

object Routes {
    const val login = "login"
    const val dashboard = "dashboard"
    const val scrum = "scrum"
    const val epics = "epics"
    const val issues = "issues"
    const val more = "more"
    const val team = "team"
    const val settings = "settings"
    const val kanban = "kanban"
    const val wiki_selector = "wiki_selector"
    const val wiki_page = "wiki_page"
    const val wiki_create_page = "wiki_create_page"
    const val projectsSelector = "projectsSelector"
    const val sprint = "sprint"
    const val commonTask = "commonTask"
    const val createTask = "createTask"
    const val profile = "profile"

    object Arguments {
        const val sprint = "sprint"
        const val sprintId = "sprintId"
        const val swimlaneId = "swimlaneId"
        const val commonTaskId = "taskId"
        const val commonTaskType = "taskType"
        const val ref = "ref"
        const val parentId = "parentId"
        const val statusId = "statusId"
        const val userId = "userId"
        const val wikiSlug = "wikiSlug"
    }
}

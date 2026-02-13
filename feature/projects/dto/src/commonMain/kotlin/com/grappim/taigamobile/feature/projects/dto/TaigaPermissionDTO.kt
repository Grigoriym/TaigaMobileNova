package com.grappim.taigamobile.feature.projects.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TaigaPermissionDTO {
    @SerialName("view_project")
    VIEW_PROJECT,

    // Milestones
    @SerialName("view_milestones")
    VIEW_MILESTONES,

    @SerialName("add_milestone")
    ADD_MILESTONE,

    @SerialName("modify_milestone")
    MODIFY_MILESTONE,

    @SerialName("delete_milestone")
    DELETE_MILESTONE,

    // Epics
    @SerialName("view_epics")
    VIEW_EPICS,

    @SerialName("add_epic")
    ADD_EPIC,

    @SerialName("modify_epic")
    MODIFY_EPIC,

    @SerialName("comment_epic")
    COMMENT_EPIC,

    @SerialName("delete_epic")
    DELETE_EPIC,

    // User Stories
    @SerialName("view_us")
    VIEW_US,

    @SerialName("add_us")
    ADD_US,

    @SerialName("modify_us")
    MODIFY_US,

    @SerialName("comment_us")
    COMMENT_US,

    @SerialName("delete_us")
    DELETE_US,

    // Tasks
    @SerialName("view_tasks")
    VIEW_TASKS,

    @SerialName("add_task")
    ADD_TASK,

    @SerialName("modify_task")
    MODIFY_TASK,

    @SerialName("comment_task")
    COMMENT_TASK,

    @SerialName("delete_task")
    DELETE_TASK,

    // Issues
    @SerialName("view_issues")
    VIEW_ISSUES,

    @SerialName("add_issue")
    ADD_ISSUE,

    @SerialName("modify_issue")
    MODIFY_ISSUE,

    @SerialName("comment_issue")
    COMMENT_ISSUE,

    @SerialName("delete_issue")
    DELETE_ISSUE,

    // Wiki Pages
    @SerialName("view_wiki_pages")
    VIEW_WIKI_PAGES,

    @SerialName("add_wiki_page")
    ADD_WIKI_PAGE,

    @SerialName("modify_wiki_page")
    MODIFY_WIKI_PAGE,

    @SerialName("comment_wiki_page")
    COMMENT_WIKI_PAGE,

    @SerialName("delete_wiki_page")
    DELETE_WIKI_PAGE,

    // Wiki Links
    @SerialName("view_wiki_links")
    VIEW_WIKI_LINKS,

    @SerialName("add_wiki_link")
    ADD_WIKI_LINK,

    @SerialName("modify_wiki_link")
    MODIFY_WIKI_LINK,

    @SerialName("delete_wiki_link")
    DELETE_WIKI_LINK,

    // Admin
    @SerialName("modify_project")
    MODIFY_PROJECT,

    @SerialName("delete_project")
    DELETE_PROJECT,

    @SerialName("add_member")
    ADD_MEMBER,

    @SerialName("remove_member")
    REMOVE_MEMBER,

    @SerialName("admin_project_values")
    ADMIN_PROJECT_VALUES,

    @SerialName("admin_roles")
    ADMIN_ROLES
}

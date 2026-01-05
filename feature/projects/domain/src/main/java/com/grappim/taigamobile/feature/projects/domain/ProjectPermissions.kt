package com.grappim.taigamobile.feature.projects.domain

import kotlinx.collections.immutable.ImmutableList

fun ImmutableList<TaigaPermission>.hasPermission(permission: TaigaPermission): Boolean = permission in this

fun ImmutableList<TaigaPermission>.hasAnyPermission(vararg permissions: TaigaPermission): Boolean =
    permissions.any { it in this }

fun ImmutableList<TaigaPermission>.hasAllPermissions(vararg permissions: TaigaPermission): Boolean =
    permissions.all { it in this }

fun ImmutableList<TaigaPermission>.canViewProject(): Boolean = hasPermission(TaigaPermission.VIEW_PROJECT)

fun ImmutableList<TaigaPermission>.canViewMilestones(): Boolean = hasPermission(TaigaPermission.VIEW_MILESTONES)
fun ImmutableList<TaigaPermission>.canAddMilestone(): Boolean = hasPermission(TaigaPermission.ADD_MILESTONE)
fun ImmutableList<TaigaPermission>.canModifyMilestone(): Boolean = hasPermission(TaigaPermission.MODIFY_MILESTONE)
fun ImmutableList<TaigaPermission>.canDeleteMilestone(): Boolean = hasPermission(TaigaPermission.DELETE_MILESTONE)

fun ImmutableList<TaigaPermission>.canViewEpics(): Boolean = hasPermission(TaigaPermission.VIEW_EPICS)
fun ImmutableList<TaigaPermission>.canAddEpic(): Boolean = hasPermission(TaigaPermission.ADD_EPIC)
fun ImmutableList<TaigaPermission>.canModifyEpic(): Boolean = hasPermission(TaigaPermission.MODIFY_EPIC)
fun ImmutableList<TaigaPermission>.canCommentEpic(): Boolean = hasPermission(TaigaPermission.COMMENT_EPIC)
fun ImmutableList<TaigaPermission>.canDeleteEpic(): Boolean = hasPermission(TaigaPermission.DELETE_EPIC)

fun ImmutableList<TaigaPermission>.canViewUserStories(): Boolean = hasPermission(TaigaPermission.VIEW_US)
fun ImmutableList<TaigaPermission>.canAddUserStory(): Boolean = hasPermission(TaigaPermission.ADD_US)
fun ImmutableList<TaigaPermission>.canModifyUserStory(): Boolean = hasPermission(TaigaPermission.MODIFY_US)
fun ImmutableList<TaigaPermission>.canCommentUserStory(): Boolean = hasPermission(TaigaPermission.COMMENT_US)
fun ImmutableList<TaigaPermission>.canDeleteUserStory(): Boolean = hasPermission(TaigaPermission.DELETE_US)

fun ImmutableList<TaigaPermission>.canViewTasks(): Boolean = hasPermission(TaigaPermission.VIEW_TASKS)
fun ImmutableList<TaigaPermission>.canAddTask(): Boolean = hasPermission(TaigaPermission.ADD_TASK)
fun ImmutableList<TaigaPermission>.canModifyTask(): Boolean = hasPermission(TaigaPermission.MODIFY_TASK)
fun ImmutableList<TaigaPermission>.canCommentTask(): Boolean = hasPermission(TaigaPermission.COMMENT_TASK)
fun ImmutableList<TaigaPermission>.canDeleteTask(): Boolean = hasPermission(TaigaPermission.DELETE_TASK)

fun ImmutableList<TaigaPermission>.canViewIssues(): Boolean = hasPermission(TaigaPermission.VIEW_ISSUES)
fun ImmutableList<TaigaPermission>.canAddIssue(): Boolean = hasPermission(TaigaPermission.ADD_ISSUE)
fun ImmutableList<TaigaPermission>.canModifyIssue(): Boolean = hasPermission(TaigaPermission.MODIFY_ISSUE)
fun ImmutableList<TaigaPermission>.canCommentIssue(): Boolean = hasPermission(TaigaPermission.COMMENT_ISSUE)
fun ImmutableList<TaigaPermission>.canDeleteIssue(): Boolean = hasPermission(TaigaPermission.DELETE_ISSUE)

fun ImmutableList<TaigaPermission>.canViewWikiPages(): Boolean = hasPermission(TaigaPermission.VIEW_WIKI_PAGES)
fun ImmutableList<TaigaPermission>.canAddWikiPage(): Boolean = hasPermission(TaigaPermission.ADD_WIKI_PAGE)
fun ImmutableList<TaigaPermission>.canModifyWikiPage(): Boolean = hasPermission(TaigaPermission.MODIFY_WIKI_PAGE)
fun ImmutableList<TaigaPermission>.canCommentWikiPage(): Boolean = hasPermission(TaigaPermission.COMMENT_WIKI_PAGE)
fun ImmutableList<TaigaPermission>.canDeleteWikiPage(): Boolean = hasPermission(TaigaPermission.DELETE_WIKI_PAGE)

fun ImmutableList<TaigaPermission>.canViewWikiLinks(): Boolean = hasPermission(TaigaPermission.VIEW_WIKI_LINKS)
fun ImmutableList<TaigaPermission>.canAddWikiLink(): Boolean = hasPermission(TaigaPermission.ADD_WIKI_LINK)
fun ImmutableList<TaigaPermission>.canModifyWikiLink(): Boolean = hasPermission(TaigaPermission.MODIFY_WIKI_LINK)
fun ImmutableList<TaigaPermission>.canDeleteWikiLink(): Boolean = hasPermission(TaigaPermission.DELETE_WIKI_LINK)

fun ImmutableList<TaigaPermission>.canModifyProject(): Boolean = hasPermission(TaigaPermission.MODIFY_PROJECT)
fun ImmutableList<TaigaPermission>.canDeleteProject(): Boolean = hasPermission(TaigaPermission.DELETE_PROJECT)
fun ImmutableList<TaigaPermission>.canAddMember(): Boolean = hasPermission(TaigaPermission.ADD_MEMBER)
fun ImmutableList<TaigaPermission>.canRemoveMember(): Boolean = hasPermission(TaigaPermission.REMOVE_MEMBER)
fun ImmutableList<TaigaPermission>.canAdminProjectValues(): Boolean =
    hasPermission(TaigaPermission.ADMIN_PROJECT_VALUES)
fun ImmutableList<TaigaPermission>.canAdminRoles(): Boolean = hasPermission(TaigaPermission.ADMIN_ROLES)

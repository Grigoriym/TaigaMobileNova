package com.grappim.taigamobile.tools.seed

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

private const val BASE_URL = "http://localhost:9000"
private const val USERNAME = "admin"
private const val PASSWORD = "admin"
private const val PROJECT_NAME = "Main"

private const val MILESTONE_COUNT = 3
private const val EPIC_COUNT = 10
private const val USER_STORY_COUNT = 20
private const val TASK_COUNT = 30
private const val ISSUE_COUNT = 20

fun main() {
    val client = HttpClient(CIO) {
        expectSuccess = false
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                    prettyPrint = true
                }
            )
        }
        install(Logging) {
            level = LogLevel.BODY
        }
    }

    val api = TaigaApi(client, BASE_URL)
    val seeder = Seeder(api)

    try {
        runBlocking {
            seeder.seed(
                username = USERNAME,
                password = PASSWORD,
                projectName = PROJECT_NAME,
                milestoneCount = MILESTONE_COUNT,
                epicCount = EPIC_COUNT,
                userStoryCount = USER_STORY_COUNT,
                taskCount = TASK_COUNT,
                issueCount = ISSUE_COUNT
            )
        }
    } finally {
        client.close()
    }
}

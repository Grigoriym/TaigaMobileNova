package com.grappim.taigamobile.feature.dashboard.domain

interface DashboardRepository {
    suspend fun getData(): Result<DashboardData>
}

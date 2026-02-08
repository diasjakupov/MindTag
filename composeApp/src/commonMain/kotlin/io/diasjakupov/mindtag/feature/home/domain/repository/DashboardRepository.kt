package io.diasjakupov.mindtag.feature.home.domain.repository

import io.diasjakupov.mindtag.feature.home.domain.model.DashboardData
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun getDashboardData(): Flow<DashboardData>
}

package io.diasjakupov.mindtag.feature.home.domain.usecase

import io.diasjakupov.mindtag.feature.home.domain.model.DashboardData
import io.diasjakupov.mindtag.feature.home.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.Flow

class GetDashboardUseCase(private val repository: DashboardRepository) {
    operator fun invoke(): Flow<DashboardData> = repository.getDashboardData()
}

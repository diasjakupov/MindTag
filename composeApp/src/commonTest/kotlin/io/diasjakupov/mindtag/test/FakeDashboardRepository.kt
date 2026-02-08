package io.diasjakupov.mindtag.test

import io.diasjakupov.mindtag.feature.home.domain.model.DashboardData
import io.diasjakupov.mindtag.feature.home.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeDashboardRepository : DashboardRepository {

    private val dashboardFlow = MutableStateFlow(TestData.dashboardData)

    fun setDashboardData(data: DashboardData) {
        dashboardFlow.value = data
    }

    override fun getDashboardData(): Flow<DashboardData> = dashboardFlow
}

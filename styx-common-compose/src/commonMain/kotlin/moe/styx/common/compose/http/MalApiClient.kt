package moe.styx.common.compose.http

import moe.styx.common.data.MyAnimeListData
import moe.styx.libs.mal.AbstractMALApiClient
import moe.styx.libs.mal.returnables.RefreshResult

class MalApiClient(malData: MyAnimeListData) : AbstractMALApiClient(malData) {
    override suspend fun refreshToken(): RefreshResult {
        val data = getObject<MyAnimeListData>(Endpoints.MAL_TOKEN, withAuth = true)
        if (data == null) {
            return RefreshResult(500, null)
        }
        return RefreshResult(200, data)
    }

}
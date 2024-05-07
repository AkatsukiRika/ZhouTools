package model

import kotlinx.serialization.Serializable

@Serializable
data class MemoSyncRequest(
    val username: String,
    val memos: List<Memo>
)

package model.request

import kotlinx.serialization.Serializable
import model.records.Memo

@Serializable
data class MemoSyncRequest(
    val username: String,
    val memos: List<Memo>
)

package model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MemoRecords(
    val memos: MutableList<Memo>
)

@Serializable
data class Memo(
    val text: String = "",
    @SerialName("is_todo")
    val isTodo: Boolean = false,
    @SerialName("is_pin")
    val isPin: Boolean = false,
    @SerialName("create_time")
    val createTime: Long = 0L,
    @SerialName("modify_time")
    val modifyTime: Long = 0L
)

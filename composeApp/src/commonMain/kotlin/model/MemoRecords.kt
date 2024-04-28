package model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MemoRecords(
    val memos: MutableList<Memo>
)

@Serializable
data class Memo(
    var text: String = "",
    @SerialName("is_todo")
    var isTodo: Boolean = false,
    @SerialName("is_todo_finished")
    var isTodoFinished: Boolean = false,
    @SerialName("is_pin")
    var isPin: Boolean = false,
    @SerialName("create_time")
    val createTime: Long = 0L,
    @SerialName("modify_time")
    var modifyTime: Long = 0L
)

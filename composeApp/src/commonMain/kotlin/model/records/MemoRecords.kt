package model.records

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import model.DoubleToLongSerializer

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
    @Serializable(with = DoubleToLongSerializer::class)
    @SerialName("create_time")
    val createTime: Long = 0L,
    @Serializable(with = DoubleToLongSerializer::class)
    @SerialName("modify_time")
    var modifyTime: Long = 0L
)

package helper

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.display.GroupDisplayItem
import model.display.IMemoDisplayItem
import model.display.MemoDisplayItem
import model.records.Memo
import model.records.MemoRecords
import model.request.MemoSyncRequest
import org.jetbrains.compose.resources.getString
import store.AppStore
import util.TimeUtil
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.unsorted

object MemoHelper {
    fun addMemo(text: String, todo: Boolean, pin: Boolean, group: String? = null) {
        val time = TimeUtil.currentTimeMillis()
        val memo = Memo(
            text = text,
            isTodo = todo,
            isPin = pin,
            createTime = time,
            modifyTime = time,
            group = group
        )
        val memos = getMemos()
        memos.add(memo)
        saveMemos(memos)
    }

    fun modifyMemo(memo: Memo, text: String, todo: Boolean, pin: Boolean, group: String? = null) {
        val memos = getMemos()
        val findResult = memos.find {
            it.text == memo.text
                    && it.isTodo == memo.isTodo
                    && it.isPin == memo.isPin
                    && it.createTime == memo.createTime
                    && it.modifyTime == memo.modifyTime
                    && it.group == memo.group
        }
        if (findResult != null) {
            findResult.text = text
            findResult.isTodo = todo
            findResult.isPin = pin
            findResult.modifyTime = TimeUtil.currentTimeMillis()
            findResult.group = group
        } else {
            addMemo(text, todo, pin, group)
        }
        saveMemos(memos)
    }

    fun markDone(memo: Memo, done: Boolean) {
        val memos = getMemos()
        val findResult = memos.find {
            it.text == memo.text
                    && it.isTodo == memo.isTodo
                    && it.isPin == memo.isPin
                    && it.createTime == memo.createTime
                    && it.modifyTime == memo.modifyTime
                    && it.group == memo.group
        }
        if (findResult != null) {
            findResult.isTodoFinished = done
            saveMemos(memos)
        }
    }

    fun deleteMemo(memo: Memo) {
        val memos = getMemos()
        memos.remove(memo)
        saveMemos(memos)
    }

    fun getDisplayList(): List<IMemoDisplayItem> {
        val memos = getMemos()
        val displayList = mutableListOf<IMemoDisplayItem>()
        val groupSet = getGroupSet().sorted()
        groupSet.forEach { group ->
            val groupDisplayItem = GroupDisplayItem(group)
            displayList.add(groupDisplayItem)
            val groupMemos = memos.filter { it.group == group }
            val groupPinMemos = groupMemos.filter { it.isPin }
            val groupNotPinMemos = groupMemos.filterNot { it.isPin }
            groupPinMemos.forEach { memo ->
                displayList.add(MemoDisplayItem(memo))
            }
            groupNotPinMemos.forEach { memo ->
                displayList.add(MemoDisplayItem(memo))
            }
        }
        val unsortedMemos = memos.filter { it.group == null }
        val unsortedPinMemos = unsortedMemos.filter { it.isPin }
        val unsortedNotPinMemos = unsortedMemos.filterNot { it.isPin }
        val unsorted = runBlocking { getString(Res.string.unsorted) }
        displayList.add(GroupDisplayItem(unsorted))
        unsortedPinMemos.forEach { memo ->
            displayList.add(MemoDisplayItem(memo))
        }
        unsortedNotPinMemos.forEach { memo ->
            displayList.add(MemoDisplayItem(memo))
        }
        return displayList
    }

    fun getGroupSet(): Set<String> {
        val memos = getMemos()
        val groupSet = mutableSetOf<String>()
        memos.forEach {
            it.group?.let { group ->
                groupSet.add(group)
            }
        }
        return groupSet
    }

    fun buildSyncRequest(): MemoSyncRequest? {
        val memos = getMemos()
        return try {
            if (AppStore.loginUsername.isEmpty()) {
                null
            } else {
                MemoSyncRequest(username = AppStore.loginUsername, memos = memos)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun getMemos() = try {
        val memoRecords = Json.decodeFromString<MemoRecords>(AppStore.memos)
        memoRecords.memos
    } catch (e: Exception) {
        e.printStackTrace()
        mutableListOf()
    }

    private fun saveMemos(memos: MutableList<Memo>) {
        val records = MemoRecords(memos)
        AppStore.memos = Json.encodeToString(records)
    }
}
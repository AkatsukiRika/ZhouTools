package model.display

import model.records.Memo

interface IMemoDisplayItem

data class GroupDisplayItem(val name: String) : IMemoDisplayItem

data class MemoDisplayItem(val memo: Memo) : IMemoDisplayItem
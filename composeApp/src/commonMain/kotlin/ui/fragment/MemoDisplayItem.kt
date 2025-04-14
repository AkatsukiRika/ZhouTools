package ui.fragment

import model.records.Memo

interface IMemoDisplayItem

data class MemoDisplayItem(val memo: Memo) : IMemoDisplayItem
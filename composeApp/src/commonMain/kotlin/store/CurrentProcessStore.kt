package store

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Data stored only for the current process.
 */
object CurrentProcessStore {
    var screenWidthPixels = MutableStateFlow(0)
}
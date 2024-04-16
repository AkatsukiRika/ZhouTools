package extension

fun String.firstCharToCapital(): String {
    if (this.isBlank()) {
        return this
    }
    return this.first().uppercase() + this.substring(1)
}

fun String.isBlankJson(): Boolean {
    return this == "{}"
}
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

fun String.isValidUrl(): Boolean {
    return this.startsWith("http://") || this.startsWith("https://")
}

fun String.isValidEmail(): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"
    return this.matches(emailRegex.toRegex())
}
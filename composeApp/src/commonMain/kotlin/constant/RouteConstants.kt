package constant

object RouteConstants {
    const val PARAM_EDIT = "{edit}"
    const val PARAM_MODE = "{mode}"

    const val ROUTE_LOGIN = "/login"
    const val ROUTE_SIGN_UP = "/signUp"
    const val ROUTE_HOME = "/home"
    const val ROUTE_DETAILS = "/details"
    const val ROUTE_WRITE_MEMO = "/writeMemo/$PARAM_EDIT"
    const val ROUTE_ADD_SCHEDULE = "/addSchedule"
    const val ROUTE_SYNC = "/sync/$PARAM_MODE"
    const val ROUTE_EXPORT = "/export"
}
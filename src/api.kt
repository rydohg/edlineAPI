import org.jsoup.Connection

import org.jsoup.Jsoup
const val EDLINE_URL = "https://www.edline.net"

fun login(username: String, password: String): LoginResponse {
    var response = Jsoup.connect("$EDLINE_URL/InterstitialLogin.page").timeout(0).userAgent("Chrome/63.0.3239.108").execute()
    val baseCookies = response.cookies()

    val data = arrayOf(
            "TCNK", "authenticationEntryComponent",
            "submitEvent", "1",
            "guestLoginEvent", "",
            "enterClicked", "false",
            "bscf", "",
            "bscv", "",
            "targetEntid", "",
            "ajaxSupported", "yes",
            "screenName", username,
            "kclq", password)

    response = Jsoup.connect("$EDLINE_URL/post/InterstitialLogin.page")
            .data(*data)
            .cookies(baseCookies)
            .userAgent("Chrome/63.0.3239.108")
            .method(Connection.Method.POST)
            .timeout(0)
            .execute()

    val loggedInCookies = response.cookies()
    loggedInCookies.putAll(baseCookies)

    println("URL: " + response.url())
    if (response.url().toString().contains("Notification.page")) {
        // Change this for use on Android
        println("Wrong username or password")
        System.exit(1)
    }

    return LoginResponse(response, loggedInCookies)
}

fun loginWithGradeReport(username: String, password: String, entId: String): LoginResponse {
    var response = Jsoup.connect("$EDLINE_URL/InterstitialLogin.page").timeout(0).userAgent("Chrome/63.0.3239.108").execute()
    val baseCookies = response.cookies()

    val data = arrayOf(
            "TCNK", "authenticationEntryComponent",
            "submitEvent", "1",
            "guestLoginEvent", "",
            "enterClicked", "false",
            "bscf", "",
            "bscv", "",
            "targetEntid", entId,
            "ajaxSupported", "yes",
            "screenName", username,
            "kclq", password)

    response = Jsoup.connect("$EDLINE_URL/post/InterstitialLogin.page")
            .data(*data)
            .cookies(baseCookies)
            .userAgent("Chrome/63.0.3239.108")
            .method(Connection.Method.POST)
            .timeout(0)
            .execute()

    val loggedInCookies = response.cookies()
    loggedInCookies.putAll(baseCookies)

    println("URL: " + response.url())
    if (response.url().toString().contains("Notification.page")) {
        println("Username and Password Incorrect")
        System.exit(1)
    }

    return LoginResponse(response, loggedInCookies)
}

fun getGradeReportList(loginCookies: Map<String, String>): Connection.Response {
    val data = arrayOf(
            "eventParms", "TCNK=headerComponent",
            "invokeEvent", "viewUserDocList",
            "sessionRenewalEnabled", "yes",
            "sessionRenewalIntervalSeconds", "300",
            "sessionRenewalMaxNumberOfRenewals", "25",
            "sessionIgnoreInitialActivitySeconds", "1200",
            "ajaxRequestKeySuffix", "0"
    )

    return Jsoup.connect("$EDLINE_URL/post/GroupHome.page")
            .data(*data)
            .cookies(loginCookies)
            .userAgent("Chrome/63.0.3239.108")
            .method(Connection.Method.POST)
            .timeout(0)
            .execute()
}

fun getGradeReport(gradeReport: GradeReport, loginCookies: Map<String, String>): Connection.Response {
    return Jsoup.connect("$EDLINE_URL/DocViewBody.page?currentDocEntid=${gradeReport.javascriptLink}&returnPage=%2FUserDocList.page")
            .cookies(loginCookies)
            .userAgent("Chrome/63.0.3239.108")
            .timeout(0)
            .execute()
}

fun getGradeReport(gradeReport: String, loginCookies: Map<String, String>): Connection.Response {
    return Jsoup.connect("$EDLINE_URL/DocViewBody.page?currentDocEntid=$gradeReport&returnPage=%2FUserDocList.page")
            .cookies(loginCookies)
            .userAgent("Chrome/63.0.3239.108")
            .timeout(0)
            .execute()
}

fun getAndParseGradeReportsList(loginCookies: Map<String, String>): ArrayList<GradeReport> {
    return Parser.parseGradeReportList(getGradeReportList(loginCookies).body())
}

fun getAndParseGradeReport(gradeReport: GradeReport, loginCookies: Map<String, String>): String {
    return Parser.parseGradeReport(getGradeReport(gradeReport, loginCookies).body())
}

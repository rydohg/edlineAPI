import java.io.File

fun main(args: Array<String>) {
    print("Enter username: ")
    val username = readLine()
    print("Enter pass: ")
    val password = readLine()

    val loginResponse = login(username!!, password!!)

    val studentInfo = Parser.getStudentInfo(loginResponse.response.body())

    println("Name: ${studentInfo.studentName}")
    println("School Name: ${studentInfo.schoolName}")
    for (course in studentInfo.courses) {
        println("Course Name: ${course.courseName}")
    }

    val gradeReports = getAndParseGradeReportsList(loginResponse.loginCookies)
    for (i in 1..3) {
        println("Report: ${gradeReports[i].reportName} ${gradeReports[i].javascriptLink}")
    }

    val report = getAndParseGradeReport(gradeReports[0], loginResponse.loginCookies)
    File("report.html").bufferedWriter().use { out ->
        out.write(report)
    }
}
import org.jsoup.Connection

data class StudentInfo(val schoolName: String, val studentName: String, val courses: ArrayList<Course>)

data class Course(val courseName: String, val classLink: String)

data class LoginResponse(val response: Connection.Response, val loginCookies: Map<String, String>)

data class GradeReport(val date: String, val reportName: String, val javascriptLink: String, val classLink: String)
import org.jsoup.Connection
import java.io.Serializable

data class StudentInfo(val schoolName: String, val studentName: String, val courses: ArrayList<Course>)

data class Course(val courseName: String, val classLink: String): Serializable

data class LoginResponse(val response: Connection.Response, val loginCookies: Map<String, String>)

data class SerializableLoginResponse(val responseBody: String, val loginCookies: Map<String, String>): Serializable

data class GradeReport(val date: String, val reportName: String, val javascriptLink: String, val classLink: String): Serializable

interface Report {
    val parsable: Boolean
}

data class ParsedGradeReport(override val parsable: Boolean, val teacher: String, val overallGrade: String, val letterGradeReport: String, val assignments: ArrayList<Assignment>) : Report

data class NotParsedGradeReport(override val parsable: Boolean, val html: String): Report

data class Assignment(val name: String, val date: String, val category: String, val pointsEarned: String, val outOf: String, val letter: String)
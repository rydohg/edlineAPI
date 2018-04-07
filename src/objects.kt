import org.jsoup.Connection
import java.io.Serializable

data class StudentInfo(val schoolName: String, val studentName: String, val courses: ArrayList<Course>)

data class Course(val courseName: String, val classLink: String) : Serializable

data class LoginResponse(val response: Connection.Response, val loginCookies: Map<String, String>)

data class SerializableLoginResponse(val responseBody: String, val loginCookies: Map<String, String>) : Serializable

data class NotParsedGradeReport(
        val reportId: Int = 0,
        val date: String,
        val reportName: String,
        val javascriptLink: String,
        val classLink: String
) : Serializable

data class ParsedGradeReport(
        var reportId: Int = 0,
        val parsable: Boolean,
        val date: String,
        val reportName: String,
        val classLink: String,
        val rawReport: String = "",
        val teacher: String = "",
        val overallGrade: String = "",
        val letterGradeReport: String = "",
        val assignments: ArrayList<Assignment> = ArrayList()
) : Serializable

data class Assignment(
        val name: String,
        val date: String,
        val category: String,
        val pointsEarned: String,
        val outOf: String,
        val percent: String,
        val letter: String
): Serializable
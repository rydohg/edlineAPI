import org.jsoup.Jsoup
import java.io.File

object Parser {
    fun getStudentInfo(html: String): StudentInfo {
        val doc = Jsoup.parse(html)
        val myClassesUl = doc.getElementsByAttributeValue("aria-labelledby", "myEdlineMenu-myClassedAndShortcuts")[0]

        val schoolName = doc.title().trimEnd(':')
        val studentName = myClassesUl.child(0).getElementsByClass("ed-studentName")[0].text()
        val courses = ArrayList<Course>()

        for (menuItem in myClassesUl.children()) {
            if (menuItem.id() == "userShortcuts0") {
                continue
            } else if (menuItem.attr("role") == "presentation") {
                break
            }

            // The first link in the menu item is the name of the class
            val classLinkElement = menuItem.getElementsByTag("a")[0]

            val className = classLinkElement.text()
            val linkToClass = classLinkElement.attr("href")
            courses.add(Course(className, linkToClass))
        }

        return StudentInfo(schoolName, studentName, courses)
    }

    fun parseGradeReportList(html: String): ArrayList<GradeReport> {
        val doc = Jsoup.parse(html)
        val form = doc.getElementsByAttributeValue("name", "userDocListTableForm")
        val gradeReportsTable = form[0]
                .getElementsByClass("ed-formTable")[1]
                .getElementsByTag("tbody")[0]
                .children()

        val gradeReports = ArrayList<GradeReport>()

        for (gradeReport in gradeReportsTable.drop(1)) {
            val data = gradeReport.children().drop(2)
            val date = data[0].text()
            val reportName = data[1].text()
            val rawJavascriptLink = data[1].getElementsByTag("a")[0].attr("href")
            val classHomePageLink = data[2].getElementsByTag("a")[0].attr("href")

            val javascriptLink = rawJavascriptLink
                    .substring(rawJavascriptLink.indexOf("('") + 2, rawJavascriptLink.indexOf("')"))

            gradeReports.add(GradeReport(date, reportName, javascriptLink, classHomePageLink))
        }

        return gradeReports
    }

    fun parseGradeReport(html: String): String {
        return Jsoup.parse(html).getElementsByTag("pre")[0].text()
    }
}
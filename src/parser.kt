import org.jsoup.Jsoup

object Parser {
    fun getStudentInfo(html: String): StudentInfo {
        val doc = Jsoup.parse(html)
        val myClassesUl = doc.getElementsByAttributeValue("aria-labelledby", "myEdlineMenu-myClassedAndShortcuts")[0]

        val schoolName = doc.title().removeSuffix(": Home Page")
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

    fun parseGradeReport(html: String): Report {
        if (isParsable(html)){
            val rawReport = Jsoup.parse(html).getElementsByTag("pre")[0].text()
            val inputByLine = rawReport.split("\n")

            val teacherName = inputByLine[2].split("   ")[0]
            val gradePercent = inputByLine[3].split(":")[1].split("   ")[0].removePrefix(" ")
            val letterGrade = inputByLine[4].split(":")[1].split("   ")[0].removePrefix(" ")

            val scoreInfoIndex = rawReport
                    .substring(rawReport.indexOf("Score Information") + 19)

            val startAssignmentsString = scoreInfoIndex.substring(scoreInfoIndex.indexOf("\n") + 4)

            var endAssignmentIndex = 0
            var lastLine = ""
            for (line in startAssignmentsString.split("\n")) {
                if (line == "\r") {
                    endAssignmentIndex = startAssignmentsString.indexOf(lastLine) + lastLine.length + 2
                    break
                }
                lastLine = line
            }

            val rawAssignmentsString = " " + startAssignmentsString.substring(0..endAssignmentIndex)
            val assignments = ArrayList<Assignment>()

            for (line in rawAssignmentsString.split("\n")) {
                if (line != "" && line != "\r") {
                    val noPrefix = line.substring(11)

                    val name = noPrefix.substring(0..8)
                    val date = noPrefix.substring(9..16)
                    val category = noPrefix.substring(18..24)
                    val pointsEarned = noPrefix.substring(37..41)
                    val outOf = noPrefix.substring(46..48)
                    val letter = noPrefix.substring(noPrefix.length - 2)

                    assignments.add(Assignment(name, date, category, pointsEarned, outOf, letter))
                }
            }

            return ParsedGradeReport(true, teacherName, gradePercent, letterGrade, assignments)
        } else {
            return NotParsedGradeReport(false, html)
        }
    }

    fun isParsable(report: String): Boolean {
        return report.contains("Score Information")
    }

}
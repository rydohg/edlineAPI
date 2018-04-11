import org.jsoup.Jsoup

object Parser {
    fun getStudentInfo(html: String): StudentInfo {
        val doc = Jsoup.parse(html)
        val myClassesUl = doc.getElementsByAttributeValue("aria-labelledby", "myEdlineMenu-myClassedAndShortcuts")[0]

        val schoolName = doc.title().removeSuffix(": Home Page")
        val studentName = myClassesUl.getElementsByClass("ed-studentName").last().text()
        val courses = ArrayList<Course>()
        for (menuItem in myClassesUl.children()) {
            if (menuItem.id() == "userShortcuts1") {
                courses.clear()
                continue
            } else if (menuItem.id() == "userShortcuts0") {
                continue
            } else if (menuItem.id() == "myOtherGroups0") {
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

    fun parseGradeReportList(html: String, loginCookies: Map<String, String>): ArrayList<NotParsedGradeReport> {
        val doc = Jsoup.parse(html)
        val form = doc.getElementsByAttributeValue("name", "userDocListTableForm")
        val gradeReportsTable = form[0]
                .getElementsByClass("ed-formTable")[1]
                .getElementsByTag("tbody")[0]
                .children()
        val gradeReports = ArrayList<NotParsedGradeReport>()
        if (gradeReportsTable.html().contains("No items found.")) {
            println("No Reports")
            println("HTML:" + doc.getElementById("vusrList").html())
            val vusrTable = doc
                    .getElementById("vusrList")
                    .children()
            return getAndParseGradeReportsListWithVusr(loginCookies, vusrTable[1].attr("value"))
        }

        for (gradeReport in gradeReportsTable.drop(1)) {
            val data = gradeReport.children().drop(2)
            val date = data[0].text()
            val reportName = data[1].text()
            val rawJavascriptLink = data[1].getElementsByTag("a")[0].attr("href")
            val classHomePageLink = data[2].getElementsByTag("a")[0].attr("href")

            val javascriptLink = rawJavascriptLink
                    .substring(rawJavascriptLink.indexOf("('") + 2, rawJavascriptLink.indexOf("')"))

            gradeReports.add(
                    NotParsedGradeReport(
                            date = date,
                            reportName = reportName,
                            javascriptLink = javascriptLink,
                            classLink = classHomePageLink
                    )
            )
        }

        return gradeReports
    }

    fun parseGradeReport(html: String, rawGradeReport: NotParsedGradeReport): ParsedGradeReport {
        if (isParsable(html)) {
            val rawReport = Jsoup.parse(html).getElementsByTag("pre")[0].text()
            val inputByLine = rawReport.split("\n")

            var teacherName = ""
            var gradePercent = ""
            var letterGrade = ""

            try {
                teacherName = inputByLine[2].split("   ")[0]
                gradePercent = inputByLine[3].split(":")[1].split("   ")[0].removePrefix(" ")
                letterGrade = inputByLine[4].split(":")[1].split("   ")[0].removePrefix(" ")
            } catch (e: Exception) {}

            val rawAssignmentsString = getAssignmentsString(rawReport)
            val assignments = parseAssignments(rawAssignmentsString)

            //If it is really parsable
            if (assignments.size != 0) {
                return ParsedGradeReport(
                        parsable = true,
                        date = rawGradeReport.date,
                        reportName = rawGradeReport.reportName,
                        classLink = rawGradeReport.classLink,
                        rawReport = html,
                        teacher = teacherName,
                        overallGrade = gradePercent,
                        letterGradeReport = letterGrade,
                        assignments = assignments
                )
            } else {
                return ParsedGradeReport(
                        parsable = false,
                        rawReport = html,
                        date = rawGradeReport.date,
                        reportName = rawGradeReport.reportName,
                        classLink = rawGradeReport.classLink
                )
            }
        } else {
            //TODO: Replace rawReport
            return ParsedGradeReport(
                    parsable = false,
                    rawReport = html,
                    date = rawGradeReport.date,
                    reportName = rawGradeReport.reportName,
                    classLink = rawGradeReport.classLink
            )
        }
    }

    private fun getAssignmentsString(rawReport: String): String {
        val scoreInfoIndex = rawReport
                .substring(rawReport.indexOf("Score Information") + 19)

        val startAssignmentsString = scoreInfoIndex.substring(scoreInfoIndex.indexOf("\n") + 4)

        var endAssignmentIndex = 0
        var lastLine = ""
        for (line in startAssignmentsString.split("\n")) {
            if (line == "\r" || line == "") {
                endAssignmentIndex = startAssignmentsString.indexOf(lastLine) + lastLine.length + 2
                break
            }
            lastLine = line
        }
        if (endAssignmentIndex == 0) {
            endAssignmentIndex = startAssignmentsString.length - 1
        }

        return " " + startAssignmentsString.substring(0..endAssignmentIndex)
    }

    private fun parseAssignments(assignmentsString: String): ArrayList<Assignment> {
        val assignments = ArrayList<Assignment>()

        var counter = 0
        for (line in assignmentsString.split("\n")) {
            val lineWithoutSpaces = line.replace("\\s".toRegex(), "")
            if (line != "" && line != "\r" && lineWithoutSpaces.isNotEmpty()) {
                try {
                    var noPrefix = ""

                    for (i in 0..(line.length - 1)) {
                        if (line[i] != ' ') {
                            noPrefix = line.substring(i)
                            break
                        }
                    }

                    val name = noPrefix.substring(0..8)
                    val date = noPrefix.substring(9..16)
                    if (counter == 0 && !date.matches("^((0?[13578]|10|12)(-|\\/)(([1-9])|(0[1-9])|([12])([0-9]?)|(3[01]?))(-|\\/)((19)([2-9])(\\d{1})|(20)([01])(\\d{1})|([8901])(\\d{1}))|(0?[2469]|11)(-|\\/)(([1-9])|(0[1-9])|([12])([0-9]?)|(3[0]?))(-|\\/)((19)([2-9])(\\d{1})|(20)([01])(\\d{1})|([8901])(\\d{1})))\$".toRegex())) {
                        throw Exception()
                    } else {
                        counter += 1
                    }
                    val category = noPrefix.substring(18..24)

                    val reversedLine = noPrefix.reversed()
                    println(reversedLine)
                    val letter = reversedLine.substring(1..1)
                    val percent = reversedLine.substring( 3..5).reversed()

                    val pointsEarned = reversedLine.substring(7..9).reversed()
                    val outOf = reversedLine.substring(11..13).reversed()
                    println("Letter; $letter Percent: $percent Earned: $pointsEarned Out of: $outOf")

                    assignments.add(Assignment(name, date, category, pointsEarned, outOf, percent, letter))
                } catch (e: Exception) {
                    e.printStackTrace()

                    // Marks it as unparsable
                    assignments.clear()
                    return assignments
                }
            }
        }

        return assignments
    }

    fun isParsable(report: String): Boolean {
        return report.contains("Score Information")
    }

}
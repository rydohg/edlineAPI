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
        if (gradeReportsTable.html().contains("No items found.")){
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
                    try {
                        val noPrefix: String =
                                if (line[3] != ' ') {
                                    line.substring(3)
                                } else if (line[9] != ' ') {
                                    line.substring(9)
                                } else if (line[11] != ' ') {
                                    line.substring(11)
                                } else if (line[12] != ' ') {
                                    line.substring(11)
                                } else if (line[17] != ' ') {
                                    line.substring(17)
                                } else if (line[18] != ' ') {
                                    line.substring(17)
                                } else {
                                    break
                                }
                        val name = noPrefix.substring(0..8)
                        val date = noPrefix.substring(9..16)
                        val category = noPrefix.substring(18..24)
                        val pointsEarned = noPrefix.substring(37..41)
                        val outOf = noPrefix.substring(43..45)
                        val percent = noPrefix.substring(46..49)
                        val letter = noPrefix.substring(noPrefix.length - 2)

                        assignments.add(Assignment(name, date, category, pointsEarned, outOf, percent, letter))
                    } catch (e: StringIndexOutOfBoundsException) {
                        e.printStackTrace()
                        return ParsedGradeReport(
                                parsable = false,
                                rawReport = rawReport,
                                date = rawGradeReport.date,
                                reportName = rawGradeReport.reportName,
                                classLink = rawGradeReport.classLink
                        )
                    }
                }
            }

            return ParsedGradeReport(
                    parsable = true,
                    date = rawGradeReport.date,
                    reportName = rawGradeReport.reportName,
                    classLink = rawGradeReport.classLink,
                    teacher = teacherName,
                    overallGrade = gradePercent,
                    letterGradeReport = letterGrade,
                    assignments = assignments
            )
        } else {
            return ParsedGradeReport(
                    parsable = false,
                    rawReport = Jsoup.parse(html).getElementsByTag("pre")[0].text(),
                    date = rawGradeReport.date,
                    reportName = rawGradeReport.reportName,
                    classLink = rawGradeReport.classLink
            )
        }
    }

    fun isParsable(report: String): Boolean {
        return report.contains("Score Information")
    }

}
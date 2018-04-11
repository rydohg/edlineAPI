import java.io.File

fun main(args: Array<String>){
    val rawReport = File("report2.html").readText()
    val testObj = NotParsedGradeReport(date = "3/30/18", reportName = "Test", javascriptLink = "Test", classLink = "Test")
    val report = Parser.parseGradeReport(rawReport, testObj)
    if (report.parsable){
/*        for(assignment in report.assignments){
        }*/
    } else {
        println("Not Parsable")
    }
}
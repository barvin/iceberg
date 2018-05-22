import org.qatools.rp.logback.ReportPortalAppender

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%date %level [%logger{0}]: %msg%n"
    }
}
appender("ReportPortalAppender", ReportPortalAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%level [%logger{0}]: %msg%n"
    }
}
root(ch.qos.logback.classic.Level.INFO, ["STDOUT", "ReportPortalAppender"])
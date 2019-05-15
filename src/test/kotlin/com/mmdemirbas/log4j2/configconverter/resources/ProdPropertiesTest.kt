package com.mmdemirbas.log4j2.configconverter.resources

import com.mmdemirbas.log4j2.configconverter.Config
import com.mmdemirbas.log4j2.configconverter.Serializer.Format.PROPERTIES
import com.mmdemirbas.log4j2.configconverter.TestBase
import org.junit.jupiter.api.DisplayName

@DisplayName("prod.properties")
object ProdPropertiesTest : TestBase(PROPERTIES,
                                     fromResource("/com/mmdemirbas/log4j2/configconverter/prod.properties"),
                                     Config())

object ReportingOldPropertiesTest : TestBase(PROPERTIES,
                                             fromFile("/Users/md/dev/opsgenie/gitlab/opsgenie-reporting/opsgenie-reporting-app-root/web-internal-api/src/main/resources/log4j2-prod.properties"),
                                             Config())

object ReportingNewPropertiesTest : TestBase(PROPERTIES,
                                             fromFile("/Users/md/dev/opsgenie/gitlab/opsgenie-reporting/opsgenie-reporting-app-root/web-internal-api/src/main/resources/log4j2-prod.properties"),
                                             Config())
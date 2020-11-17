package org.opengroup.osdu.file.runner;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(features = "classpath:features", glue = { "classpath:org.opengroup.osdu.filedms.stepdefs" }, tags = {
        "@TearDown" }, plugin = { "pretty", "junit:target/cucumber-reports/file-DMS-test-report.xml" })
public class TearDownTestsRunner {

}

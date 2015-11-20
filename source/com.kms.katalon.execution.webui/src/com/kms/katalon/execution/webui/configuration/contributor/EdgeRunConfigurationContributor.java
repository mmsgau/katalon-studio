package com.kms.katalon.execution.webui.configuration.contributor;

import java.io.IOException;
import java.util.Map;

import com.kms.katalon.core.webui.driver.WebUIDriverType;
import com.kms.katalon.entity.testcase.TestCaseEntity;
import com.kms.katalon.entity.testsuite.TestSuiteEntity;
import com.kms.katalon.execution.configuration.IRunConfiguration;
import com.kms.katalon.execution.configuration.contributor.IRunConfigurationContributor;
import com.kms.katalon.execution.exception.ExecutionException;
import com.kms.katalon.execution.webui.configuration.EdgeRunConfiguration;

public class EdgeRunConfigurationContributor implements IRunConfigurationContributor {

    @Override
    public String getId() {
        return WebUIDriverType.EDGE_DRIVER.toString();
    }

    @Override
    public IRunConfiguration getRunConfiguration(TestCaseEntity testCase, Map<String, String> runInput)
            throws IOException, ExecutionException {
        return new EdgeRunConfiguration(testCase);
    }

    @Override
    public IRunConfiguration getRunConfiguration(TestSuiteEntity testSuite, Map<String, String> runInput)
            throws IOException, ExecutionException {
        return new EdgeRunConfiguration(testSuite);
    }

}
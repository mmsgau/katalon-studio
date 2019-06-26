package com.kms.katalon.core.windows.keyword.builtin;

import org.apache.commons.io.FileUtils
import org.openqa.selenium.OutputType
import org.openqa.selenium.WebElement

import com.kms.katalon.core.annotation.internal.Action;
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.exception.StepFailedException
import com.kms.katalon.core.keyword.internal.AbstractKeyword
import com.kms.katalon.core.keyword.internal.KeywordMain
import com.kms.katalon.core.keyword.internal.SupportLevel
import com.kms.katalon.core.logging.KeywordLogger
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.testobject.WindowsTestObject
import com.kms.katalon.core.windows.WindowsDriverFactory
import com.kms.katalon.core.windows.keyword.helper.WindowsElementHelper

import groovy.transform.CompileStatic
import io.appium.java_client.windows.WindowsDriver

@Action(value = "setText")
public class SetTextKeyword extends AbstractKeyword {
    private KeywordLogger logger = KeywordLogger.getInstance(SetTextKeyword.class)
    
        @Override
        public SupportLevel getSupportLevel(Object... params) {
            return SupportLevel.NOT_SUPPORT;
        }

        @CompileStatic
        @Override
        public Object execute(Object ...params) {
            WindowsTestObject testObject = (WindowsTestObject) params[0]
            String text = (String) params[1]
            FailureHandling flowControl = (FailureHandling)(params.length > 2 && params[2] instanceof FailureHandling ? params[2] : RunConfiguration.getDefaultFailureHandling())
            setText(testObject, text, flowControl)
        }

        @CompileStatic
        public String setText(WindowsTestObject testObject, String text, FailureHandling flowControl) throws StepFailedException {
            return (String) KeywordMain.runKeyword({
                WindowsDriver windowsDriver = WindowsDriverFactory.getWindowsDriver()
                if (windowsDriver == null) {
                    KeywordMain.stepFailed("WindowsDriver has not started. Please try Windows.startApplication first.", flowControl)
                }

                WebElement windowElement = WindowsElementHelper.findElement(testObject)
                logger.logDebug('Setting text of test object: ' + testObject.getObjectId())
                windowElement.sendKeys(text)
                logger.logPassed('Set text to test object: ' + testObject.getObjectId() + ' successfully')
            }, flowControl)
        }
}

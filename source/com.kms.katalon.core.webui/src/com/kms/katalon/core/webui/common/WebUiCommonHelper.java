package com.kms.katalon.core.webui.common;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.kms.katalon.core.configuration.RunConfiguration;
import com.kms.katalon.core.exception.StepFailedException;
import com.kms.katalon.core.helper.KeywordHelper;
import com.kms.katalon.core.logging.KeywordLogger;
import com.kms.katalon.core.testobject.TestObject;
import com.kms.katalon.core.webui.constants.StringConstants;
import com.kms.katalon.core.webui.driver.DriverFactory;

public class WebUiCommonHelper extends KeywordHelper {
	private static KeywordLogger logger = KeywordLogger.getInstance();
	
	public static boolean isTextPresent(WebDriver webDriver, String text, boolean isRegex) throws WebDriverException,
			IllegalArgumentException {
		String regularExpressionLog = ((isRegex) ? " using regular expression" : "");
		logger.logInfo(MessageFormat.format(StringConstants.COMM_EXC_CHECKING_TEXT_PRESENT, regularExpressionLog));
		if (text == null) {
			throw new IllegalArgumentException(StringConstants.COMM_EXC_TEXT_IS_NULL);
		}

		boolean isContained = false;
		WebElement bodyElement = webDriver.findElement(By.tagName("body"));
		String pageText = bodyElement.getText();

		logger.logInfo(MessageFormat.format(StringConstants.COMM_LOG_INFO_FINDING_TEXT_ON_PAGE, text, regularExpressionLog));
		if (pageText != null && !pageText.isEmpty()) {
			if (isRegex) {
				Pattern pattern = Pattern.compile(text);
				Matcher matcher = pattern.matcher(pageText);
				while (matcher.find()) {
					isContained = true;
					break;
				}
			} else {
				isContained = pageText.contains(text);
			}
		}
		return isContained;
	}

	public static boolean switchToWindowUsingTitle(WebDriver webDriver, String title) throws WebDriverException, InterruptedException {
		float timeCount = 0;
		while (timeCount < RunConfiguration.getTimeOut()) {
			Set<String> availableWindows = webDriver.getWindowHandles();
			for (String windowId : availableWindows) {
				webDriver = webDriver.switchTo().window(windowId);
				if (webDriver.getTitle().equals(title)) {
					return true;
				}
			}
			Thread.sleep(200);
			timeCount += 0.2;
		}
		return false;
	}

	public static boolean closeWindowUsingTitle(WebDriver webDriver, String title) throws InterruptedException {
		float timeCount = 0;
		while (timeCount < RunConfiguration.getTimeOut()) {
			Set<String> availableWindows = webDriver.getWindowHandles();
			for (String windowId : availableWindows) {
				webDriver = webDriver.switchTo().window(windowId);
				if (webDriver.getTitle().equals(title)) {
					webDriver.close();
					return true;
				}
			}
			Thread.sleep(200);
			timeCount += 0.2;
		}
		return false;
	}

	public static boolean switchToWindowUsingUrl(WebDriver webDriver, String url) throws InterruptedException {
		float timeCount = 0;
		while (timeCount < RunConfiguration.getTimeOut()) {
			Set<String> availableWindows = webDriver.getWindowHandles();
			for (String windowId : availableWindows) {
				if (webDriver.switchTo().window(windowId).getCurrentUrl().equals(url)) {
					return true;
				}
			}
			Thread.sleep(200);
			timeCount += 0.2;
		}
		return false;
	}

	public static boolean closeWindowUsingUrl(WebDriver webDriver, String url) throws InterruptedException {
		float timeCount = 0;
		while (timeCount < RunConfiguration.getTimeOut()) {
			Set<String> availableWindows = webDriver.getWindowHandles();
			for (String windowId : availableWindows) {
				if (webDriver.switchTo().window(windowId).getCurrentUrl().equals(url)) {
					webDriver.close();
					return true;
				}
			}
			Thread.sleep(200);
			timeCount += 0.2;
		}
		return false;
	}

	public static boolean switchToWindowUsingIndex(WebDriver webDriver, int index) throws InterruptedException {
		float timeCount = 0;
		while (timeCount < RunConfiguration.getTimeOut()) {
			List<String> availableWindows = new ArrayList<String>(webDriver.getWindowHandles());
			if (index >= 0 && index < availableWindows.size()) {
				webDriver.switchTo().window(availableWindows.get(index));
				return true;
			}
			Thread.sleep(200);
			timeCount += 0.2;
		}
		return false;
	}

	public static boolean closeWindowUsingIndex(WebDriver webDriver, int index) throws InterruptedException {
		float timeCount = 0;
		while (timeCount < RunConfiguration.getTimeOut()) {
			List<String> availableWindows = new ArrayList<String>(webDriver.getWindowHandles());
			if (index >= 0 && index < availableWindows.size()) {
				webDriver.switchTo().window(availableWindows.get(index));
				webDriver.close();
				return true;
			}
			Thread.sleep(200);
			timeCount += 0.2;
		}
		return false;
	}


	public static void checkSelectIndex(Integer[] indexes, Select select) throws IllegalArgumentException {
		logger.logInfo(StringConstants.COMM_LOG_INFO_CHECKING_INDEX_PARAMS);
		List<WebElement> allSelectOptions = select.getOptions();
		if (allSelectOptions.size() > 0) {
			for (int index : indexes) {
				if (index < 0 || index >= allSelectOptions.size()) {
					throw new IllegalArgumentException(MessageFormat.format(StringConstants.COMM_EXC_INVALID_INDEX, index, (allSelectOptions.size() - 1)));
				}
			}
		}
	}
	
	public static void selectOrDeselectAllOptions(Select select, boolean isSelect, TestObject to) {
		if (isSelect) {
			logger.logInfo(MessageFormat.format(StringConstants.KW_LOG_INFO_SELECTING_ALL_OPT_ON_OBJ, to.getObjectId()));
		} else {
			logger.logInfo(MessageFormat.format(StringConstants.KW_LOG_INFO_DESELECTING_ALL_OPTS_ON_OBJ, to.getObjectId()));
		}
		for (int index = 0; index < select.getOptions().size(); index++) {
			if (isSelect) {
				select.selectByIndex(index);
				logger.logInfo(MessageFormat.format(StringConstants.KW_LOG_INFO_OPT_W_INDEX_X_IS_SELECTED, index));
			} else {
				select.deselectByIndex(index);
				logger.logInfo(MessageFormat.format(StringConstants.KW_LOG_INFO_DESELECTED_OPT_IDX_X, index));
			}
		}
	}
	
	public static void selectOrDeselectOptionsByIndex(Select select, Integer[] indexes, boolean isSelect, TestObject to) {
		WebUiCommonHelper.checkSelectIndex(indexes, select);
		if (isSelect) {
			logger.logInfo(MessageFormat.format(StringConstants.KW_LOG_INFO_SELECTING_OBJ_OPTS_W_INDEX_IN, to.getObjectId(), WebUiCommonHelper.integerArrayToString(indexes)));
		} else {
			logger.logInfo(MessageFormat.format(StringConstants.KW_LOG_INFO_DESELECTING_OPTS_ON_OBJ_W_IDX, to.getObjectId(), WebUiCommonHelper.integerArrayToString(indexes)));
		}
		for (int index : indexes) {
			if (isSelect) {
				select.selectByIndex(index);
				logger.logInfo(MessageFormat.format(StringConstants.KW_LOG_INFO_OPT_W_INDEX_X_IS_SELECTED, index));
			} else {
				select.deselectByIndex(index);
				logger.logInfo(MessageFormat.format(StringConstants.KW_LOG_INFO_DESELECTED_OPT_IDX_X, index));
			}
		}
	}
	
	public static void selectOrDeselectOptionsByValue(Select select, String value, boolean isRegex, boolean isSelect, TestObject to, String regularExpressionLog) {
		List<WebElement> allOptions = select.getOptions();
		if (isSelect) {
			logger.logInfo(MessageFormat.format(StringConstants.KW_LOG_INFO_SELECTING_OPTS_ON_OBJ_X_W_VAL_Y, to.getObjectId(), value, regularExpressionLog));
		} else {
			logger.logInfo(MessageFormat.format(StringConstants.KW_LOG_INFO_DESELECTING_OPTS_ON_OBJ_W_VAL, to.getObjectId(), value, regularExpressionLog));
		}
		for (int index = 0; index < allOptions.size(); index++) {
			String optionValue = allOptions.get(index).getAttribute("value");
			if (optionValue != null && WebUiCommonHelper.match(optionValue, value, isRegex)) {
				if (isSelect) {
					select.selectByIndex(index);
					logger.logInfo(MessageFormat.format(StringConstants.KW_LOG_INFO_SELECTED_OPT_AT_INDEX_W_VAL, index, optionValue, regularExpressionLog));
				} else {
					select.deselectByIndex(index);
					logger.logInfo(MessageFormat.format(StringConstants.KW_LOG_INFO_OPT_AT_IDX_X_W_VAL_Y_IS_SELECTED, index, optionValue, regularExpressionLog));
				}
				if (!isRegex) {
					break;
				}
			}
		}
	}
	
	public static void selectOrDeselectOptionsByLabel(Select select, String label, boolean isRegex, boolean isSelect, TestObject to, String regularExpressionLog) {
		List<WebElement> allOptions = select.getOptions();
		if (isSelect) {
			logger.logInfo(MessageFormat.format(StringConstants.KW_LOG_INFO_SELECTING_OPTS_ON_OBJ_X_W_LBL_Y, to.getObjectId(), label, regularExpressionLog));
		} else {
			logger.logInfo(MessageFormat.format(StringConstants.KW_LOG_INFO_DESELECTING_OPTS_ON_OBJ_X_W_LBL_Y, to.getObjectId(), label, regularExpressionLog));
		}
		for (int index = 0; index < allOptions.size(); index++) {
			String optionValue = allOptions.get(index).getText();
			if (optionValue != null && WebUiCommonHelper.match(optionValue, label, isRegex)) {
				if (isSelect) {
					select.selectByIndex(index);
					logger.logInfo(MessageFormat.format(StringConstants.KW_LOG_INFO_OPT_AT_IDX_X_W_LBL_TXT_Y_IS_SELECTED, index, optionValue, regularExpressionLog));
				} else {
					select.deselectByIndex(index);
					logger.logInfo(MessageFormat.format(StringConstants.KW_LOG_INFO_OPT_AT_IDX_X_W_LBL_TXT_Y_IS_DESELECTED, index, optionValue, regularExpressionLog));
				}
				if (!isRegex) {
					break;
				}
			}
		}
	}

	public static int getNumberOfOptionByLabel(Select select, String label, boolean isRegex, String objectId) {
		int count = 0;
		String regularExpressionLog = ((isRegex) ? " using regular expression" : "");
		logger.logInfo(MessageFormat.format(StringConstants.COMM_LOG_INFO_COUNTING_NUM_OPTS_W_LBL_PRESENT_ON_OBJ, label, objectId, regularExpressionLog));
		List<WebElement> allOptions = select.getOptions();
		for (int index = 0; index < allOptions.size(); index++) {
			String optionLabel = allOptions.get(index).getText();
			if (optionLabel != null && KeywordHelper.match(optionLabel, label, isRegex)) {
				count++;
				logger.logInfo(MessageFormat.format(StringConstants.COMM_LOG_INFO_OPT_AT_INDEX_W_LBL_IS_PRESENT, index, optionLabel, regularExpressionLog));
			}
		}
		return count;
	}

	public static int getNumberOfOptionByValue(Select select, String value, boolean isRegex, String objectId) {
		int count = 0;
		String regularExpressionLog = ((isRegex) ? " using regular expression" : "");
		logger.logInfo(MessageFormat.format(StringConstants.COMM_LOG_INFO_COUNTING_NUM_OPTS_W_VAL_PRESENT_ON_OBJ, value, objectId, regularExpressionLog));
		List<WebElement> allOptions = select.getOptions();
		for (int index = 0; index < allOptions.size(); index++) {
			String optionValue = allOptions.get(index).getAttribute("value");
			if (optionValue != null && KeywordHelper.match(optionValue, value, isRegex)) {
				count++;
				logger.logInfo(MessageFormat.format(StringConstants.COMM_LOG_INFO_OPT_AT_INDEX_W_VAL_IS_PRESENT, index, optionValue, regularExpressionLog));
			}
		}
		return count;
	}

	public static int getNumberOfSelectedOptionByLabel(Select select, String label, boolean isRegex, String objectId) {
		int count = 0;
		String regularExpressionLog = ((isRegex) ? " using regular expression" : "");
		logger.logInfo(MessageFormat.format(StringConstants.COMM_LOG_INFO_COUNTING_NUM_OPTS_W_LBL_SELECTED_ON_OBJ, label, objectId, regularExpressionLog));
		List<WebElement> allOptions = select.getOptions();
		List<WebElement> allSelectedOptions = select.getAllSelectedOptions();
		for (int index = 0; index < allOptions.size(); index++) {
			String optionLabel = allOptions.get(index).getText();
			if (optionLabel != null && KeywordHelper.match(optionLabel, label, isRegex)) {
				if (allSelectedOptions.contains(allOptions.get(index))) {
					count++;
					logger.logInfo(MessageFormat.format(StringConstants.COMM_LOG_INFO_OPT_AT_INDEX_W_LBL_IS_SELECTED, index, optionLabel, regularExpressionLog));
				}
			}
		}
		return count;
	}

	public static int getNumberOfNotSelectedOptionByLabel(Select select, String label, boolean isRegex, String objectId) {
		int count = 0;
		String regularExpressionLog = ((isRegex) ? " using regular expression" : "");
		logger.logInfo(MessageFormat.format(StringConstants.COMM_LOG_INFO_COUNTING_NUM_OPTS_W_LBL_NOT_SELECTED_ON_OBJ, label, objectId, regularExpressionLog));
		List<WebElement> allOptions = select.getOptions();
		List<WebElement> allSelectedOptions = select.getAllSelectedOptions();
		for (int index = 0; index < allOptions.size(); index++) {
			String optionLabel = allOptions.get(index).getText();
			if (optionLabel != null && KeywordHelper.match(optionLabel, label, isRegex)) {
				if (!allSelectedOptions.contains(allOptions.get(index))) {
					count++;
					logger.logInfo(MessageFormat.format(StringConstants.COMM_LOG_INFO_OPT_AT_INDEX_W_LBL_IS_NOT_SELECTED, index, optionLabel, regularExpressionLog));
				}
			}
		}
		return count;
	}

	public static int getNumberOfSelectedOptionByValue(Select select, String value, boolean isRegex, String objectId) {
		int count = 0;
		String regularExpressionLog = ((isRegex) ? " using regular expression" : "");
		logger.logInfo(MessageFormat.format(StringConstants.COMM_LOG_INFO_COUNTING_NUM_OPTS_W_VAL_SELECTED_ON_OBJ, value, objectId, regularExpressionLog));
		List<WebElement> allOptions = select.getOptions();
		List<WebElement> allSelectedOptions = select.getAllSelectedOptions();
		for (int index = 0; index < allOptions.size(); index++) {
			String optionValue = allOptions.get(index).getAttribute("value");
			if (optionValue != null && KeywordHelper.match(optionValue, value, isRegex)) {
				if (allSelectedOptions.contains(allOptions.get(index))) {
					count++;
					logger.logInfo(MessageFormat.format(StringConstants.COMM_LOG_INFO_OPT_AT_INDEX_W_VAL_IS_SELECTED, index, optionValue, regularExpressionLog));
				}
			}
		}
		return count;
	}

	public static int getNumberOfNotSelectedOptionByValue(Select select, String value, boolean isRegex, String objectId) {
		int count = 0;
		String regularExpressionLog = ((isRegex) ? " using regular expression" : "");
		logger.logInfo(MessageFormat.format(StringConstants.COMM_LOG_INFO_COUNTING_NUM_OPTS_W_VAL_NOT_SELECTED_ON_OBJ, value, objectId, regularExpressionLog));
		List<WebElement> allOptions = select.getOptions();
		List<WebElement> allSelectedOptions = select.getAllSelectedOptions();
		for (int index = 0; index < allOptions.size(); index++) {
			String optionValue = allOptions.get(index).getAttribute("value");
			if (optionValue != null && KeywordHelper.match(optionValue, value, isRegex)) {
				if (!allSelectedOptions.contains(allOptions.get(index))) {
					count++;
					logger.logInfo(MessageFormat.format(StringConstants.COMM_LOG_INFO_OPT_AT_INDEX_W_VAL_IS_NOT_SELECTED, index, optionValue, regularExpressionLog));
				}
			}
		}
		return count;
	}

	public static int getNumberOfSelectedOptionByIndex(Select select, Integer[] indexes, String objectId)
			throws IllegalArgumentException {
				logger.logInfo(MessageFormat.format(StringConstants.COMM_LOG_INFO_COUNTING_NUM_OPTS_W_INDEX_RANGE_SELECTED_ON_OBJ, integerArrayToString(indexes), objectId));
		int count = 0;
		List<WebElement> allSelectedOptions = select.getAllSelectedOptions();
		for (int index : indexes) {
			// Index is 0-based, lstIndexes is list of 0-based indexing
			// number
			if (allSelectedOptions.contains(select.getOptions().get(index))) {
				count++;
				logger.logInfo(MessageFormat.format(StringConstants.COMM_LOG_INFO_OPT_AT_INDEX_IS_SELECTED, index));
			}
		}
		return count;
	}

	public static int getNumberOfNotSelectedOptionByIndex(Select select, Integer[] indexes, String objectId) {
		logger.logInfo(MessageFormat.format(StringConstants.COMM_LOG_INFO_COUNTING_NUM_OPTS_W_INDEX_RANGE_NOT_SELECTED_ON_OBJ, integerArrayToString(indexes), objectId));
		int count = 0;
		List<WebElement> allSelectedOptions = select.getAllSelectedOptions();
		for (int index : indexes) {
			// Index is 0-based, lstIndexes is list of 0-based indexing
			// number
			if (!allSelectedOptions.contains(select.getOptions().get(index))) {
				count++;
				logger.logInfo(MessageFormat.format(StringConstants.COMM_LOG_INFO_OPT_AT_INDEX_IS_NOT_SELECTED, index));
			}
		}
		return count;
	}
	
	public static void focusOnBrowser() throws WebDriverException, StepFailedException {
		((JavascriptExecutor) DriverFactory.getWebDriver()).executeScript("window.focus()");
	}
}
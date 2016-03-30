package com.kms.katalon.core.logging;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SocketHandler;

import org.apache.commons.lang.StringUtils;

import com.kms.katalon.core.configuration.RunConfiguration;
import com.kms.katalon.core.constants.StringConstants;

public class KeywordLogger {    
    private static final int MAXIMUM_LOG_FILES = 100;
    
    private static final int MAXIMUM_LOG_FILE_SIZE = 10 * 1024 * 1024; //10MB
    
    private Logger logger;

    private String pendingDescription = null;

    private Stack<KeywordStackElement> currentKeywordStack = null;

    private Stack<Stack<KeywordStackElement>> keywordStacksContainer = new Stack<Stack<KeywordStackElement>>();

    private int nestedLevel;
    
    private static final ThreadLocal<KeywordLogger> localKeywordLoggerStorage = new ThreadLocal<KeywordLogger>() {
        @Override
        protected KeywordLogger initialValue() {
            return new KeywordLogger();
        }
    };

    public class KeywordStackElement {
        private String keywordName;

        private int nestedLevel;

        public KeywordStackElement(String keywordName, int nestedLevel) {
            this.setKeywordName(keywordName);
            this.setNestedLevel(nestedLevel);
        }

        public String getKeywordName() {
            return keywordName;
        }

        public void setKeywordName(String keywordName) {
            this.keywordName = keywordName;
        }

        public int getNestedLevel() {
            return nestedLevel;
        }

        public void setNestedLevel(int nestedLevel) {
            this.nestedLevel = nestedLevel;
        }
    }

    public static KeywordLogger getInstance() {
        return localKeywordLoggerStorage.get();
    }

    private KeywordLogger() {
    }

    /**
     * @return Returns current logger if it exists. Otherwise, create a new one includes: customized log file with XML
     * format and customized console handler.
     */
    private Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(KeywordLogger.class.getName());

            // remove default parent's setting
            logger.setUseParentHandlers(false);            
        }

        String logFolder = getLogFolderPath();
        if (logger.getHandlers().length == 0 && StringUtils.isNotEmpty(logFolder)) {
            try {                
                SystemConsoleHandler consoleHandler = new SystemConsoleHandler();
                logger.addHandler(consoleHandler);
                
                // Split log into 100 files, every file is maximum 10MB                
                FileHandler fileHandler = new FileHandler(logFolder + File.separator + "execution%g.log",
                        MAXIMUM_LOG_FILE_SIZE, MAXIMUM_LOG_FILES, true);
                
                CustomXmlFormatter formatter = new CustomXmlFormatter();
                fileHandler.setFormatter(formatter);
                logger.addHandler(fileHandler);

                SocketHandler socketHandler = new SystemSocketHandler(getHostAddress(), getPort());
                logger.addHandler(socketHandler);
            } catch (SecurityException | IOException e) {
                System.err.println("Unable to create logger. Root cause (" + e.getMessage() + ").");
            }
        }
        return logger;
    }
    
    public void close() {
        for (Handler handler : logger.getHandlers()) {
            handler.close();
        }
    }

    public static void cleanUp() {
        
    }

    public String getLogFolderPath() {
        String logFilePath = RunConfiguration.getSettingFilePath();
        return (logFilePath != null) ? new File(logFilePath).getParentFile().getAbsolutePath() : null;
    }
    
    private int getPort() {
        return RunConfiguration.getPort();
    }
    
    private String getHostAddress() {
        return RunConfiguration.getHostAddress();
    }

    public void startSuite(String name, Map<String, String> attributes) {
        getLogger().log(
                new XmlLogRecord(LogLevel.START.getLevel(), StringConstants.LOG_START_SUITE + " : " + name, nestedLevel,
                        attributes));
        logRunData(RunConfiguration.HOST_NAME, RunConfiguration.getHostName());
        logRunData(RunConfiguration.HOST_OS, RunConfiguration.getOS());
    }

    public void endSuite(String name, Map<String, String> attributes) {
        getLogger().log(
                new XmlLogRecord(LogLevel.END.getLevel(), StringConstants.LOG_END_SUITE + " : " + name, nestedLevel, attributes));
    }

    public void startTest(String name, Map<String, String> attributes, Stack<KeywordStackElement> keywordStack,
            boolean isOptional) {
        nestedLevel++;
        getLogger()
                .log(new XmlLogRecord(LogLevel.START.getLevel(), StringConstants.LOG_START_TEST + " : " + name, nestedLevel,
                        attributes));
        if (currentKeywordStack != null) {
            keywordStacksContainer.push(currentKeywordStack);
        }
        this.currentKeywordStack = keywordStack;
    }

    public void endTest(String name, Map<String, String> attributes) {
        nestedLevel--;
        getLogger().log(
                new XmlLogRecord(LogLevel.END.getLevel(), StringConstants.LOG_END_TEST + " : " + name, nestedLevel, attributes));

        if (!keywordStacksContainer.isEmpty()) {
            currentKeywordStack = keywordStacksContainer.pop();
        } else {
            currentKeywordStack = null;
        }
    }

    public void startKeyword(String name, Map<String, String> attributes, Stack<KeywordStackElement> keywordStack) {
        if (attributes == null) {
            attributes = new HashMap<String, String>();
        }
        if (pendingDescription != null) {
            attributes.put(StringConstants.XML_LOG_DESCRIPTION_PROPERTY, pendingDescription);
            pendingDescription = null;
        }
        getLogger().log(
                new XmlLogRecord(LogLevel.START.getLevel(), StringConstants.LOG_START_KEYWORD + " : " + name, nestedLevel,
                        attributes));
        if (currentKeywordStack != null) {
            keywordStacksContainer.push(currentKeywordStack);
        }
        this.currentKeywordStack = keywordStack;
    }

    public void startKeyword(String name, Map<String, String> attributes, int nestedLevel) {
        if (attributes == null) {
            attributes = new HashMap<String, String>();
        }
        if (pendingDescription != null) {
            attributes.put(StringConstants.XML_LOG_DESCRIPTION_PROPERTY, pendingDescription);
            pendingDescription = null;
        }
        popKeywordFromStack(nestedLevel);
        getLogger().log(
                new XmlLogRecord(LogLevel.START.getLevel(), StringConstants.LOG_START_KEYWORD + " : " + name, nestedLevel,
                        attributes));
        pushKeywordToStack(name, nestedLevel);
    }

    private void pushKeywordToStack(String keywordName, int nestedLevel) {
        if (currentKeywordStack != null) {
            currentKeywordStack.push(new KeywordStackElement(keywordName, nestedLevel));
        }
    }

    private void popKeywordFromStack(int nestedLevel) {
        while (currentKeywordStack != null && !currentKeywordStack.isEmpty()
                && currentKeywordStack.peek().getNestedLevel() >= nestedLevel) {
            KeywordStackElement keywordStackElement = currentKeywordStack.pop();
            endKeyword(keywordStackElement.getKeywordName(), null, keywordStackElement.getNestedLevel());
        }
    }

    public void endKeyword(String name, Map<String, String> attributes, int nestedLevel) {
        getLogger()
                .log(new XmlLogRecord(LogLevel.END.getLevel(), StringConstants.LOG_END_KEYWORD + " : " + name, nestedLevel,
                        attributes));
    }

    public void endKeyword(String name, Map<String, String> attributes, Stack<KeywordStackElement> keywordStack) {
        getLogger()
                .log(new XmlLogRecord(LogLevel.END.getLevel(), StringConstants.LOG_END_KEYWORD + " : " + name, nestedLevel,
                        attributes));
        if (currentKeywordStack == keywordStack && !keywordStacksContainer.isEmpty()) {
            currentKeywordStack = keywordStacksContainer.pop();
        } else {
            currentKeywordStack = null;
        }
    }

    public void logFailed(String message) {
        logMessage(LogLevel.FAILED, message);
    }

    public void logFailed(String message, Map<String, String> attributes) {
        logMessage(LogLevel.FAILED, message, attributes);
    }

    public void logWarning(String message) {
        logMessage(LogLevel.WARNING, message);
    }

    public void logWarning(String message, Map<String, String> attributes) {
        logMessage(LogLevel.WARNING, message, attributes);
    }

    public void logPassed(String message) {
        logMessage(LogLevel.PASSED, message);
    }

    public void logPassed(String message, Map<String, String> attributes) {
        logMessage(LogLevel.PASSED, message, attributes);
    }

    public void logInfo(String message) {
        logMessage(LogLevel.INFO, message);
    }

    public void logInfo(String message, Map<String, String> attributes) {
        logMessage(LogLevel.INFO, message, attributes);
    }

    public void logRunData(String dataKey, String dataValue) {
        Map<String, String> attributeMap = new HashMap<String, String>();
        attributeMap.put(dataKey, dataValue);
        logMessage(LogLevel.RUN_DATA, "Logging run data '" + dataKey + "' with value '" + dataValue + "'", attributeMap);
    }

    public void logError(String message) {
        logMessage(LogLevel.ERROR, message);
    }

    public void logError(String message, Map<String, String> attributes) {
        logMessage(LogLevel.ERROR, message, attributes);
    }

    public void logMessage(LogLevel level, String message) {
        if (message == null) {
            message = "";
        }
        Logger logger = getLogger();
        if (logger != null) {
            logger.log(new XmlLogRecord(level.getLevel(), message, nestedLevel));
        }
    }

    public void logMessage(LogLevel level, String message, Map<String, String> attributes) {
        if (message == null) {
            message = "";
        }
        Logger logger = getLogger();
        if (logger != null) {
            logger.log(new XmlLogRecord(level.getLevel(), message, nestedLevel, attributes));
        }
        
    }

    public void logMessage(LogLevel level, String message, Throwable thrown) {
        if (message == null) {
            message = "";
        }
        Logger logger = getLogger();
        if (logger != null) {
            logger.log(level.getLevel(), message, thrown);
        }
    }

    public void setPendingDescription(String stepDescription) {
        pendingDescription = stepDescription;
    }
}
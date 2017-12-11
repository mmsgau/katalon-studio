package com.kms.katalon.core.main;

import static com.kms.katalon.core.constants.StringConstants.DF_CHARSET;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.io.FileUtils;
import org.codehaus.groovy.ast.MethodNode;

import com.kms.katalon.core.annotation.AfterTestSuite;
import com.kms.katalon.core.annotation.BeforeTestSuite;
import com.kms.katalon.core.annotation.SetUp;
import com.kms.katalon.core.annotation.SetupTestCase;
import com.kms.katalon.core.annotation.TearDown;
import com.kms.katalon.core.annotation.TearDownTestCase;
import com.kms.katalon.core.configuration.RunConfiguration;
import com.kms.katalon.core.constants.StringConstants;
import com.kms.katalon.core.context.internal.InternalTestSuiteContext;
import com.kms.katalon.core.context.internal.TestContextEvaluator;
import com.kms.katalon.core.driver.internal.DriverCleanerCollector;
import com.kms.katalon.core.logging.ErrorCollector;
import com.kms.katalon.core.logging.KeywordLogger;
import com.kms.katalon.core.logging.KeywordLogger.KeywordStackElement;
import com.kms.katalon.core.model.FailureHandling;
import com.kms.katalon.core.testcase.TestCaseBinding;

import groovy.lang.Binding;

public class TestSuiteExecutor {

    private static final String TS_SCRIPTS_ROOT_FOLDER = "Test Scripts";

    private static final KeywordLogger LOG = KeywordLogger.getInstance();

    private final String testSuiteId;

    private final ScriptEngine scriptEngine;

    private InternalTestSuiteContext testSuiteContext;

    private TestContextEvaluator contextEvaluator;

    private ScriptCache scriptCache;

    private static final Set<String> TEST_SUITE_ANNOTATION_METHODS;

    static {
        TEST_SUITE_ANNOTATION_METHODS = new HashSet<>();
        TEST_SUITE_ANNOTATION_METHODS.add(SetUp.class.getName());
        TEST_SUITE_ANNOTATION_METHODS.add(TearDown.class.getName());
        TEST_SUITE_ANNOTATION_METHODS.add(SetupTestCase.class.getName());
        TEST_SUITE_ANNOTATION_METHODS.add(TearDownTestCase.class.getName());
    }

    public TestSuiteExecutor(String testSuiteId, ScriptEngine scriptEngine, TestContextEvaluator contextEvaluator) {
        this.testSuiteId = testSuiteId;
        this.scriptEngine = scriptEngine;
        
        this.contextEvaluator = contextEvaluator;
        this.testSuiteContext = new InternalTestSuiteContext();
        testSuiteContext.setTestSuiteId(testSuiteId);
    }

    public void execute(Map<String, String> suiteProperties, List<TestCaseBinding> testCaseBindings) {
        LOG.startSuite(testSuiteId, suiteProperties);

        contextEvaluator.invokeListenerMethod(BeforeTestSuite.class.getName(), new Object[] { testSuiteContext });

        accessTestSuiteMainPhase(testCaseBindings);

        String status = "COMPLETE";
        if (ErrorCollector.getCollector().containsErrors()) {
            status = "ERROR";
        }
        testSuiteContext.setStatus(status);

        contextEvaluator.invokeListenerMethod(AfterTestSuite.class.getName(), new Object[] { testSuiteContext });
        
        if (RunConfiguration.shouldTerminateDriverAfterTestSuite()) {
            DriverCleanerCollector.getInstance().cleanDrivers();
        }

        LOG.endSuite(testSuiteId, Collections.emptyMap());
    }

    private void accessTestSuiteMainPhase(List<TestCaseBinding> testCaseBindings) {
        ErrorCollector errorCollector = ErrorCollector.getCollector();
        try {
            this.scriptCache = new ScriptCache(testSuiteId);
        } catch (IOException e) {
            errorCollector.addError(e);
            return;
        }
        invokeTestSuiteMethod(SetUp.class.getName(), StringConstants.LOG_SETUP_ACTION, false);
        if (errorCollector.containsErrors()) {
            return;
        }

        for (TestCaseBinding tcBinding : testCaseBindings) {
            accessTestCaseMainPhase(tcBinding);
        }

        invokeTestSuiteMethod(TearDown.class.getName(), StringConstants.LOG_TEAR_DOWN_ACTION, true);
    }

    private void accessTestCaseMainPhase(TestCaseBinding tcBinding) {
        ErrorCollector errorCollector = ErrorCollector.getCollector();
        List<Throwable> coppiedErrors = errorCollector.getCoppiedErrors();
        errorCollector.clearErrors();

        try {
            TestCaseExecutor testCaseExecutor = new TestCaseExecutor(tcBinding, scriptEngine, contextEvaluator);
            testCaseExecutor.setTestSuiteExecutor(this);
            testCaseExecutor.execute(FailureHandling.STOP_ON_FAILURE);
        } finally {
            errorCollector.clearErrors();
            errorCollector.getErrors().addAll(coppiedErrors);
        }
    }

    public void invokeEachTestCaseMethod(String methodName, String actionType, boolean ignoredIfFailed) {
        invokeTestSuiteMethod(methodName, actionType, ignoredIfFailed);
    }

    private void invokeTestSuiteMethod(String annotatedMethodName, String actionType, boolean ignoredIfFailed) {
        if (!scriptCache.hasScriptContent()) {
            return;
        }

        try {
            List<MethodNode> annotatedMethods = scriptCache.getMethodNodes(annotatedMethodName);
            if (annotatedMethods.isEmpty()) {
                return;
            }

            scriptEngine.changeConfigForExecutingScript();

            annotatedMethods.forEach(methodNode -> {
                runMethod(methodNode.getName(), actionType, ignoredIfFailed);
            });
        } catch (IOException e) {
            if (!ignoredIfFailed) {
                ErrorCollector.getCollector().addError(e);
            }
        } catch (ClassNotFoundException ignored) {}
    }

    private void runMethod(String methodName, String actionType, boolean ignoredIfFailed) {
        Stack<KeywordStackElement> keywordStack = new Stack<>();

        Map<String, String> startKeywordAttributeMap = new HashMap<>();
        startKeywordAttributeMap.put(StringConstants.XML_LOG_IS_IGNORED_IF_FAILED, String.valueOf(ignoredIfFailed));
        LOG.startKeyword(methodName, actionType, startKeywordAttributeMap, keywordStack);

        ErrorCollector errorCollector = ErrorCollector.getCollector();
        List<Throwable> oldErrors = errorCollector.getCoppiedErrors();

        try {
            errorCollector.clearErrors();
            scriptEngine.runScriptMethodAsRawText(scriptCache.scriptContent, scriptCache.className, methodName,
                    new Binding());
            endAllUnfinishedKeywords(keywordStack);
        } catch (Throwable e) {
            errorCollector.getErrors().add(e);
        }

        if (errorCollector.containsErrors()) {
            endAllUnfinishedKeywords(keywordStack);
            String errorMessage = errorCollector.getFirstError().getMessage();
            if (ignoredIfFailed) {
                LOG.logWarning(errorMessage);
            } else {
                oldErrors.add(errorCollector.getFirstError());
                LOG.logError(errorMessage);
            }
        } else {
            LOG.logPassed(MessageFormat.format(StringConstants.MAIN_LOG_PASSED_METHOD_COMPLETED, methodName));
        }

        errorCollector.clearErrors();
        errorCollector.getErrors().addAll(oldErrors);
        LOG.endKeyword(methodName, Collections.emptyMap(), keywordStack);
    }

    private void endAllUnfinishedKeywords(Stack<KeywordStackElement> keywordStack) {
        while (!keywordStack.isEmpty()) {
            KeywordStackElement keywordStackElement = keywordStack.pop();
            LOG.endKeyword(keywordStackElement.getKeywordName(), null, keywordStackElement.getNestedLevel());
        }
    }

    private class ScriptCache {
        private File scriptFile;

        private AnnotatedMethodCollector annotatedMethodCollector;

        private String className;

        private String scriptContent;

        private ScriptCache(String testSuiteId) throws IOException {
            this.scriptFile = getTestSuiteScriptFile(testSuiteId);
            if (this.scriptFile != null) {
                scriptContent = FileUtils.readFileToString(scriptFile, DF_CHARSET);
                className = scriptFile.toURI().toURL().toExternalForm();

                this.annotatedMethodCollector = new AnnotatedMethodCollector(TEST_SUITE_ANNOTATION_METHODS);
            }
        }

        public List<MethodNode> getMethodNodes(String annotatedMethodName) throws IOException {
            Map<String, List<MethodNode>> methodNodeCollection = annotatedMethodCollector.getMethodNodes(scriptContent);
            if (!methodNodeCollection.containsKey(annotatedMethodName)) {
                return Collections.emptyList();
            }
            return methodNodeCollection.get(annotatedMethodName);
        }

        public boolean hasScriptContent() {
            return scriptFile != null && scriptFile.exists();
        }

        private File getTestSuiteScriptFile(String testSuiteId) {
            File rootFolder = new File(RunConfiguration.getProjectDir(), TS_SCRIPTS_ROOT_FOLDER);

            File tsScriptFolder = new File(rootFolder, testSuiteId);

            if (!tsScriptFolder.exists()) {
                return null;
            }

            File[] scripts = tsScriptFolder.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return new File(dir, name).isFile() && name.matches("Script\\d{13}\\.groovy");
                }
            });
            if (scripts != null && scripts.length > 0) {
                return scripts[0];
            }
            return null;
        }
    }
}

package com.kms.katalon.composer.testcase.parts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MCompositePart;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.internal.e4.compatibility.CompatibilityEditor;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import com.kms.katalon.composer.components.impl.tree.FolderTreeEntity;
import com.kms.katalon.composer.components.impl.tree.TestCaseTreeEntity;
import com.kms.katalon.composer.components.impl.util.EntityPartUtil;
import com.kms.katalon.composer.components.log.LoggerSingleton;
import com.kms.katalon.composer.components.tree.ITreeEntity;
import com.kms.katalon.composer.parts.MultipleTabsCompositePart;
import com.kms.katalon.composer.testcase.constants.ImageConstants;
import com.kms.katalon.composer.testcase.constants.StringConstants;
import com.kms.katalon.composer.testcase.model.TestCaseTreeTableInput.NodeAddType;
import com.kms.katalon.composer.testcase.util.TestCaseEntityUtil;
import com.kms.katalon.constants.EventConstants;
import com.kms.katalon.constants.IdConstants;
import com.kms.katalon.controller.FolderController;
import com.kms.katalon.controller.TestCaseController;
import com.kms.katalon.core.groovy.GroovyParser;
import com.kms.katalon.entity.folder.FolderEntity;
import com.kms.katalon.entity.testcase.TestCaseEntity;
import com.kms.katalon.entity.variable.VariableEntity;
import com.kms.katalon.groovy.util.GroovyEditorUtil;
import com.kms.katalon.groovy.util.GroovyUtil;

@SuppressWarnings("restriction")
public class TestCaseCompositePart implements EventHandler, MultipleTabsCompositePart {

    public static final int CHILD_TEST_CASE_EDITOR_PART_INDEX = 1;
    private static final int CHILD_TEST_CASE_MANUAL_PART_INDEX = 0;
    private static final int CHILD_TEST_CASE_VARIABLE_PART_INDEX = 2;
    private static final int CHILD_TEST_CASE_INTEGRATION_PART_INDEX = 3;
    public static final String SCRIPT_TAB_TITLE = StringConstants.PA_TAB_SCRIPT;
    public static final String MANUAL_TAB_TITLE = StringConstants.PA_TAB_MANUAL;
    public static final String VARIABLE_TAB_TITLE = StringConstants.PA_TAB_VARIABLE;
    public static final String INTEGRATION_TAB_TITLE = StringConstants.PA_TAB_INTEGRATION;

    @Inject
    private MDirtyable dirty;

    public MDirtyable getDirty() {
        return dirty;
    }

    private IPropertyListener childPropertyListner;

    private MCompositePart compositePart;

    @Inject
    private EPartService partService;

    @Inject
    private IEventBroker eventBroker;

    @Inject
    protected EModelService modelService;

    @Inject
    protected MApplication application;

    @Named(IServiceConstants.ACTIVE_SHELL)
    private Shell parentShell;

    private TestCasePart childTestCasePart;

    private TestCaseVariablePart childTestCaseVariablesPart;

    private CompatibilityEditor childTestCaseEditorPart;

    private TestCaseIntegrationPart childTestCaseIntegrationPart;

    private GroovyEditor groovyEditor;

    private CTabFolder tabFolder;

    private MPartStack subPartStack;

    private boolean editorLastDirty;
    private boolean isInitialized;
    private boolean isScriptChanged;

    private TestCaseEntity testCase;
    private TestCaseEntity originalTestCase;
    private List<ASTNode> astNodes;

    public boolean isInitialized() {
        return isInitialized;
    }

    @PostConstruct
    public void init(Composite parent, MCompositePart compositePart, TestCaseEntity testCase) {
        this.compositePart = compositePart;
        dirty.setDirty(false);
        isInitialized = false;
        isScriptChanged = false;
        changeOriginalTestCase(testCase);
        initListeners();
    }

    public void initComponent() {
        if (compositePart.getChildren().size() == 1 && compositePart.getChildren().get(0) instanceof MPartStack) {
            subPartStack = (MPartStack) compositePart.getChildren().get(0);
            if (subPartStack.getChildren().size() == 4) {
                for (MStackElement stackElement : subPartStack.getChildren()) {
                    if (stackElement instanceof MPart) {
                        if (((MPart) stackElement).getObject() instanceof TestCasePart) {
                            childTestCasePart = (TestCasePart) ((MPart) stackElement).getObject();
                        } else if (((MPart) stackElement).getObject() instanceof CompatibilityEditor) {
                            initChildEditorPart((CompatibilityEditor) ((MPart) stackElement).getObject());
                        } else if (((MPart) stackElement).getObject() instanceof TestCaseVariablePart) {
                            childTestCaseVariablesPart = (TestCaseVariablePart) ((MPart) stackElement).getObject();
                        } else if (((MPart) stackElement).getObject() instanceof TestCaseIntegrationPart) {
                            childTestCaseIntegrationPart = (TestCaseIntegrationPart) ((MPart) stackElement).getObject();
                        }
                    }
                }
            }

            if (subPartStack.getWidget() instanceof CTabFolder) {
                tabFolder = (CTabFolder) subPartStack.getWidget();

                tabFolder.setTabPosition(SWT.BOTTOM);
                tabFolder.setBorderVisible(false);
                tabFolder.setMaximizeVisible(false);
                tabFolder.setMinimizeVisible(false);

                if (tabFolder.getItemCount() == 4) {
                    CTabItem testCasePartTab = tabFolder.getItem(CHILD_TEST_CASE_MANUAL_PART_INDEX);
                    testCasePartTab.setText(MANUAL_TAB_TITLE);
                    testCasePartTab.setImage(ImageConstants.IMG_16_MANUAL);
                    testCasePartTab.setShowClose(false);

                    CTabItem groovyEditorPartTab = tabFolder.getItem(CHILD_TEST_CASE_EDITOR_PART_INDEX);
                    groovyEditorPartTab.setText(SCRIPT_TAB_TITLE);
                    groovyEditorPartTab.setImage(ImageConstants.IMG_16_SCRIPT);
                    groovyEditorPartTab.setShowClose(false);

                    CTabItem variablePartTab = tabFolder.getItem(CHILD_TEST_CASE_VARIABLE_PART_INDEX);
                    variablePartTab.setText(VARIABLE_TAB_TITLE);
                    variablePartTab.setImage(ImageConstants.IMG_16_VARIABLE);
                    variablePartTab.setShowClose(false);

                    CTabItem integrationPartTab = tabFolder.getItem(CHILD_TEST_CASE_INTEGRATION_PART_INDEX);
                    integrationPartTab.setText(INTEGRATION_TAB_TITLE);
                    integrationPartTab.setImage(ImageConstants.IMG_16_INTEGRATION);
                    integrationPartTab.setShowClose(false);
                }

                tabFolder.addSelectionListener(new SelectionAdapter() {
                    public void widgetSelected(SelectionEvent event) {
                        if (tabFolder.getSelectionIndex() == CHILD_TEST_CASE_MANUAL_PART_INDEX && isScriptChanged) {
                            setScriptContentToManual();
                        } else if (tabFolder.getSelectionIndex() == CHILD_TEST_CASE_EDITOR_PART_INDEX
                                && childTestCasePart.isManualScriptChanged()) {
                            setChildEditorContents(astNodes);
                        }
                    }
                });
            }
            setScriptContentToManual();
            childTestCaseVariablesPart.loadVariables();
            childTestCaseIntegrationPart.loadInput();
            isInitialized = true;
        }

    }

    private void initChildEditorPart(CompatibilityEditor compatibilityEditor) {
        childTestCaseEditorPart = compatibilityEditor;
        groovyEditor = (GroovyEditor) childTestCaseEditorPart.getEditor();
        groovyEditor.getViewer().getDocument().addDocumentListener(new IDocumentListener() {
            @Override
            public void documentChanged(DocumentEvent event) {
                try {
                    if (!childTestCasePart.isManualScriptChanged()) {
                        if (!subPartStack.getSelectedElement().equals(partService.getActivePart())) {
                            setScriptContentToManual();
                        } else {
                            if (!isScriptChanged) {
                                isScriptChanged = true;
                            }

                            childTestCaseEditorPart.getModel().setDirty(true);
                        }

                        GroovyEditorUtil.showProblems(groovyEditor);
                    }
                    checkDirty();
                } catch (Exception e) {
                    LoggerSingleton.logError(e);
                }

            }

            @Override
            public void documentAboutToBeChanged(DocumentEvent event) {
                // TODO Auto-generated method stub
            }
        });
    }

    public void changeOriginalTestCase(TestCaseEntity testCase) {
        originalTestCase = testCase;
        cloneTestCase();
    }

    private void cloneTestCase() {
        testCase = originalTestCase.clone();
        TestCaseEntityUtil.copyTestCaseProperties(originalTestCase, testCase);
        testCase.setTestCaseGuid(originalTestCase.getTestCaseGuid());
    }

    public boolean setScriptContentToManual() {
        try {
            astNodes = getAstNodesFromScript();
            if (astNodes == null || astNodes.isEmpty()) {
                astNodes = GroovyParser.generateNewScript();
            }
            if (childTestCasePart != null && groovyEditor != null) {
                childTestCasePart.loadASTNodesToTreeTable(astNodes);
                isScriptChanged = false;
                return true;
            }
        } catch (CompilationFailedException exception) {
            MessageDialog.openError(null, StringConstants.ERROR_TITLE,
                    StringConstants.PA_ERROR_MSG_PLS_FIX_ERROR_IN_SCRIPT);
            subPartStack.setSelectedElement(childTestCaseEditorPart.getModel());
            isScriptChanged = true;
            GroovyEditorUtil.showProblems(groovyEditor);
        } catch (Exception e) {
            LoggerSingleton.logError(e);
        }
        return false;
    }

    public List<ASTNode> getAstNodesFromScript() throws Exception {
        if (groovyEditor != null) {
            return GroovyParser.parseGroovyScriptIntoAstNodes(groovyEditor.getViewer().getDocument().get());
        }
        return Collections.emptyList();
    }

    private boolean setChildEditorContents(List<ASTNode> astNodes) {
        while (groovyEditor.getViewer() == null) {
            // wait for groovy Editor appears
        }

        if (groovyEditor.getViewer() != null) {
            try {
                StringBuilder stringBuilder = new StringBuilder();
                new GroovyParser(stringBuilder).parseGroovyAstIntoScript(astNodes);
                groovyEditor.getViewer().getDocument().set(stringBuilder.toString());
                childTestCasePart.setManualScriptChanged(false);
                childTestCaseEditorPart.getModel().setDirty(true);
                checkDirty();
                return true;
            } catch (Exception e) {
                LoggerSingleton.logError(e);
            }
        }
        return false;
    }

    private void initListeners() {
        eventBroker.subscribe(EventConstants.TESTCASE_UPDATED, this);
        eventBroker.subscribe(EventConstants.ECLIPSE_EDITOR_CLOSED, this);
        eventBroker.subscribe(EventConstants.EXPLORER_REFRESH_SELECTED_ITEM, this);

        childPropertyListner = new IPropertyListener() {
            @Override
            public void propertyChanged(Object source, int propId) {
                if (source instanceof GroovyEditor && propId == ISaveablePart.PROP_DIRTY) {
                    editorLastDirty = childTestCaseEditorPart.getModel().isDirty();
                }
            }
        };
    }
    
    public TestCasePart getChildTestCasePart() {
        return childTestCasePart;
    }

    public MPart getChildManualPart() {
        return childTestCasePart.getMPart();
    }

    public MPart getChildCompatibilityPart() {
        return childTestCaseEditorPart.getModel();
    }

    public MPart getChildVariablesPart() {
        return childTestCaseVariablesPart.getMPart();
    }

    public MPart getChildIntegrationPart() {
        return childTestCaseIntegrationPart.getMPart();
    }

    public TestCaseEntity getTestCase() {
        return testCase;
    }

    @Override
    public void save() throws Exception {
        if (childTestCasePart.isManualScriptChanged()) {
            setChildEditorContents(astNodes);
        }
        saveTestScript();
        saveTestCase();
    }

    public void addVariables(VariableEntity[] variables) {
        childTestCaseVariablesPart.addVariable(variables);
    }

    public void addStatements(List<Statement> statements) throws Exception {
        childTestCasePart.addStatements(statements, NodeAddType.InserAfter);
    }

    private boolean validateInput() {
        IStatus status = ResourcesPlugin.getWorkspace().validateName(testCase.getName(), IResource.FOLDER);
        if (status.isOK()) {
            return childTestCaseVariablesPart.validateVariables();
        } else {
            MessageDialog.openError(Display.getCurrent().getActiveShell(), StringConstants.ERROR_TITLE,
                    status.getMessage());
            return false;
        }
    }

    public boolean saveTestCase() throws Exception {
        if (validateInput()) {
            // preSave

            // back-up
            String oldPk = originalTestCase.getId();
            String oldIdForDisplay = TestCaseController.getInstance().getIdForDisplay(originalTestCase);
            TestCaseEntity temp = new TestCaseEntity();
            TestCaseEntityUtil.copyTestCaseProperties(originalTestCase, temp);
            TestCaseEntityUtil.copyTestCaseProperties(testCase, originalTestCase);
            try {
                boolean nameChanged = !originalTestCase.getName().equals(temp.getName());
                if (nameChanged) {
                    GroovyUtil.loadScriptContentIntoTestCase(temp);
                    originalTestCase.setScriptContents(temp.getScriptContents());
                    temp.setScriptContents(originalTestCase.getScriptContents());
                }
                TestCaseController.getInstance().updateTestCase(originalTestCase);
                // Send event if Test Case name has changed
                if (nameChanged) {
                    eventBroker.post(EventConstants.EXPLORER_RENAMED_SELECTED_ITEM, new Object[] { oldIdForDisplay,
                            TestCaseController.getInstance().getIdForDisplay(originalTestCase) });

                    // refresh TreeExplorer
                    eventBroker.post(EventConstants.EXPLORER_REFRESH_TREE_ENTITY, null);
                }

                // raise Event to update Test Suite Part and others Test Case
                // Part
                // which refer to test case
                eventBroker.send(EventConstants.TESTCASE_UPDATED, new Object[] { oldPk, originalTestCase });
                
                originalTestCase.setScriptContents(new byte[0]);
                temp.setScriptContents(new byte[0]);
                return true;
            } catch (Exception e) {
                // revert
                TestCaseEntityUtil.copyTestCaseProperties(temp, originalTestCase);
                originalTestCase.setScriptContents(temp.getScriptContents());
                
                LoggerSingleton.logError(e);
                MessageDialog.openWarning(Display.getCurrent().getActiveShell(), StringConstants.WARN_TITLE,
                        e.getMessage());
            }
        }
        return false;
    }

    public boolean saveTestScript() {
        try {
            groovyEditor.doSave(null);
            return true;
        } catch (Exception e) {
            LoggerSingleton.logError(e);
            MessageDialog.openError(parentShell, StringConstants.ERROR_TITLE, e.getMessage());
        }
        return false;
    }

    public void setDirty(boolean isDirty) {
        dirty.setDirty(isDirty);
    }

    public void checkDirty() {
        dirty.setDirty(isAnyChildDirty());
        childTestCasePart.getMPart().setDirty(false);
        childTestCaseVariablesPart.getMPart().setDirty(false);
        childTestCaseEditorPart.getModel().setDirty(false);
        childTestCaseIntegrationPart.getMPart().setDirty(false);
    }

    private boolean isAnyChildDirty() {
        return childTestCasePart.getMPart().isDirty() || childTestCaseEditorPart.getModel().isDirty()
                || childTestCaseVariablesPart.getMPart().isDirty() || childTestCaseIntegrationPart.getMPart().isDirty();
    }

    @Persist
    public void onSave() {
        try {
            save();
        } catch (Exception e) {
            MessageDialog
                    .openError(null, StringConstants.ERROR_TITLE, StringConstants.PA_ERROR_MSG_UNABLE_TO_SAVE_PART);
            LoggerSingleton.logError(e);
        }
    }

    public IPropertyListener getChildPropertyListner() {
        return childPropertyListner;
    }

    @Override
    public void handleEvent(Event event) {
        if (event.getTopic().equals(EventConstants.TESTCASE_UPDATED)) {
            try {
                Object object = event.getProperty(EventConstants.EVENT_DATA_PROPERTY_NAME);
                if (object != null && object instanceof Object[]) {
                    String elementId = EntityPartUtil.getTestCaseCompositePartId((String) ((Object[]) object)[0]);
                    if (elementId.equalsIgnoreCase(compositePart.getElementId())) {
                        TestCaseEntity testCase = (TestCaseEntity) ((Object[]) object)[1];
                        changeOriginalTestCase(testCase);
                        updatePart(testCase);
                    }
                }
            } catch (Exception e) {
                LoggerSingleton.logError(e);
            }
        } else if (event.getTopic().equals(EventConstants.ECLIPSE_EDITOR_CLOSED)) {
            try {
                Object object = event.getProperty(EventConstants.EVENT_DATA_PROPERTY_NAME);
                if (object != null && object instanceof GroovyEditor && object.equals(groovyEditor)) {
                    GroovyEditorUtil.clearEditorProblems(groovyEditor);
                    if (!editorLastDirty) {
                        if (partService.savePart(compositePart, false)) {
                            partService.hidePart(compositePart);
                        }
                    } else {
                        partService.hidePart(compositePart, true);
                    }
                }
            } catch (Exception e) {
                LoggerSingleton.logError(e);
            }
        } else if (event.getTopic().equals(EventConstants.EXPLORER_REFRESH_SELECTED_ITEM)) {
            try {
                Object object = event.getProperty(EventConstants.EVENT_DATA_PROPERTY_NAME);
                if (object != null && object instanceof ITreeEntity) {
                    if (object instanceof TestCaseTreeEntity) {
                        TestCaseTreeEntity testCaseTreeEntity = (TestCaseTreeEntity) object;
                        TestCaseEntity testCase = (TestCaseEntity) (testCaseTreeEntity).getObject();
                        if (testCase != null && testCase.getId().equals(getTestCase().getId())) {
                            if (TestCaseController.getInstance().getTestCase(testCase.getId()) != null) {
                                boolean isDirty = dirty.isDirty();
                                changeOriginalTestCase(testCase);
                                childTestCaseVariablesPart.loadVariables();
                                updatePart(testCase);
                                childTestCaseIntegrationPart.loadInput();
                                checkDirty();
                                dirty.setDirty(isDirty);
                            } else {
                                dispose();
                            }
                        }
                    } else if (object instanceof FolderTreeEntity) {
                        FolderEntity folder = (FolderEntity) ((ITreeEntity) object).getObject();
                        if (folder != null
                                && FolderController.getInstance().isFolderAncestorOfEntity(folder, getTestCase())) {
                            if (TestCaseController.getInstance().getTestCase(getTestCase().getId()) == null) {
                                dispose();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LoggerSingleton.logError(e);
            }
        }
    }

    private void dispose() {
        MPartStack mStackPart = (MPartStack) modelService.find(IdConstants.COMPOSER_CONTENT_PARTSTACK_ID, application);
        mStackPart.getChildren().remove(compositePart);
        eventBroker.unsubscribe(childTestCasePart);
        eventBroker.unsubscribe(this);
    }

    @PreDestroy
    public void preDestroy() {
        try {
            if (groovyEditor != null && getChildCompatibilityPart() != null) {
                GroovyEditorUtil.clearEditorProblems(groovyEditor);
            }

            dispose();
        } catch (CoreException e) {
            LoggerSingleton.logError(e);
        }
    }

    private void updatePart(TestCaseEntity testCase) throws Exception {
        String newCompositePartId = EntityPartUtil.getTestCaseCompositePartId(testCase.getId());
        changeOriginalTestCase(testCase);
        if (!newCompositePartId.equals(compositePart.getElementId())) {

            compositePart.setLabel(testCase.getName());
            compositePart.setElementId(newCompositePartId);

            if (compositePart.getChildren().size() == 1 && compositePart.getChildren().get(0) instanceof MPartStack) {
                MPartStack partStack = (MPartStack) compositePart.getChildren().get(0);
                partStack.setElementId(newCompositePartId + IdConstants.TEST_CASE_SUB_PART_STACK_ID_SUFFIX);

                childTestCasePart.getMPart().setElementId(
                        newCompositePartId + IdConstants.TEST_CASE_GENERAL_PART_ID_SUFFIX);
                childTestCaseVariablesPart.getMPart().setElementId(
                        newCompositePartId + IdConstants.TEST_CASE_VARIABLES_PART_ID_SUFFIX);

                partService.hidePart(getChildCompatibilityPart(), true);
                String testCaseEditorId = newCompositePartId + IdConstants.TEST_CASE_EDITOR_PART_ID_SUFFIX;
                MPart editorPart = GroovyEditorUtil.createTestCaseEditorPart(ResourcesPlugin.getWorkspace().getRoot()
                        .getFile(GroovyUtil.getGroovyScriptForTestCase(testCase).getPath()), partStack,
                        testCaseEditorId, partService, CHILD_TEST_CASE_EDITOR_PART_INDEX);
                partService.activate(editorPart);
                initComponent();
                partStack.setSelectedElement(getChildManualPart());
                setScriptContentToManual();
                childTestCaseEditorPart.getEditor().addPropertyListener(getChildPropertyListner());
                checkDirty();
            }
        }
        boolean isAnyChildDirty = isAnyChildDirty();

        // refresh child parts
        childTestCasePart.updateInput();
        childTestCaseVariablesPart.loadVariables();
        childTestCaseIntegrationPart.loadInput();

        checkDirty();
        setDirty(isAnyChildDirty);
    }

    public void setSelectedPart(MPart partToSelect) {
        if (subPartStack.getChildren().contains(partToSelect)) {
            subPartStack.setSelectedElement(partToSelect);
        }
    }

    @Override
    public List<MPart> getChildParts() {
        List<MPart> childrenParts = new ArrayList<MPart>();
        childrenParts.add(getChildManualPart());
        childrenParts.add(getChildCompatibilityPart());
        childrenParts.add(getChildVariablesPart());
        childrenParts.add(getChildIntegrationPart());
        return childrenParts;
    }
}

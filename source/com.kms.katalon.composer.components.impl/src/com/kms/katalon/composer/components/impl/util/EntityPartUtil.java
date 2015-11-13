package com.kms.katalon.composer.components.impl.util;

import org.apache.commons.lang.StringUtils;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

import com.kms.katalon.composer.components.application.ApplicationSingleton;
import com.kms.katalon.composer.components.services.ModelServiceSingleton;
import com.kms.katalon.constants.IdConstants;
import com.kms.katalon.controller.ObjectRepositoryController;
import com.kms.katalon.controller.ReportController;
import com.kms.katalon.controller.TestCaseController;
import com.kms.katalon.controller.TestDataController;
import com.kms.katalon.controller.TestSuiteController;
import com.kms.katalon.entity.IEntity;
import com.kms.katalon.entity.report.ReportEntity;
import com.kms.katalon.entity.repository.WebElementEntity;
import com.kms.katalon.entity.testcase.TestCaseEntity;
import com.kms.katalon.entity.testdata.DataFileEntity;
import com.kms.katalon.entity.testsuite.TestSuiteEntity;

public class EntityPartUtil {
    public static String getTestCaseCompositePartId(String testCasePk) {
        return IdConstants.TEST_CASE_PARENT_COMPOSITE_PART_ID_PREFIX + "(" + testCasePk + ")";
    }

    public static String getTestObjectPartId(String testObjectPk) {
        return IdConstants.TESTOBJECT_CONTENT_PART_ID_PREFIX + "(" + testObjectPk + ")";
    }

    public static String getTestSuiteCompositePartId(String testSuitePk) {
        return IdConstants.TESTSUITE_CONTENT_PART_ID_PREFIX + "(" + testSuitePk + ")";
    }

    public static String getTestDataPartId(String testDataPk) {
        return IdConstants.TESTDATA_CONTENT_PART_ID_PREFIX + "(" + testDataPk + ")";
    }

    public static String getReportPartId(String reportPk) {
        return IdConstants.REPORT_CONTENT_PART_ID_PREFIX + "(" + reportPk + ")";
    }

    public static void closePart(IEntity entity) {

        if (entity == null) {
            return;
        }

        String partId = "";
        String entityId = entity.getId();

        if (entity instanceof TestCaseEntity) {
            partId = getTestCaseCompositePartId(entityId);
        } else if (entity instanceof WebElementEntity) {
            partId = getTestObjectPartId(entityId);
        } else if (entity instanceof DataFileEntity) {
            partId = getTestDataPartId(entityId);
        } else if (entity instanceof TestSuiteEntity) {
            partId = getTestSuiteCompositePartId(entityId);
        } else if (entity instanceof ReportEntity) {
            partId = getReportPartId(entity.getId());
        } else {
            return;
        }

        EModelService modelService = ModelServiceSingleton.getInstance().getModelService();
        MApplication application = ApplicationSingleton.getInstance().getApplication();

        MPartStack mStackPart = (MPartStack) modelService.find(IdConstants.COMPOSER_CONTENT_PARTSTACK_ID, application);
        MPart mPart = (MPart) modelService.find(partId, application);
        if (mPart != null) {
            mStackPart.getChildren().remove(mPart);
        }
    }

    public static IEntity getEntityByPartId(String partElementId) {
        try {
            if (StringUtils.isBlank(partElementId)) {
                return null;
            }

            if (partElementId.startsWith(IdConstants.TEST_CASE_PARENT_COMPOSITE_PART_ID_PREFIX)) {
                String testCaseId = partElementId.substring(
                        IdConstants.TEST_CASE_PARENT_COMPOSITE_PART_ID_PREFIX.length() + 1,
                        partElementId.lastIndexOf(")"));
                return TestCaseController.getInstance().getTestCase(testCaseId);
            } else if (partElementId.startsWith(IdConstants.TESTOBJECT_CONTENT_PART_ID_PREFIX)) {
                String testObjectId = partElementId.substring(
                        IdConstants.TESTOBJECT_CONTENT_PART_ID_PREFIX.length() + 1,
                        partElementId.lastIndexOf(")"));
                return ObjectRepositoryController.getInstance().getWebElement(testObjectId);
            } else if (partElementId.startsWith(IdConstants.TESTDATA_CONTENT_PART_ID_PREFIX)) {
                String testDataId = partElementId.substring(
                        IdConstants.TESTDATA_CONTENT_PART_ID_PREFIX.length() + 1,
                        partElementId.lastIndexOf(")"));
                return TestDataController.getInstance().getTestData(testDataId);
            } else if (partElementId.startsWith(IdConstants.TESTSUITE_CONTENT_PART_ID_PREFIX)) {
                String testSuiteId = partElementId.substring(
                        IdConstants.TESTSUITE_CONTENT_PART_ID_PREFIX.length() + 1,
                        partElementId.lastIndexOf(")"));
                return TestSuiteController.getInstance().getTestSuite(testSuiteId);
            } else if (partElementId.startsWith(IdConstants.REPORT_CONTENT_PART_ID_PREFIX)) {
                String reportId = partElementId.substring(
                        IdConstants.REPORT_CONTENT_PART_ID_PREFIX.length() + 1,
                        partElementId.lastIndexOf(")"));
                return ReportController.getInstance().getReportEntity(reportId);
            }
            return null;
        } catch (Exception ex) {
            return null;
        }
    }
}

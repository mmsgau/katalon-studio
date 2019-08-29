package com.kms.katalon.composer.integration.qtest.wizard.provider;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;

import com.kms.katalon.composer.components.impl.constants.ImageConstants;
import com.kms.katalon.composer.components.impl.providers.CellLayoutInfo;
import com.kms.katalon.composer.components.impl.providers.TypeCheckedStyleCellLabelProvider;
import com.kms.katalon.composer.components.impl.wizard.AbstractWizardPage;
import com.kms.katalon.composer.components.impl.wizard.IWizardPage;
import com.kms.katalon.composer.components.impl.wizard.WizardManager;
import com.kms.katalon.composer.components.util.ColorUtil;
import com.kms.katalon.composer.integration.qtest.wizard.page.QTestWizardPage;

public class WizardTableLabelProvider extends TypeCheckedStyleCellLabelProvider<IWizardPage> {

    private WizardManager fWizardManager;

    private Object currentElement;

    public WizardTableLabelProvider(WizardManager wizardManager) {
        super(0);
        fWizardManager = wizardManager;
    }

    @Override
    protected void paint(Event event, Object element) {
        currentElement = element;
        super.paint(event, element);
    }

    @Override
    protected Color getBackground(Color background, IWizardPage wizardPage) {
        return (getStepNumber(wizardPage) == getCurrentStepNumber()) ? ColorUtil.getSelectedTableItemBackgroundColor()
                : ColorUtil.getWhiteBackgroundColor();
    }

    private int getCurrentStepNumber() {
        return fWizardManager.getWizardPages().indexOf(fWizardManager.getCurrentPage()) + 1;
    }

    private int getStepNumber(IWizardPage wizardPage) {
        return fWizardManager.getWizardPages().indexOf(wizardPage) + 1;
    }

    @Override
    protected Color getForeground(Color foreground, IWizardPage wizardPage) {
        return (getStepNumber(wizardPage) == getCurrentStepNumber()) ? ColorUtil.getTextWhiteColor()
                : ColorUtil.getDefaultTextColor();
    }

    @Override
    protected Class<IWizardPage> getElementType() {
        return IWizardPage.class;
    }

    @Override
    protected Image getImage(IWizardPage wizardPage) {
        int stepNumber = getStepNumber(wizardPage);
        int currentStepNumber = getCurrentStepNumber();

        if (wizardPage instanceof AbstractWizardPage) {
            return (stepNumber < currentStepNumber) ? ImageConstants.IMG_16_CHECKED : ImageConstants.IMG_16_UNCHECKED;
        }
        return null;
    }

    @Override
    protected String getText(IWizardPage wizardPage) {
        return ((QTestWizardPage) wizardPage).getStepIndexAsString() + ". " + wizardPage.getTitle();
    }

    @Override
    public CellLayoutInfo getCellLayoutInfo() {
        CellLayoutInfo layoutInfo = super.getCellLayoutInfo();
        return new CellLayoutInfo() {

            @Override
            public int getSpace() {
                return layoutInfo.getSpace();
            }

            @Override
            public int getRightMargin() {
                return layoutInfo.getRightMargin();
            }

            @Override
            public int getLeftMargin() {
                int extendedSpace = 0;
                if (currentElement instanceof QTestWizardPage) {
                    QTestWizardPage wizardPage = (QTestWizardPage) currentElement;
                    extendedSpace += wizardPage.isChild() ? 15 : 0;
                }
                return layoutInfo.getLeftMargin() + extendedSpace;
            }
        };
    }
}
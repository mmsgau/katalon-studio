package com.kms.katalon.composer.windows.action;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.kms.katalon.composer.mobile.recorder.components.MobileActionButtonSelectionHandler;

public class WindowsActionButtonWrapper {
    private Button button;

    private WindowsAction mobileAction;

    private MobileActionButtonSelectionHandler selectionHandler;

    public WindowsActionButtonWrapper(Composite parent, WindowsAction mobileAction,
            MobileActionButtonSelectionHandler selectionHandler) {
        this.selectionHandler = selectionHandler;
        this.mobileAction = mobileAction;
        initButton(parent);
    }

    protected void initButton(Composite parent) {
        button = new Button(parent, SWT.NONE);
        button.setText(mobileAction.getReadableName());
        button.setToolTipText(mobileAction.getDescription());
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (selectionHandler != null) {
                    selectionHandler.handleButtonClicked(event);
                }
            }
        });
    }

    public WindowsAction getWindowsAction() {
        return mobileAction;
    }

    public void enableButton() {
        button.setEnabled(true);
    }

    public void disableButton() {
        button.setEnabled(false);
    }

    public void setEnabledButton(boolean isEnabled) {
        if (button.isDisposed()) {
            return;
        }
        button.setEnabled(isEnabled);
    }
}

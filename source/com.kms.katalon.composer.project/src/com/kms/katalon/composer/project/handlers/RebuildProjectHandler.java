package com.kms.katalon.composer.project.handlers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.dialogs.MessageDialog;

import com.kms.katalon.composer.components.log.LoggerSingleton;
import com.kms.katalon.composer.project.constants.StringConstants;
import com.kms.katalon.controller.FolderController;
import com.kms.katalon.controller.ProjectController;
import com.kms.katalon.entity.project.ProjectEntity;
import com.kms.katalon.execution.launcher.manager.LauncherManager;
import com.kms.katalon.groovy.util.GroovyUtil;

public class RebuildProjectHandler {

    @CanExecute
    public boolean canExecute() {
        try {
            return (ProjectController.getInstance().getCurrentProject() != null)
                    && !LauncherManager.getInstance().isAnyLauncherRunning();
        } catch (CoreException e) {
            LoggerSingleton.logError(e);
            return false;
        }
    }

    @Execute
    public void execute() {
        try {
            Job job = new Job(StringConstants.HAND_REBUILD_PROJ) {

                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    try {
                        monitor.beginTask(StringConstants.HAND_REBUILDING_PROJ, 10);
                        ProjectEntity projectEntity = ProjectController.getInstance().getCurrentProject();
                        GroovyUtil.initGroovyProjectClassPath(ProjectController.getInstance().getCurrentProject(),
                                FolderController.getInstance().getTestCaseRoot(projectEntity), false,
                                new SubProgressMonitor(monitor, 10));
                        return Status.OK_STATUS;
                    } catch (Exception e) {
                        return Status.CANCEL_STATUS;
                    } finally {
                        monitor.done();
                    }
                }
            };
            job.setUser(true);
            job.schedule();
        } catch (Exception e) {
            MessageDialog.openError(null, StringConstants.ERROR_TITLE,
                    StringConstants.HAND_ERROR_MSG_UNABLE_TO_REBUILD_PROJ);
        }
    }
}
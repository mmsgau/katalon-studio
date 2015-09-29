package com.kms.katalon.execution.addon;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import com.kms.katalon.constants.EventConstants;
import com.kms.katalon.controller.ProjectController;
import com.kms.katalon.entity.project.ProjectEntity;
import com.kms.katalon.execution.handler.EvaluateRunConfigurationContributionsHandler;
import com.kms.katalon.execution.integration.EvaluateReportIntegrationContribution;


public class ExecutionInjectionManagerAddon implements EventHandler {
    @Inject
    private IEventBroker eventBroker;
    
	@PostConstruct
    public void initHandlers(IEclipseContext context) {
        ContextInjectionFactory.make(EvaluateReportIntegrationContribution.class, context);
        ContextInjectionFactory.make(EvaluateRunConfigurationContributionsHandler.class, context);
        eventBroker.subscribe(EventConstants.PROJECT_OPENED, this);
    }

    @Override
    public void handleEvent(Event event) {
        if (event.getTopic().equals(EventConstants.PROJECT_OPENED)) {
            collectCustomExecutionConfigs();
        }
    }

    protected void collectCustomExecutionConfigs() {
        ProjectEntity currentProject = ProjectController.getInstance().getCurrentProject();
        if (currentProject == null) {
            return;
        }
    }
}
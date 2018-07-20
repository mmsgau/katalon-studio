package com.kms.katalon.addons;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;

import com.kms.katalon.composer.handlers.CheckForUpdateOnStartupHandler;


public class ContextServiceRegistryAddon {

    @Inject
    private IEclipseContext context;
    
    @PostConstruct
    public void registerHandlers() {
        ContextInjectionFactory.make(CheckForUpdateOnStartupHandler.class, context);
    }
    
}
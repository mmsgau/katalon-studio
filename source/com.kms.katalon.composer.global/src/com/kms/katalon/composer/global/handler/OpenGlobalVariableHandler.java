package com.kms.katalon.composer.global.handler;

import javax.annotation.PostConstruct;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.osgi.framework.FrameworkUtil;

import com.kms.katalon.composer.components.impl.handler.OpenFileEntityHandler;
import com.kms.katalon.composer.components.impl.util.EntityPartUtil;
import com.kms.katalon.composer.global.part.GlobalVariablePart;
import com.kms.katalon.composer.resources.constants.IImageKeys;
import com.kms.katalon.composer.resources.image.ImageManager;
import com.kms.katalon.entity.global.ExecutionProfileEntity;
import com.kms.katalon.tracking.service.Trackings;

public class OpenGlobalVariableHandler extends OpenFileEntityHandler<ExecutionProfileEntity> {
    private static final String GL_VARIABLE_COLLECTION_BUNDLE_URI = "bundleclass://"
            + FrameworkUtil.getBundle(OpenGlobalVariableHandler.class).getSymbolicName() + "/";

    private static final String GL_VARIABLE_COLLECTION_PART_URI = GL_VARIABLE_COLLECTION_BUNDLE_URI
            + GlobalVariablePart.class.getName();

    @PostConstruct
    @Override
    protected void initialize() {
        super.initialize();
    }
    
    @Override
    protected void execute(ExecutionProfileEntity profileEntity) {
        String partId = getPartId(profileEntity);

        MPart mPart = (MPart) getModelService().find(partId, getApplication());
        boolean alreadyOpened = mPart != null;
        
        super.execute(profileEntity);
        
        if (!alreadyOpened) {
            Trackings.trackOpenObject("profile");
        }
    }

    @Override
    public String getContributionURI() {
        return GL_VARIABLE_COLLECTION_PART_URI;
    }

    @Override
    public String getIconURI() {
        return ImageManager.getImageURLString(IImageKeys.GLOBAL_VARIABLE_16);
    }

    @Override
    public String getPartId(ExecutionProfileEntity executionProfile) {
        return EntityPartUtil.getExecutionProfilePartId(executionProfile.getIdForDisplay());
    }

    @Override
    protected Class<? extends ExecutionProfileEntity> getEntityType() {
        return ExecutionProfileEntity.class;
    }
}
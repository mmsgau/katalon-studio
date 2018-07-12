package com.kms.katalon.execution.configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.kms.katalon.core.constants.StringConstants;
import com.kms.katalon.core.testobject.ResponseObject;
import com.kms.katalon.core.util.internal.JsonUtil;
import com.kms.katalon.execution.exception.ExecutionException;

public class WSVerificationRunConfiguration extends AbstractRunConfiguration {
    
    private String testObjectId;
    
    private ResponseObject responseObject;
    
    public WSVerificationRunConfiguration(String testObjectId, ResponseObject responseObject) {
        this.testObjectId = testObjectId;
        this.responseObject = responseObject;
    }
    
    @Override
    public Map<String, IDriverConnector> getDriverConnectors() {
        return new HashMap<>();
    }

    @Override
    public IRunConfiguration cloneConfig() throws IOException, ExecutionException {
        return new WSVerificationRunConfiguration(testObjectId, responseObject);
    }
    
    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = super.getProperties();
        properties.put(StringConstants.WS_RESPONSE_OBJECT, JsonUtil.toJson(responseObject));
        return properties;
    }
    
    public String getTestObjectId() {
        return testObjectId;
    }
}

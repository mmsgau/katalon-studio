package com.kms.katalon.integration.analytics.providers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.kms.katalon.feature.TestOpsFeatureActivator;
import com.kms.katalon.integration.analytics.entity.AnalyticsFeature;
import com.kms.katalon.integration.analytics.entity.AnalyticsTokenInfo;
import com.kms.katalon.logging.LogUtil;

public class TestOpsFeatureActivatorImpl implements TestOpsFeatureActivator {

    @Override
    public Set<String> getFeatures(String serverUrl, String email, String password, long organizationId, String ksVersion) {
        try {
            AnalyticsTokenInfo token = AnalyticsApiProvider.requestToken(serverUrl, email, password);
            List<AnalyticsFeature> features = AnalyticsApiProvider.getFeatures(serverUrl, token.getAccess_token(), organizationId, ksVersion);
            Set<String> featureKeys = features.stream().map(AnalyticsFeature::getKey).collect(Collectors.toSet());
            return featureKeys;
        } catch (Exception ex) {
            LogUtil.logError(ex);
            return new HashSet<String>();
        }
    }

}
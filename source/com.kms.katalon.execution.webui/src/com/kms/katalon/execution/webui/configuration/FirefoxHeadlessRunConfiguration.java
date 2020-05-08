package com.kms.katalon.execution.webui.configuration;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.kms.katalon.constants.IdConstants;
import com.kms.katalon.core.model.RunningMode;
import com.kms.katalon.core.setting.PropertySettingStoreUtil;
import com.kms.katalon.core.util.ApplicationRunningMode;
import com.kms.katalon.core.webui.driver.WebUIDriverType;
import com.kms.katalon.execution.configuration.IDriverConnector;
import com.kms.katalon.execution.configuration.IRunConfiguration;
import com.kms.katalon.execution.preferences.WebUIConsoleOptionContributor;
import com.kms.katalon.execution.webui.driver.FirefoxDriverConnector;
import com.kms.katalon.execution.webui.driver.FirefoxHeadlessDriverConnector;
import com.kms.katalon.execution.webui.driver.SeleniumWebDriverProvider;
import com.kms.katalon.logging.LogUtil;
import com.kms.katalon.preferences.internal.PreferenceStoreManager;
import com.kms.katalon.preferences.internal.ScopedPreferenceStore;

public class FirefoxHeadlessRunConfiguration extends WebUiRunConfiguration {

    private String driverPath;

    public FirefoxHeadlessRunConfiguration(String projectDir) throws IOException {
        super(projectDir, new FirefoxHeadlessDriverConnector(projectDir + File.separator
                + PropertySettingStoreUtil.INTERNAL_SETTING_ROOT_FOLDER_NAME));
        this.driverPath = buildGeckoDriverPath();
    }

    @Override
    public IRunConfiguration cloneConfig() throws IOException {
        return new FirefoxHeadlessRunConfiguration(projectDir);
    }
    
    private String buildGeckoDriverPath() {
        String driverPath = "";
        try {
            driverPath = SeleniumWebDriverProvider.getGeckoDriverPath();
        } catch (IOException ex) {
            LogUtil.printAndLogError(ex);
        }
        ScopedPreferenceStore store = PreferenceStoreManager
                .getPreferenceStore(IdConstants.KATALON_WEB_UI_BUNDLE_ID);
        boolean isUpdateDriverAllowed = store.getBoolean(WebUIConsoleOptionContributor.WEB_UI_AUTO_UPDATE_DRIVERS);
        if (isUpdateDriverAllowed && ApplicationRunningMode.get() == RunningMode.CONSOLE) {
            WebDriverManagerRunConfiguration webDriverManagerRunConfiguration = new WebDriverManagerRunConfiguration();
            try {
                webDriverManagerRunConfiguration.downloadDriver(WebUIDriverType.FIREFOX_HEADLESS_DRIVER,
                        SeleniumWebDriverProvider.getTempDriverDirectory());
                String tempDriverPath = SeleniumWebDriverProvider.getTempGeckoDriverPath();
                if (new File(tempDriverPath).exists()) {
                    driverPath = tempDriverPath;
                }
            } catch (InterruptedException | IOException e) {
                LogUtil.logError(e);
            }
            LogUtil.printOutputLine("gecko driver is located at: " + driverPath);
        } else {
            LogUtil.printOutputLine(String.format(
                    "gecko driver is located at default location: %s. In case your browser is updated to a newer version,"
                            + " please use this command to update gecko driver: --config -%s=true",
                    driverPath, WebUIConsoleOptionContributor.WEB_UI_AUTO_UPDATE_DRIVERS));
        }        
        return driverPath;
    }

    @Override
    public Map<String, IDriverConnector> getDriverConnectors() {
        Map<String, IDriverConnector> driverConnectors = super.getDriverConnectors();
        for (java.util.Map.Entry<String, IDriverConnector> entry : driverConnectors.entrySet()) {
            if (entry.getValue() instanceof FirefoxHeadlessDriverConnector) {
                FirefoxHeadlessDriverConnector driverConnector = (FirefoxHeadlessDriverConnector) entry.getValue();
                driverConnector.setGeckoDriverPath(driverPath);
            }
        }
        return driverConnectors;
    }
}
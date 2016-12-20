package com.kms.katalon.composer.integration.jira.report.dialog;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.kms.katalon.composer.components.log.LoggerSingleton;
import com.kms.katalon.composer.integration.jira.JiraUIComponent;
import com.kms.katalon.composer.integration.jira.constant.ComposerJiraIntegrationMessageConstant;
import com.kms.katalon.core.logging.model.TestCaseLogRecord;
import com.kms.katalon.integration.jira.issue.IssueHTMLLinkProvider;

public class JiraIssueBrowserDialog extends Dialog implements JiraUIComponent {
    private Text txtBrowserUrl;

    protected Browser browser;

    private String issueKey;

    private IssueHTMLLinkProvider htmlLinkProvider;

    public JiraIssueBrowserDialog(Shell parentShell, TestCaseLogRecord logRecord,
            IssueHTMLLinkProvider htmlLinkProvider) throws URISyntaxException, IOException {
        super(parentShell);
        this.htmlLinkProvider = htmlLinkProvider;
    }

    @Override
    protected int getShellStyle() {
        return (SWT.CLOSE | SWT.ON_TOP | SWT.RESIZE);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = (GridLayout) composite.getLayout();
        gridLayout.numColumns = 2;
        composite.setBackgroundMode(SWT.INHERIT_FORCE);
        txtBrowserUrl = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
        txtBrowserUrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        new Label(composite, SWT.NONE);

        browser = new Browser(composite, SWT.NONE);
        browser.setJavascriptEnabled(true);
        browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        new Label(composite, SWT.NONE);

        Label lblNotification = new Label(composite, SWT.WRAP);
        lblNotification.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        lblNotification.setText(ComposerJiraIntegrationMessageConstant.DIA_ISSUE_BROWSE_NOTIFICATION);

        registerControlModifyListeners();
        setInput();
        return composite;
    }

    private void setInput() {
        try {
            browser.setUrl(htmlLinkProvider.getLoginHTMLLink());
        } catch (IOException | URISyntaxException e) {
            LoggerSingleton.logError(e);
        }
    }

    private void registerControlModifyListeners() {
        browser.addLocationListener(new LocationListener() {
            private boolean ready;

            private boolean dashBoardSet;

            private boolean loggedIn;

            @Override
            public void changing(LocationEvent event) {
                txtBrowserUrl.setText(event.location);
            }

            @Override
            public void changed(LocationEvent event) {
                try {
                    String location = browser.getUrl();
                    if (!ready) {
                        if (!loggedIn && isLoginPage()) {
                            loggedIn = true;
                            login();
                            return;
                        }

                        if (location.startsWith(htmlLinkProvider.getIssueUrlPrefix())) {
                            ready = true;
                            trigger();
                            return;
                        }
                        if (!dashBoardSet && !event.location.equals(htmlLinkProvider.getDashboardHTMLLink())) {
                            browser.setUrl(htmlLinkProvider.getDashboardHTMLLink());
                            browser.setUrl(htmlLinkProvider.getHTMLLink());
                            dashBoardSet = true;
                            return;
                        }
                        return;
                    }
                    getNewIssueKey(location);
                } catch (IOException | URISyntaxException e) {
                    LoggerSingleton.logError(e);
                }
            }

            private void login() throws IOException, URISyntaxException {
                if (isLoginDashboard()) {
                    loginForServer();
                } else {
                    loginForCloud();
                }
            }

            private void getNewIssueKey(String location) throws IOException {
                String createdIssueURLPrefix = getHTMLIssueURLPrefix();
                if (location.startsWith(createdIssueURLPrefix)) {
                    browser.removeLocationListener(this);
                    issueKey = location.substring(createdIssueURLPrefix.length() + 1);
                    close();
                }
            }
        });
    }

    private boolean isLoginDashboard() throws IOException, URISyntaxException {
        return htmlLinkProvider.getLoginHTMLLink().equals(browser.getUrl());
    }

    private boolean isLoginPage() throws IOException, URISyntaxException {
        return htmlLinkProvider.getLoginHTMLLink().startsWith(browser.getUrl());
    }

    protected void loginForCloud() {
        try {
            StringBuilder js = new StringBuilder()
                    .append("document.getElementById(\"username\").value = \""
                            + StringEscapeUtils.escapeEcmaScript(getCredential().getUsername()) + "\";\n")
                    .append("document.getElementById(\"password\").value = \""
                            + StringEscapeUtils.escapeEcmaScript(getCredential().getPassword()) + "\";\n")
                    .append("document.getElementById(\"remember-me\").checked = true;\n")
                    .append("document.getElementById(\"login\").click();\n");
            browser.execute(waitAndExec("remember-me", js.toString()));
        } catch (IOException e) {
            LoggerSingleton.logError(e);
        }
    }

    protected void loginForServer() {
        try {
            StringBuilder js = new StringBuilder();
            js.append("document.getElementById(\"login-form-username\").value = \""
                    + StringEscapeUtils.escapeEcmaScript(getCredential().getUsername()) + "\";\n")
                    .append("document.getElementById(\"login-form-password\").value = \""
                            + StringEscapeUtils.escapeEcmaScript(getCredential().getPassword()) + "\";\n")
                    .append("document.getElementById(\"login-form-remember-me\").checked = true;\n")
                    .append("document.getElementById(\"login-form-submit\").click();\n");
            browser.execute(waitAndExec("login-form-submit", js.toString()));
        } catch (IOException e) {
            LoggerSingleton.logError(e);
        }
    }

    protected String waitAndExec(String element, String js) {
        return "function waitUntilExist() {" + "if (document.getElementById(\"" + element + "\") === null) {"
                + "setTimeout(waitUntilExist, 1000);" + "} else {" + js + "}" + "};" + "waitUntilExist();";
    }

    protected void trigger() {
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        return null;
    }

    @Override
    protected Point getInitialSize() {
        return new Point(1200, 800);
    }

    @Override
    public boolean close() {
        browser.close();
        return super.close();
    }

    public String getIssueKey() {
        return issueKey;
    }
}
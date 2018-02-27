package com.kms.katalon.composer.webservice.editor;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

import com.kms.katalon.composer.components.log.LoggerSingleton;
import com.kms.katalon.core.util.internal.JsonUtil;
import com.kms.katalon.entity.webservice.TextBodyContent;

public class TextBodyEditor extends HttpBodyEditor {

    private enum TextContentType {
        TEXT("Text", "text/plain"),
        JSON("JSON", "application/json"),
        XML("XML", "application/xml"),
        HTML("HTML", "text/html"),
        JAVASCRIPT("JavaScript", "application/javascript");

        private String text;

        private String contentType;

        private TextContentType(String text, String contentType) {
            this.text = text;
            this.contentType = contentType;
        }

        public String getText() {
            return text;
        }

        public String getContentType() {
            return contentType;
        }

        public static String[] getTextValues() {
            return Arrays.asList(values()).stream().map(t -> t.getText()).toArray(String[]::new);
        }

        public static TextContentType evaluateContentType(String contentType) {
            switch (contentType) {
                case "application/json":
                case "application/ld+json":
                    return TextContentType.JSON;
                case "application/javascript":
                case "application/ecmascript":
                    return TextContentType.JAVASCRIPT;
                case "application/xml":
                case "application/atom+xml":
                case "application/soap+xml":
                    return TextContentType.XML;
                case "text/html":
                case "application/xhtml+xml":
                    return TextContentType.HTML;
                default:
                    return TextContentType.TEXT;
            }
        }
    }

    private TextBodyContent textBodyContent;

    private Browser browser;

    // A collection of mirror modes for some text types
    private static final Map<String, String> TEXT_MODE_COLLECTION;

    // List of TextContentType by name
    private static final String[] TEXT_MODE_NAMES;

    private Map<String, Button> TEXT_MODE_SELECTION_BUTTONS = new HashMap<>();

    static {
        TEXT_MODE_COLLECTION = new HashMap<>();
        TEXT_MODE_COLLECTION.put(TextContentType.TEXT.getText(), "text/plain");
        TEXT_MODE_COLLECTION.put(TextContentType.JSON.getText(), "application/ld+json");
        TEXT_MODE_COLLECTION.put(TextContentType.XML.getText(), "application/xml");
        TEXT_MODE_COLLECTION.put(TextContentType.HTML.getText(), "text/html");
        TEXT_MODE_COLLECTION.put(TextContentType.JAVASCRIPT.getText(), "application/javascript");

        TEXT_MODE_NAMES = TextContentType.getTextValues();
    }

    public TextBodyEditor(Composite parent, int style) {
        super(parent, style);

        GridLayout gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        this.setLayout(gridLayout);

        browser = new Browser(this, SWT.NONE);
        browser.setLayoutData(new GridData(GridData.FILL_BOTH));
        browser.setJavascriptEnabled(true);

        File templateFile = new File("/Users/duyluong/Documents/Work/Katalon/code/katalon/source"
                + "/com.kms.katalon.composer.webservice/resources/template/editor/codemirror/template.html");
        try {
            browser.setUrl(templateFile.toURI().toURL().toString());
        } catch (IOException e) {
            LoggerSingleton.logError(e);
        }

        Composite tbBodyType = new Composite(this, SWT.NONE);
        tbBodyType.setLayout(new GridLayout(TEXT_MODE_NAMES.length, false));

        Arrays.asList(TextContentType.values()).forEach(textContentType -> {
            Button btnTextMode = new Button(tbBodyType, SWT.RADIO);
            btnTextMode.setText(textContentType.getText());
            TEXT_MODE_SELECTION_BUTTONS.put(textContentType.getText(), btnTextMode);

            btnTextMode.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    changeMode(textContentType.getText());
                    
                    textBodyContent.setContentType(textContentType.getContentType());

                    TextBodyEditor.this.setContentTypeUpdated(true);

                    TextBodyEditor.this.notifyListeners(SWT.Modify, new Event());
                }
            });
        });

        browser.addProgressListener(new ProgressListener() {

            @Override
            public void completed(ProgressEvent event) {
                setText(textBodyContent.getText());

                TextContentType preferedContentType = TextContentType
                        .evaluateContentType(textBodyContent.getContentType());

                Button selectionButton = TEXT_MODE_SELECTION_BUTTONS.get(preferedContentType.getText());
                selectionButton.setSelection(true);

                changeMode(preferedContentType.getText());

                handleControlModifyListener();
            }

            @Override
            public void changed(ProgressEvent event) {
            }
        });
    }

    private void handleControlModifyListener() {
        new BrowserFunction(browser, "handleEditorChanged") {
            @Override
            public Object function(Object[] objects) {
                TextBodyEditor.this.notifyListeners(SWT.Modify, new Event());
                return null;
            }
        };
    }

    private void changeMode(String text) {
        String textType = TEXT_MODE_COLLECTION.keySet()
                .stream()
                .filter(key -> text.toLowerCase().startsWith(key.toLowerCase()))
                .findFirst()
                .orElse(TextContentType.TEXT.getText());

        String mode = TEXT_MODE_COLLECTION.get(textType);
        browser.evaluate(MessageFormat.format("changeMode(editor, \"{0}\");", mode));
    }

    private void setText(String text) {
        browser.evaluate(String.format("editor.setValue(\"%s\");", StringEscapeUtils.escapeEcmaScript(text)));
    }

    @SuppressWarnings("unused")
    private void setEditable(boolean editable) {
        browser.evaluate(MessageFormat.format("editor.setOption(\"{0}\", {1});", "readOnly", !editable));
    }

    @Override
    public String getContentType() {
        return textBodyContent.getContentType();
    }

    @Override
    public String getContentData() {
        textBodyContent.setText((String) browser.evaluate("return editor.getValue();"));
        return JsonUtil.toJson(textBodyContent);
    }

    @Override
    public void setInput(String httpBodyContent) {
        if (StringUtils.isEmpty(httpBodyContent)) {
            textBodyContent = new TextBodyContent();
        } else {
            textBodyContent = JsonUtil.fromJson(httpBodyContent, TextBodyContent.class);
        }
    }
}

package com.kms.katalon.composer.components.impl.providers;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

public abstract class HyperLinkColumnLabelProvider<T> extends MouseCursorColumnLabelProvider<T> {

    private int charWidth;

    public HyperLinkColumnLabelProvider(int columnIndex) {
        super(columnIndex);
    }

    @Override
    protected void paint(Event event, Object element) {
        super.paint(event, element);
        charWidth = Math.max(1, event.gc.getFontMetrics().getAverageCharWidth());
    }
    
    @Override
    protected boolean shouldShowCursor(ViewerCell cell, Point currentMouseLocation) {
        Rectangle rect = cell.getTextBounds();
        rect.width = cell.getText().length() * charWidth;
        Rectangle cellBounds = cell.getBounds();
        if (!cellBounds.contains(new Point(rect.x, rect.y))) {
            rect.x = cellBounds.x;
            rect.y = cellBounds.y;
        }
        return rect.contains(currentMouseLocation);
    }

    @Override
    public void update(ViewerCell cell) {
        super.update(cell);

        cell.setStyleRanges(new StyleRange[] { getHyperLinkStyleRange(cell) });
    }

    private StyleRange getHyperLinkStyleRange(ViewerCell cell) {
        StyleRange hyperLinkStyle = new StyleRange();
        hyperLinkStyle.foreground = JFaceColors.getHyperlinkText(Display.getDefault());
        hyperLinkStyle.underline = true;
        hyperLinkStyle.start = 0;
        hyperLinkStyle.length = cell.getText().length();
        return hyperLinkStyle;
    }

    protected boolean isPlacedMouseHover(ViewerCell cell) {
        return super.isPlacedMouseHover(cell) && StringUtils.isNotEmpty(cell.getText());
    }
}
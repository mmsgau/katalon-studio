package com.kms.katalon.composer.report.provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Image;

import com.kms.katalon.composer.components.util.ColorUtil;
import com.kms.katalon.composer.components.util.DateUtil;
import com.kms.katalon.composer.report.constants.ImageConstants;
import com.kms.katalon.composer.report.parts.ReportPart;
import com.kms.katalon.core.logging.model.ILogRecord;
import com.kms.katalon.core.logging.model.TestCaseLogRecord;

public class ReportPartTestCaseLabelProvider extends StyledCellLabelProvider {
	
	private static final int CLMN_TEST_CASE_ORDER = 0;
	private static final int CLMN_TEST_CASE_NAME = 1;
	private static final int CLMN_TEST_CASE_INTEGRATION = 2;
	
	private ReportPart reportPart;
	
	public ReportPartTestCaseLabelProvider(ReportPart reportExpertPart) {
		reportPart = reportExpertPart;
	}

	public Image getImage(Object element, int columnIndex) {
		if (element == null || !(element instanceof ILogRecord)) return null;
		switch (columnIndex) {
		case CLMN_TEST_CASE_NAME:
			ILogRecord logRecord = (ILogRecord) element;
			switch (logRecord.getStatus().getStatusValue()) {
			case ERROR:
				return ImageConstants.IMG_16_ERROR;
			case FAILED:
				return ImageConstants.IMG_16_FAILED;
			case NOT_RUN:
				return ImageConstants.IMG_16_FAILED;
			case PASSED:
				return ImageConstants.IMG_16_PASSED;
			}
			break;
		case CLMN_TEST_CASE_INTEGRATION:
			String integratedProduct = reportPart.getIntegratedProductName();
			if (integratedProduct == null || integratedProduct.isEmpty()) return null;
			return reportPart.getIntegratingCompositeMap().get(integratedProduct).getImage((TestCaseLogRecord) element);
		}
		return null;
	}
	
	private int getOrder(ILogRecord[] testCaseLogRecords, ILogRecord testCaseLogRecord) {
		for (int index = 0; index < testCaseLogRecords.length; index++) {
			if (testCaseLogRecord.equals(testCaseLogRecords[index])) return index + 1;
		}
		return 0;
	}
	
	private StyledString getText(ILogRecord logRecord, int columnIndex) {
		StyledString styledString = new StyledString();
		switch (columnIndex) {
		case CLMN_TEST_CASE_ORDER:			
			styledString.append(Integer.toString(getOrder((ILogRecord[]) getViewer().getInput(), logRecord)));
			break;
		case CLMN_TEST_CASE_NAME:
			String testCaseId = logRecord.getName();
			String testCaseName = testCaseId.substring(testCaseId.lastIndexOf("/") + 1, testCaseId.length());
			styledString.append(testCaseName);			
			
			String eslapsedTime = DateUtil.getElapsedTime(logRecord.getStartTime(), logRecord.getEndTime());
			styledString.append(" (" + eslapsedTime + ")", StyledString.COUNTER_STYLER);
			break;
		case CLMN_TEST_CASE_INTEGRATION:
			break;
		}
		return styledString;
	}

	@Override
	public void update(ViewerCell cell) {		
		if (cell.getElement() != null) {
			cell.setImage(getImage(cell.getElement(), cell.getColumnIndex()));
			
			List<StyleRange> range = new ArrayList<>();
			StyledString styledString = getText((ILogRecord) cell.getElement(), cell.getColumnIndex());			
			cell.setText(styledString.toString());
			range.addAll(Arrays.asList(styledString.getStyleRanges()));
			
			if (cell.getColumnIndex() == CLMN_TEST_CASE_NAME) {
				ReportTestCaseTableViewer tableViewer = (ReportTestCaseTableViewer) getViewer();
				String searchedString = tableViewer.getSearchedString().toLowerCase().trim();
				
				if (!searchedString.isEmpty())  {				
					Matcher m = Pattern.compile(Pattern.quote(searchedString)).matcher(cell.getText().toLowerCase());
					while (m.find()) {
						StyleRange myStyledRange = new StyleRange(m.start(), searchedString.length(), null,
								ColorUtil.getHighlightBackgroundColor());
						range.add(myStyledRange);
					}				
				}
			}
			cell.setStyleRanges(range.toArray(new StyleRange[0]));
			
		}
		super.update(cell);
	}
	
	@Override
	public String getToolTipText(Object element) {		
		if (element == null) return null;
		return getText((ILogRecord) element, CLMN_TEST_CASE_NAME).getString();
	}
	
}
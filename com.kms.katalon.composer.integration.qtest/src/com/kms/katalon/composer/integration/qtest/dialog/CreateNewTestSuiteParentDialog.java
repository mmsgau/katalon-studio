package com.kms.katalon.composer.integration.qtest.dialog;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import com.kms.katalon.composer.components.dialogs.MultiStatusErrorDialog;
import com.kms.katalon.composer.integration.qtest.dialog.provider.QTestSuiteParentTreeContentProvider;
import com.kms.katalon.composer.integration.qtest.dialog.provider.QTestSuiteParentTreeLabelProvider;
import com.kms.katalon.controller.ProjectController;
import com.kms.katalon.integration.qtest.QTestIntegrationTestSuiteManager;
import com.kms.katalon.integration.qtest.entity.QTestProject;
import com.kms.katalon.integration.qtest.entity.QTestSuiteParent;

public class CreateNewTestSuiteParentDialog extends Dialog {
	
	private TreeViewer treeViewer;
	private Label lblStatus;
	private Composite container;

	private boolean exit;
	
	private QTestProject qTestProject;
	private List<String> parentIds;
	private QTestSuiteParent newTestSuiteParent;

	public CreateNewTestSuiteParentDialog(Shell parentShell, List<String> parentIds, QTestProject qTestProject) {
		super(parentShell);
		this.parentIds = parentIds;
		this.qTestProject = qTestProject;
		exit = false;
	}

	protected Control createDialogArea(Composite parent) {
		container = (Composite) super.createDialogArea(parent);

		Label lblTitle = new Label(container, SWT.NONE);
		lblTitle.setText("Choose parent for test suite.");
		
		lblStatus = new Label(container, SWT.NONE);
		lblStatus.setText("Please wait...");

		treeViewer = new TreeViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
		Tree tree = treeViewer.getTree();
		tree.setHeaderVisible(true);
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		TreeViewerColumn treeViewerColumnName = new TreeViewerColumn(treeViewer, SWT.NONE);
		TreeColumn trclmnName = treeViewerColumnName.getColumn();
		trclmnName.setWidth(200);
		trclmnName.setText("Name");

		TreeViewerColumn treeViewerColumnType = new TreeViewerColumn(treeViewer, SWT.NONE);
		TreeColumn trclmnType = treeViewerColumnType.getColumn();
		trclmnType.setWidth(100);
		trclmnType.setText("Type");

		TreeViewerColumn treeViewerColumnIsUsed = new TreeViewerColumn(treeViewer, SWT.NONE);
		TreeColumn trclmnIsUsed = treeViewerColumnIsUsed.getColumn();
		trclmnIsUsed.setWidth(100);
		trclmnIsUsed.setText("In Use");

		treeViewer.setContentProvider(new QTestSuiteParentTreeContentProvider());
		treeViewer.setLabelProvider(new QTestSuiteParentTreeLabelProvider(parentIds));

		return container;
	}

	@Override
	public void create() {
		super.create();

		loadInput();
		addSelectionListener();
	}

	private void addSelectionListener() {
		treeViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				validate();
			}
		});

		getShell().addListener(SWT.Activate, new Listener() {

			public void handleEvent(Event event) {
				if (exit) {
					close();
				}
			}
		});
	}

	private void validate() {
		StructuredSelection selection = (StructuredSelection) treeViewer.getSelection();
		if (selection == null || selection.getFirstElement() == null) {
			getButton(OK).setEnabled(false);
			return;
		}

		QTestSuiteParent selectedParent = (QTestSuiteParent) selection.getFirstElement();
		if (parentIds.contains(Long.toString(selectedParent.getId()))) {
			getButton(OK).setEnabled(false);
		} else {
			getButton(OK).setEnabled(true);
		}
	}

	private void loadInput() {
		Display display = treeViewer.getControl().getDisplay();

		getButton(OK).setEnabled(false);
		getButton(CANCEL).setEnabled(false);
		
		display.asyncExec(new Runnable() {

			@Override
			public void run() {
				try {					
					String projectDir = ProjectController.getInstance().getCurrentProject().getFolderLocation();
					QTestSuiteParent releaseRoot = QTestIntegrationTestSuiteManager
							.getTestSuiteIdRootOnQTest(projectDir, qTestProject);					
					lblStatus.dispose();
					container.layout();
					treeViewer.setInput(new QTestSuiteParent[] { releaseRoot });
					treeViewer.expandAll();
					
					getButton(OK).setEnabled(true);
					getButton(CANCEL).setEnabled(true);
					
					validate();					
				} catch (Exception e) {
					getShell().setVisible(false);
					MultiStatusErrorDialog.showErrorDialog(e, "Unable to load test suite's parent from qTest server.",
							e.getClass().getSimpleName());
					
					exit = true;
				}
			}
		});

	}

	protected void okPressed() {
		StructuredSelection selection = (StructuredSelection) treeViewer.getSelection();
		if (selection == null || selection.getFirstElement() == null)
			return;
		newTestSuiteParent = (QTestSuiteParent) selection.getFirstElement();

		super.okPressed();
	}

	@Override
	protected Point getInitialSize() {
		return new Point(500, 500);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Create Test Suite's parent");
	}

	public QTestSuiteParent getNewTestSuiteParent() {
		return newTestSuiteParent;
	}

}
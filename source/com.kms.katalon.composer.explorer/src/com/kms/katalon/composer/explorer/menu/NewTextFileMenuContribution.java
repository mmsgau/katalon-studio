package com.kms.katalon.composer.explorer.menu;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;

import com.kms.katalon.composer.components.impl.tree.FolderTreeEntity;
import com.kms.katalon.composer.components.menu.MenuFactory;
import com.kms.katalon.composer.components.tree.ITreeEntity;
import com.kms.katalon.constants.IdConstants;
import com.kms.katalon.constants.helper.ConstantsHelper;
import com.kms.katalon.entity.folder.FolderEntity.FolderType;

@SuppressWarnings("restriction")
public class NewTextFileMenuContribution {

    private static final String NEW_TEXT_FILE_COMMAND_ID = "com.kms.katalon.composer.explorer.command.newTextFile";

    @Inject
    private ECommandService commandService;

    @Inject
    private ESelectionService selectionService;

    @AboutToShow
    public void aboutToShow(List<MMenuElement> menuItems) {
        try {
            FolderTreeEntity parentTreeFolder = getParentTreeFolder();
            if (parentTreeFolder != null && parentTreeFolder.getObject().getFolderType() != FolderType.USER) {
                return;
            }
    
            MHandledMenuItem newTextFileMenuItem = MenuFactory.createPopupMenuItem(
                    commandService.createCommand(NEW_TEXT_FILE_COMMAND_ID, null), "File",
                    ConstantsHelper.getApplicationURI());
            if (newTextFileMenuItem != null) {
                menuItems.add(newTextFileMenuItem);
            }
        } catch (Exception e) {
            return;
        }
    }

    private FolderTreeEntity getParentTreeFolder() {
        try {
            Object[] selectedObjects = (Object[]) selectionService.getSelection(IdConstants.EXPLORER_PART_ID);
            if (selectedObjects == null || selectedObjects.length != 1
                    || !(selectedObjects[0] instanceof ITreeEntity)) {
                return null;
            }

            if (selectedObjects[0] instanceof FolderTreeEntity) {
                FolderTreeEntity parentFolder = (FolderTreeEntity) selectedObjects[0];
                return parentFolder;
            } else {
                ITreeEntity treeEntity = (ITreeEntity) selectedObjects[0];
                ITreeEntity parent = treeEntity.getParent();
                if (!(parent instanceof FolderTreeEntity)) {
                    return null;
                }
                FolderTreeEntity parentFolder = (FolderTreeEntity) parent;
                return parentFolder;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
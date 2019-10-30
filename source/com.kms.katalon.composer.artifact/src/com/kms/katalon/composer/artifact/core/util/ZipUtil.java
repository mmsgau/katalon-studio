package com.kms.katalon.composer.artifact.core.util;

import java.io.File;

import com.kms.katalon.composer.artifact.core.FileCompressionException;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

public class ZipUtil {

    public static ZipFile getZipFile(File zipFile, File folderToZip) throws FileCompressionException {
        if (zipFile.exists()) {
            zipFile.delete();
        }
        try {
            ZipFile returnedZipFile = new ZipFile(zipFile);

            ZipParameters parameters = new ZipParameters();

            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);

            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

            returnedZipFile.addFolder(folderToZip.getAbsolutePath(), parameters);

            return returnedZipFile;
        } catch (ZipException e) {
            throw new FileCompressionException("Cannot zip folder: " + folderToZip.getAbsolutePath(), e);
        }
    }
    
    public static void extractAll(File file, File destinationFolder) throws FileCompressionException {
        try {
            ZipFile zipFile = new ZipFile(file.getAbsolutePath());
            
            zipFile.extractAll(destinationFolder.getAbsolutePath());
        } catch (ZipException e) {
            throw new FileCompressionException("Cannot extract file: " + file.getAbsolutePath(), e);
        }
    }
}

package com.sprd.logmanager.utils;

import java.io.File;
import java.util.zip.ZipOutputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.io.FileInputStream;

/**
 * Created by chrison.chai on 2017/2/4.
 */
public class ZipUtil {

    public static void zipFolder(String srcFilePath, String zipFilePath) throws Exception {
        ZipOutputStream outZip = new ZipOutputStream(new FileOutputStream(zipFilePath));

        File file = new File(srcFilePath);
        zipFiles(file.getParent() + File.separator, file.getPath().toString(), outZip);
        outZip.finish();
        outZip.close();
    }

    public static void zipFolder(String[] srcFilePaths, String zipFilePath) throws Exception {
        ZipOutputStream outZip = new ZipOutputStream(new FileOutputStream(zipFilePath));

        for(String src : srcFilePaths) {
            File file = new File(src);
            if(file.exists() && file.list() != null && file.list().length > 0) {
                zipFiles(file.getParent() + File.separator, file.getPath().toString(), outZip);
            }
        }
        outZip.finish();
        outZip.close();
    }

    private static void zipFiles(String folderPath, String filePath, ZipOutputStream zipOut)
            throws Exception {
        if (zipOut == null) {
            return;
        }
        File file = new File(filePath);

        if (file.isFile()) {
            ZipEntry zipEntry = new ZipEntry(filePath);
            FileInputStream inputStream = new FileInputStream(file);
            zipOut.putNextEntry(zipEntry);
            int len;
            byte[] buffer = new byte[100000];

            while ((len = inputStream.read(buffer)) != -1) {
                zipOut.write(buffer, 0, len);
            }
            inputStream.close();
            zipOut.closeEntry();
        } else {
            String fileList[] = file.list();
            if (fileList == null) return;
            if (fileList.length <= 0) {
                ZipEntry zipEntry = new ZipEntry(filePath + File.separator);
                zipOut.putNextEntry(zipEntry);
                zipOut.closeEntry();
            }
            for (int i = 0; i < fileList.length; i++) {
                zipFiles(folderPath, filePath + File.separator + fileList[i], zipOut);
            }
        }
    }
}

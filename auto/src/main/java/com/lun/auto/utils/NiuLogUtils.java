package com.lun.auto.utils;

import static com.lun.auto.utils.NiuFilesUtils.write;

import android.annotation.SuppressLint;
import android.content.Context;

import com.elvishew.xlog.LogConfiguration;
import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.flattener.PatternFlattener;
import com.elvishew.xlog.printer.AndroidPrinter;
import com.elvishew.xlog.printer.Printer;
import com.elvishew.xlog.printer.file.FilePrinter;
import com.elvishew.xlog.printer.file.backup.NeverBackupStrategy;
import com.elvishew.xlog.printer.file.clean.FileLastModifiedCleanStrategy;
import com.elvishew.xlog.printer.file.naming.DateFileNameGenerator;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

public class NiuLogUtils {
    @SuppressLint("SdCardPath")
    public static void initLog(Context context) {
        // 先判断有无写入文件权限
        if (XXPermissions.isGranted(context, Permission.MANAGE_EXTERNAL_STORAGE)) {
            write("/sdcard/autoScript/log/log.txt","");
        }
        LogConfiguration config = new LogConfiguration.Builder()
                .logLevel(LogLevel.ALL)
                .tag("autoScript")
                .build();
        PatternFlattener patternFlattener = new PatternFlattener("{d yyyy/MM/dd HH:mm:ss} {l}/{t}: {m}");
        Printer androidPrinter = new AndroidPrinter(true);
        Printer filePrinter = new FilePrinter
                .Builder("/sdcard/autoScript/log/")
                .fileNameGenerator(new DateFileNameGenerator() {
                    @Override
                    public String generateFileName(int logLevel, long timestamp) {
//                        return super.generateFileName(logLevel, timestamp) + "log.txt";
                        return "log.txt";
                    }
                })
                .backupStrategy(new NeverBackupStrategy())
                .cleanStrategy(new FileLastModifiedCleanStrategy(7))
                .flattener(patternFlattener)
                .build();
        XLog.init(config, androidPrinter, filePrinter);
    }
}
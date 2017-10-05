/*
 * Copyright (c) 2017. Roberto Sales @ rsalesc
 *
 * This software is provided 'as-is', without any express or implied
 * warranty. In no event will the authors be held liable for any damages
 * arising from the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 *    1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software. If you use this software
 *    in a product, an acknowledgment in the product documentation would be
 *    appreciated but is not required.
 *
 *    2. Altered source versions must be plainly marked as such, and must not be
 *    misrepresented as being the original software.
 *
 *    3. This notice may not be removed or altered from any source
 *    distribution.
 */

package rsalesc.mega.learning.recording;

import rsalesc.runner.FileUtils;
import rsalesc.runner.SerializeHelper;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Predicate;

/**
 * Created by Roberto Sales on 01/10/17.
 */
public class DuelRecordFS {
    private static final String SEPARATOR = "__";

    private final String directory;
    private final String myself;
    private final String prefix;

    public DuelRecordFS(String directory, String myself, String prefix) {
        this.directory = directory;
        this.myself = myself;
        this.prefix = prefix;
    }

    public DuelRecordFS(String directory, String myself) {
        this(directory, myself, "");
    }

    public File getFile(DuelRecord record) {
        return getFile(record.getEnemyName());
    }

    public String getMyself() {
        return myself;
    }

    public String getFilePath(DuelRecord record) {
        return getFile(record).getPath();
    }

    public String getAbsoluteFilePath(DuelRecord record) {
        return getFile(record).getAbsolutePath();
    }

    private String getPrefixS() {
        if(prefix.isEmpty())
            return "";
        return prefix + "_";
    }

    public File getFile(String name) {
        return new File(directory, getPrefixS()
                + (myself.replace("*", "_dev") + SEPARATOR + name).replace(" ", "_")
                + ".zip");
    }

    public String getFilePath(String name) {
        return getFile(name).getPath();
    }

    public String getAbsoluteFilePath(String name) {
        return getFile(name).getAbsolutePath();
    }

    public File getDirectory() {
        return new File(directory);
    }

    public static String getInnerFile(int i) {
        return i + ".dr";
    }

    public static int getNextAvailableIndex(FileSystem fs) throws IOException {
        int i = 0;
        while(FileUtils.compressedExists(fs, getInnerFile(i)))
            i++;

        return i;
    }

    public FileSystem getZipFs(String enemyName) throws IOException {
        return FileUtils.getZipFs(getAbsoluteFilePath(enemyName));
    }

    public FileSystem getZipFs(DuelRecord record) throws IOException {
        return getZipFs(record.getEnemyName());
    }

    public DuelRecordPackage load(String enemyName) throws IOException {
        return new DuelRecordPackage(getZipFs(enemyName));
    }

    public DuelRecordPackage load(String enemyName, Predicate<DuelRecord> predicate) throws IOException {
        return new DuelRecordPackage(getZipFs(enemyName), predicate);
    }

    public DuelRecordPackage load(FileSystem fs) {
        return new DuelRecordPackage(fs);
    }

    public DuelRecordPackage load(FileSystem fs, Predicate<DuelRecord> predicate) {
        return new DuelRecordPackage(fs, predicate);
    }

    public void save(DuelRecord record) throws IOException {
        File directoryFile = getDirectory();
        if(!directoryFile.isDirectory() && !directoryFile.mkdirs())
            throw new FileNotFoundException("could not create record directory");

        FileSystem fs = getZipFs(record);

        save(fs, record);
        fs.close();
    }

    public static void save(FileSystem fs, DuelRecord record) throws IOException {
        int nextIndex = getNextAvailableIndex(fs);
        FileUtils.compressedWrite(fs, getInnerFile(nextIndex), SerializeHelper.convertToByteArray(record).get());
    }

    public void save(ArrayList<DuelRecord> records) throws IOException {
        for(DuelRecord record : records) {
            this.save(record);
        }
    }
}

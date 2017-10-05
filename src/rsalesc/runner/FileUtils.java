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

package rsalesc.runner;

import com.sun.nio.zipfs.ZipFileSystem;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Roberto Sales on 02/10/17.
 */
public class FileUtils {
    private static final String DUMMY_ZIP_ENTRY = "/_";

    public static void backupedWrite(String file, byte[] data) throws IOException {
        if(Files.exists(Paths.get(file))) {
            if(!Files.isRegularFile(Paths.get(file)))
                throw new IOException("file to be written (and possibly backuped) exists but is not a regular file");

            Files.copy(Paths.get(file), Paths.get(file + ".bkp"), StandardCopyOption.REPLACE_EXISTING);
        }

        Files.write(Paths.get(file), data, StandardOpenOption.CREATE, StandardOpenOption.SYNC);
        Files.deleteIfExists(Paths.get(file + ".bkp"));
    }

    public static void write(Path path, byte[] data) throws IOException {
        Files.write(path, data, StandardOpenOption.CREATE, StandardOpenOption.SYNC);
    }

    public static void compressedWrite(String file, byte[] data) throws IOException {
        compressedWrite(file, DUMMY_ZIP_ENTRY, data);
    }

    public static void compressedWrite(String zipFile, String innerFile, byte[] data) throws IOException {
        compressedWrite(getZipFs(zipFile), innerFile, data);
    }

    public static void compressedWrite(FileSystem zipFs, String innerFile, byte[] data) throws IOException {
        if(!innerFile.startsWith("/"))
            innerFile = "/" + innerFile;

        write(zipFs.getPath(innerFile), data);
        flushZipFs(zipFs);
    }

    public static byte[] compressedRead(FileSystem zipFs, String innerFile) throws IOException {
        return Files.readAllBytes(zipFs.getPath(innerFile));
    }

    public static byte[] compressedRead(String zipFile, String innerFile) throws IOException {
        return compressedRead(getZipFs(zipFile), innerFile);
    }

    public static byte[] compressedRead(String zipFile) throws IOException {
        return compressedRead(zipFile, DUMMY_ZIP_ENTRY);
    }

    public static boolean compressedExists(String zipFile, String innerFile) throws IOException {
        FileSystem fs = getZipFs(zipFile);
        return Files.exists(fs.getPath(innerFile));
    }

    public static boolean compressedExists(FileSystem fs, String innerFile) {
        return Files.exists(fs.getPath(innerFile));
    }

    public static FileSystem getZipFs(String absolutePath) throws IOException {
        // ensure directory existence
        new File(absolutePath).getParentFile().mkdirs();

        final Path path = Paths.get(absolutePath);

        Map<String, Object> env = new HashMap<>();
        env.put("create", "true");
//        env.put("useTempFile", Boolean.TRUE);

        URI uri = URI.create("jar:file:" + path.toUri().getPath());
        return FileSystems.newFileSystem(uri, env);
    }

    public static void flushZipFs(FileSystem fs) {
        if(!(fs instanceof ZipFileSystem))
            throw new IllegalStateException();

        ZipFileSystem zipFs = (ZipFileSystem) fs;

        invokePrivate(zipFs, "beginWrite");
        invokePrivate(zipFs, "sync");
        invokePrivate(zipFs, "endWrite");
    }

    public static <T> void invokePrivate(T self, String methodName) {
        Class<?> clazz = self.getClass();
        try {
            Method method = clazz.getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(self);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}

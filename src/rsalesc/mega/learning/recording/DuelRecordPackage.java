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

import rsalesc.baf2.core.utils.Pair;
import rsalesc.mega.utils.BatchIterable;
import rsalesc.runner.SerializeHelper;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Predicate;

/**
 * Created by Roberto Sales on 02/10/17.
 */
public class DuelRecordPackage implements Iterable<DuelRecord>, BatchIterable<DuelRecord> {
    private final FileSystem zipFs;
    private final Predicate<DuelRecord> predicate;
    private Long shufflingSeed;

    public DuelRecordPackage(FileSystem zipFs, Predicate<DuelRecord> predicate) {
        this.zipFs = zipFs;
        this.predicate = predicate;
    }

    public DuelRecordPackage(FileSystem zipFs, Predicate<DuelRecord> predicate, long seed) {
        this.zipFs = zipFs;
        this.predicate = predicate;
        this.shufflingSeed = seed;
    }

    public void setSeed(Long seed) {
        this.shufflingSeed = seed;
    }

    public void close() throws IOException {
        zipFs.close();
    }

    public void save(DuelRecord record) throws IOException {
        DuelRecordFS.save(zipFs, record);
    }

    public DuelRecordPackage(FileSystem zipFs) {
        this(zipFs, null);
    }

    public List<Path> getRecordInnerPaths() throws IOException {
        PathMatcher matcher = zipFs.getPathMatcher("regex:/?[0-9]+\\.dr");

        ArrayList<Path> matches = new ArrayList<>();

        FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if(attrs.isDirectory())
                    return FileVisitResult.SKIP_SUBTREE;

                if(matcher.matches(file)) {
                    matches.add(file);
                }

                return FileVisitResult.CONTINUE;
            }
        };

        Files.walkFileTree(zipFs.getPath("/"), visitor);

        matches.sort(new Comparator<Path>() {
            @Override
            public int compare(Path o1, Path o2) {
                return o1.compareTo(o2);
            }
        });

        if(shufflingSeed != null) {
            Collections.shuffle(matches, new Random(shufflingSeed));
        }

        return matches;
    }

    public Iterator iterator() {
        return new Iterator();
    }

    private int count(Predicate<DuelRecord> predicate) {
        int res = 0;

        for (DuelRecord record : this) {
            if (predicate.test(record))
                res++;
        }

        return res;
    }

    public int count() throws IOException {
        if(predicate == null)
            return getRecordInnerPaths().size();
        else
            return count(duelRecord -> true);
    }

    public class Iterator implements java.util.Iterator<DuelRecord> {
        private Path current = null;

        private Path lookAfter(Path cur) throws IOException {
            List<Path> paths = getRecordInnerPaths();

            int pos = -1;
            if(current != null) {
                pos = paths.indexOf(cur);
                if (pos == -1)
                    throw new IllegalStateException("iterated file could not be found in DuelRecordPackage");
            }

            if(pos + 1 >= paths.size())
                return null;

            return paths.get(pos + 1);
        }

        private Pair<Path, DuelRecord> lookReallyAfter(Path cur) throws IOException {
            Path nextPath = cur;
            while((nextPath = lookAfter(nextPath)) != null) {
                byte[] read = Files.readAllBytes(nextPath);
                Optional<DuelRecord> optional = SerializeHelper.convertFrom(read);
                DuelRecord record = optional.orElseGet(null);
                if(predicate == null || record != null && predicate.test(record))
                    return new Pair<>(nextPath, record);
            }

            return null;
        }

        @Override
        public boolean hasNext() {
            try {
                if(predicate == null)
                    return lookAfter(current) != null;
                else
                    return lookReallyAfter(current) != null;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        public DuelRecord next() {
            Pair<Path, DuelRecord> next;
            try {
                next = lookReallyAfter(current);
            } catch (IOException e) {
                throw new IllegalStateException("error finding or reading iterated file in DuelRecordPackage");
            }

            if(next == null)
                throw new IndexOutOfBoundsException("iterator somehow exceeded DuelRecordPackage bounds");

            current = next.first;
            return next.second;
        }
    }
}

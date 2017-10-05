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

import java.io.*;
import java.util.Base64;
import java.util.Optional;

/**
 * Created by Roberto Sales on 14/09/17.
 */
public interface SerializeHelper {
    static Optional<String> convertToString(final Serializable object) {
        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(object);
            return Optional.of(Base64.getEncoder().encodeToString(baos.toByteArray()));
        } catch (final IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    static Optional<byte[]> convertToByteArray(final Serializable object) {
        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(object);
            return Optional.of(baos.toByteArray());
        } catch (final IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    static <T extends Serializable> Optional<T> convertFrom(final String objectAsString) {
        final byte[] data = Base64.getDecoder().decode(objectAsString);
        try (final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return Optional.of((T) ois.readObject());
        } catch (final IOException | ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    static <T extends Serializable> Optional<T> convertFrom(final byte[] objectAsByteArray) {
        try (final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(objectAsByteArray))) {
            return Optional.of((T) ois.readObject());
        } catch (final IOException | ClassNotFoundException e) {
            return Optional.empty();
        }
    }
}
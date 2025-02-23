/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor;

import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import java.io.File;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.FileUtils;

/**
 * Feature toggles.
 *
 * @since 1.0
 */
@Immutable
public interface Toggles {

    /**
     * Toggle read only mode.
     * @throws IOException If fails
     */
    void toggle() throws IOException;

    /**
     * Is it read only mode now?
     * @return TRUE if read only
     */
    boolean readOnly();

    /**
     * Inner file.
     *
     * @since 1.0
     */
    @Immutable
    @ToString
    @EqualsAndHashCode
    final class InFile implements Toggles {

        /**
         * Directory to work in.
         */
        private static final String DIR = String.format(
            "/tmp/rultor-%s", Env.read("Rultor-Revision")
        );

        @Override
        public void toggle() throws IOException {
            final File file = this.file();
            synchronized (Toggles.class) {
                if (this.readOnly()) {
                    if (!file.delete()) {
                        throw new IllegalStateException(
                            String.format("failed to delete %s", file)
                        );
                    }
                } else {
                    FileUtils.touch(file);
                }
            }
        }

        @Override
        public boolean readOnly() {
            synchronized (Toggles.class) {
                return this.file().exists();
            }
        }

        /**
         * Get file.
         * @return File
         */
        private File file() {
            final File file = new File(Toggles.InFile.DIR, "read-only");
            if (file.getParentFile().mkdirs()) {
                Logger.info(this, "directory created for %s", file);
            }
            return file;
        }
    }

}

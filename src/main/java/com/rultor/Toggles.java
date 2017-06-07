/**
 * Copyright (c) 2009-2017, rultor.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the rultor.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.rultor;

import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import com.jcabi.manifests.Manifests;
import java.io.File;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.FileUtils;

/**
 * Feature toggles.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
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

    @Immutable
    @ToString
    @EqualsAndHashCode
    final class InFile implements Toggles {
        /**
         * Directory to work in.
         */
        private static final String DIR = String.format(
            "/tmp/rultor-%s", Manifests.read("Rultor-Revision")
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

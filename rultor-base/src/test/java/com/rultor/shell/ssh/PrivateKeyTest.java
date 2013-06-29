/**
 * Copyright (c) 2009-2013, rultor.com
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
package com.rultor.shell.ssh;

import java.io.File;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link PrivateKey}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class PrivateKeyTest {

    /**
     * PrivateKey can parse input text.
     * @throws Exception If some problem inside
     */
    @Test
    public void parsesValidText() throws Exception {
        final PrivateKey key = new PrivateKey(
            // @checkstyle StringLiteralsConcatenation (3 lines)
            "\n\t\t-----BEGIN RSA PRIVATE KEY-----\n"
            + "\t\tMIIEowIBAAKCAQEA2Df07r59ThOErTDr\n\n"
            + "\t\t-----END RSA PRIVATE KEY-----\n\n"
        );
        final File file = key.asFile();
        MatcherAssert.assertThat(file.exists(), Matchers.is(true));
        MatcherAssert.assertThat(file.length(), Matchers.greaterThan(0L));
    }

    /**
     * PrivateKey can parse and understand a real RSA key.
     * @throws Exception If some problem inside
     */
    @Test
    public void parsesAndUnderstandsRsaKey() throws Exception {
        final PrivateKey key = new PrivateKey(
            // @checkstyle StringLiteralsConcatenation (50 lines)
            // @checkstyle LineLength (50 lines)
            "-----BEGIN RSA PRIVATE KEY-----\n"
            + "MIIEogIBAAKCAQEA5avG9QSc28r8lc7dzwD4WwuU8HYd8PQ1vFp3LeHsm/GMhpBx\n"
            + "4TsjSNF/Yn9hyRb6RlTYMglZ6yI5RCfEceAARgWiez0fljNoQt7yupNMqHjtlXk+\n"
            + "HM7cW3xekf9fwQloe5XEIQgNxHm9mgpPEMMWL9gAxoFB2ZmbuFrD+qQlS6fROCRu\n"
            + "GxzUV4Xhm0k82ojGChVVQgbi3whs90ogaj3ClUfg+cv2IZSORIDf0wHe+A8c2zbC\n"
            + "G1pBTQ1WmqtjyQmRV0dvlnxheFDR1v5jl/rRN0lhDrTMK8vpD+6fNYj2NrqMJIHD\n"
            + "Je+q0zVHlIhcpx01UqOgmDbNx8yujYhEZytUfQIDAQABAoIBAFe/dIlCVOfIuw5F\n"
            + "dgtTtI+ccjAZAMGBnFI1Qhl+mtbNx8HVyBykaou0tvWb83FP3Rkn613aoscqUTTP\n"
            + "+McFLTWTi4uIQ9wwEeKSqaxNANnRMfyM0QVSp3AwqTUgm6X2ATxOcRuppxDs7lq/\n"
            + "G4ws7Pokjc9JrS4E+TDLRNsrAbDQWNXwKfBSlBSHvmNN6ah2vMPV1ypyVzFX8HSn\n"
            + "zzXUMoJA+lSanqvcahkAMkNKflye6V/dH2TxwLjCOaa1RToyZkkpnLFpYaIhf8Hq\n"
            + "mlfCaTvCvFQgke0HFOuYQC2D3+PiyEwEzW+h1GnJDE3jNlKGtkTuUDsBcZosEigb\n"
            + "iHP1gMUCgYEA905UXF2LhLTzNaf1fYdWkagIAaq9zDc9hw7gU0GLoSKJCe1FWJXh\n"
            + "2o/MMjVZai3Iuhh1TjwuCF36wPah+XXdaCOUjGC2u9zd4VH8PTCBhw1zRgMHfkZL\n"
            + "JBhg4xLshX645e0FdqXU4QDpwG9/KmMG/qcImeckr84MPox/Lx3kiD8CgYEA7b69\n"
            + "JVXEjMYVNpPxo0Qu48KxneV77RkQP/cnVhKrRu9pndmCImw6QLtwvzTakV18/hOc\n"
            + "09LkZEIXqWTx6TC7QxVgEv88eGlkA3iALeJuRKvdu1wxQV2tKaq/s8D9x4NgWlC/\n"
            + "hwyR9Z1xM97VEm5SbNIP/3Mlo4lKXefYulaZVEMCgYAOQaO3V2Utl0jV9QK/48dn\n"
            + "yin7/p3GYgDYAlGIOyUTeuwveFAhFTLZ6KQ62Lx41H7Xy4Unp2x36wMDkLQxr0u+\n"
            + "Lx7nIMQn/EmGLbW+yUcORAY7KP2Ll/3I7ObY8ERWRcHe71T7TAADoIvZHhRUmzTS\n"
            + "BUsHpD2HLPeju3Gxg6Wi/QKBgCABuMco99AX7s0tN6/KQyHImU7vkTKHWEe0R2hw\n"
            + "FPz0yFxG5x0pQuJqGbC0NeSyGFzAWTGvR64zs6nLfEHvnJZYb1m/YO25CozpESCc\n"
            + "RkoadTx/GeZ57REZ+rsBdWkBx9wA2Pgeehv9+TqJelgD4DbkROEYKG4O0qM3zlav\n"
            + "x+6vAoGAbQzLM0sk4BiDDek2oeU7m7SYn3ivSrgXFJ4RoWmEWWvPGt4238FWBzD5\n"
            + "7kOayfXYajrio9ERAS52jTwUitdsEdkrNqzKPJ3oxv3+RHveYmiJUysyf/+Oyes/\n"
            + "8EwyuWTDO5kWN0Oo0ghYBQ7kXbSIMF0Nj7x+KtDdtWW6nKJc3nE="
            + "-----END RSA PRIVATE KEY-----"
        );
        MatcherAssert.assertThat(
            key, Matchers.hasToString("`RSA PRIVATE KEY/1152 bytes`")
        );
    }

}

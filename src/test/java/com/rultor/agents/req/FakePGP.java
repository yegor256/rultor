/**
 * Copyright (c) 2009-2018, rultor.com
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
package com.rultor.agents.req;

import java.io.IOException;
import org.cactoos.list.SolidList;
import org.cactoos.text.JoinedText;

/**
 * Fake PGP Signature.
 *
 * @author Filipe Freire (livrofubia@gmail.com)
 * @version $Id$
 * @since ?
 */
final class FakePGP {

    /**
     * Returns FakePGP string.
     *
     * @return String
     * @throws IOException ex
     */
    public String asString() throws IOException {
        return new JoinedText(
            "\n",
            new SolidList<>(
                "-----BEGIN PGP MESSAGE-----",
                "Version: GnuPG v1\n",
                "hQEMA5qETcGag5w6AQgAvm/P0JUlQAd",
                "OtGng5zHLx5cV+BrbpFt1m2ja4BjacYMU",
                "wcubtJSh+n0XNLk6zMMCsrDnTfzvi/F",
                "EFaRsPVb/ZJHiJGvwhNGyenQWgd6bczIL",
                "1UxBZ1BpHTPv5hVK43fb6cYq+e/gniB",
                "MvIKlKV+Qh/NVtiQACQJ5xL1M16S9SQuY",
                "hjnVEL3JNHiLEAfPS/8xS05DY/w1k/J",
                "yPXMZlrR7YGMxUsG6aDaFPAdjcdSbzGCT",
                "j4yZPdZtyqePFGXn0VJE7GRywWcmk3N",
                "j+oZzgx6DLV3PH40HSYNuyA9a2xFpghTr",
                "7uiYRf+rRzXlx7qnBLsvETlhc77zpf0",
                "FW4pLq/08ttLADQFsIU2BNHJGPw+96GKJ",
                "AVNAm0OxfaMz+U+gy2kIgteuMQmfkYD",
                "F0u9HE7NwZ1PlXO5Oszhfdim2LPSyxYMi",
                "sKlVilWhPwdumSjmY0IG1B6yc8ZLG4B",
                "jBucu3dMjj98iKRjlKvEmqqdUmoZY+l/N",
                "Ye9gRf0UY44jJ0f4H81osGtmXg1dRc4",
                "7OE/pUGGbIare4GNvBB/oiksvoCDOOEKy",
                "cj6IAjR/BnSZ1mYvSShSPatu7QRFdd/",
                "HFRt76pGj2G6ibnnDNpfjDwgNaWbiGUU=",
                "=d2bb",
                "-----END PGP MESSAGE-----"
            )
        ).asString();
    }

}

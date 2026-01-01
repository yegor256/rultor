/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.req;

import org.cactoos.list.ListOf;
import org.cactoos.text.Joined;
import org.cactoos.text.UncheckedText;

/**
 * Fake PGP Signature.
 *
 * @since 1.67.1
 * @checkstyle AbbreviationAsWordInNameCheck (5 lines)
 */
final class FakePGP {

    /**
     * Returns FakePGP string.
     *
     * @return String
     * @checkstyle NonStaticMethodCheck (35 lines)
     */
    public String asString() {
        return new UncheckedText(
            new Joined(
                "\n",
                new ListOf<>(
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
            )
        ).asString();
    }

}

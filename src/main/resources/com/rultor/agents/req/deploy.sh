#!/usr/bin/env bash
# SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
# SPDX-License-Identifier: MIT

set -ex -o pipefail

cd repo
docker_when_possible

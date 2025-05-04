#!/bin/bash
# SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
# SPDX-License-Identifier: MIT

set -ex -o pipefail

cd repo
docker_when_possible

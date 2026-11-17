#!/usr/bin/env bash

# SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
# SPDX-License-Identifier: MIT

# shellcheck disable=SC2154

set -ex -o pipefail

cd repo
git remote add fork "${fork}"
git remote update
git checkout -B "${fork_branch}" "fork/${fork_branch}"
git rebase "origin/${head_branch}"
git push --force fork "${fork_branch}:refs/heads/${fork_branch}"

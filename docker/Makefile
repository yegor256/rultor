# SPDX-FileCopyrightText: Copyright (c) 2024 Yegor Bugayenko
# SPDX-License-Identifier: MIT

.ONESHELL:
.SHELLFLAGS := -e -o pipefail -c
SHELL := bash

all:
	for i in ruby java latex python; do
		docker build --file "$${i}/Dockerfile" --platform=linux/x86_64 -t "yegor256/rultor-$${i}" "$${i}"
		docker push "yegor256/rultor-$${i}"
	done

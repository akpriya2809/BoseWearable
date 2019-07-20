#!/usr/bin/env bash

work_dir=binary-update
binary_repo=git@github.com:BoseCorp/BoseWearable-Android-bin.git
full_repo=git@github.com:BoseCorp/BoseWearable-Android-src.git
docs_repo=git@github.com:BoseCorp/staging-bose-ar-android-portal-reference.git

mkdir "$work_dir"
pushd "$work_dir"

# Check out from binary
git clone $binary_repo BoseWearable-Android
cd BoseWearable-Android

# Get updates from BoseCorp repository
git remote add bosecorp $full_repo
git fetch bosecorp

cd ..
git clone $docs_repo docs-repo
popd

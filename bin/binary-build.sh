#!/usr/bin/env bash

work_dir=binary-update

if [ -z "$ANDROID_HOME" ]
then
    sdk_path="~/Library/Android/sdk"
else
    sdk_path="$ANDROID_HOME"
fi

if [ ! -d "$sdk_path" ]
then
    echo "Android SDK directory not found."
    echo "Use ANDROID_HOME environment variable to specify it"
    exit 1
fi

pushd "$work_dir/BoseWearable-Android"

git checkout -b upstream bosecorp/master
ANDROID_HOME="$sdk_path" ./gradlew blecore:assembleRelease bosewearable:assembleRelease bosewearableui:assembleRelease generateCombinedJavadoc
git filter-branch --prune-empty --index-filter 'git rm --cached -r bosewearableui bosewearable blecore javadoc_* || true' -- --all

# Merge upstream changes
mv README.html README.html.new
git checkout master
mv README.html.new README.html

git diff --quiet master..upstream
changed=$?
if [ $changed -ne 0 ]
then
  echo "BoseWearable-Android changed"
  # FIXME: Detect conflicts
  git merge upstream
else
  echo "BoseWearable-Android did not change"
fi

# Copy updated binaries
mkdir -p aar
cp ./blecore/build/outputs/aar/blecore-release.aar ./aar
cp ./bosewearable/build/outputs/aar/bosewearable-release.aar ./aar
cp ./bosewearableui/build/outputs/aar/bosewearableui-release.aar ./aar
git add aar/

# Copy updated docs
cp -r build/docs/ ./html-docs/
git add html-docs
git add README.html

git diff --quiet --cached
binaries_changed=$?
if [ $binaries_changed -ne 0 ]
then
  echo "Binaries changed"
  if [ $changed -ne 0 ]
  then
    # Both changed, merge to one commit
    git commit --amend -C HEAD
  else
    # Only binaries changed, create a new commit
    git commit -m "Merging binaries and docs"
  fi
else
  echo "Binaries did not change"
fi


git branch -D upstream
popd

if [ $changed -ne 0 ] || [ $binaries_changed -ne 0 ]
then
  exit 1
fi

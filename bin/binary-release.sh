#!/usr/bin/env bash

work_dir=binary-update

E_BADARGS=85
if [ $# -ne 1 ]; then
    echo "Usage: $(basename $0) version-name"
    exit $E_BADARGS
fi

# Take the version name from the command line argument. This is used at various
# points throughout the script.
version_name="$1"

pushd "$work_dir"

# Tag a release in -src repo
cd BoseWearable-Android
git checkout -b upstream bosecorp/master
git tag -s $version_name -m "Release $version_name"
git push --tags bosecorp upstream:master

# Tag a release in -bin repo
git checkout master
git tag -d $version_name
git tag -s $version_name -m "Release $version_name"
git push --tags origin master
hub release create -m "$version_name" "$version_name"

cd ..

# Copy docs
[ -d "docs-repo/$version_name" ] && rm -rf "docs-repo/$version_name"
mkdir -p "docs-repo/$version_name"

cp -R BoseWearable-Android/html-docs/ "docs-repo/$version_name"
cd docs-repo
git add .
git commit -m "Adding $version_name docs"
git push origin master

popd

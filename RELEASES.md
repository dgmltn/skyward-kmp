# Releases

Pushing a `vX.Y.Z` tag makes GitHub build a signed Android APK and publish a release with it
attached, with notes listing the changes since the previous tag. The workflow is
[.github/workflows/release.yml](.github/workflows/release.yml).

## Making a release

1. Bump the version in both places, keeping them the same:
   - `gradle.properties`: `skyward.versionName` and `skyward.versionCode` (the code must go up
     every release, or Android won't install the new APK over the old one).
   - `app-ios/project.yml`: `MARKETING_VERSION` and `CURRENT_PROJECT_VERSION`.
2. Commit and push to `main`.
3. Tag and push the tag:

   ```sh
   git tag v0.2.0
   git push origin v0.2.0
   ```

The workflow stops with an error if the tag doesn't match `skyward.versionName`.

## One-time setup

The release workflow signs with a key kept outside the repository. It reads four secrets from the
repository's `release` environment:

| Secret | What it holds |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | The keystore file, base64-encoded |
| `ANDROID_KEYSTORE_PASSWORD` | The keystore's password |
| `ANDROID_KEY_ALIAS` | The key's alias in the keystore |
| `ANDROID_KEY_PASSWORD` | The key's password |

Keep a backup of the keystore and its passwords: losing them means later releases can't be
installed over earlier ones.

## Who can release

Only people with write access to the repository can push tags, so only they can trigger a release.
Forks and pull requests can't: workflows from a fork run in the fork, without this repository's
secrets.

For extra safety, the signing secrets live in the `release` environment, which can be set (under
Settings → Environments → release) to require your approval before each release job runs, and a
tag ruleset (Settings → Rules) can limit who may create `v*` tags.

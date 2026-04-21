# Releasing resource-server

Adapted from the uPortal release guide, with learnings from the March/April 2026 uPortal 5.x release folded in. Resource-server is a Maven build (uses `maven-release-plugin`), so the mechanics differ from uPortal's Gradle flow — but the Sonatype / Central Publisher Portal requirements are the same.

## Prerequisites

There are 3 prerequisites to cutting releases:

1. [Sonatype Account at Central Publisher Portal](https://central.sonatype.com/)
    - NOTE!! Do not link a social account — create a local account.
    - See first part of [Register to Publish Via the Central Portal](https://central.sonatype.org/register/central-portal/).
    - **Note:** The legacy OSSRH service (`oss.sonatype.org`) was sunset June 2025. All publishing now goes through the Central Publisher Portal.
2. Permissions to release projects
    - Namespace permissions are managed via the [Central Publisher Portal](https://central.sonatype.com/).
    - If you need access to the `org.jasig.resourceserver` namespace, contact a uPortal committer.
    - Expect approval to take a few days to complete.
3. [Set up public PGP key on a server](https://central.sonatype.org/pages/working-with-pgp-signatures.html)
    - Generate a key pair `gpg2 --gen-key`.
    - If you choose to have an expiration date, edit the key via `gpg2 --edit-key {key ID}`.
    - Determine the key ID and keyring file `gpg2 --list-keys` (the key ID is the `pub` ID).
    - Distribute your public key `gpg2 --keyserver hkp://keyserver.ubuntu.com --send-keys {key ID}`.

## Setup

Export your secret keyring via `gpg2 --keyring secring.gpg --export-secret-keys > ~/.gnupg/secring.gpg`.

In `$HOME/.m2/settings.xml` place your credentials for the Central Publisher Portal and your configuration for signing artifacts with GNU Privacy Guard (GnuPG). Use **Central Portal user tokens**, not your login password — generate tokens at <https://central.sonatype.com> under your profile/account settings.

```xml
<settings>
  <servers>
    <server>
      <id>central</id>
      <username>{portal token username}</username>
      <password>{portal token password}</password>
    </server>
  </servers>
  <profiles>
    <profile>
      <id>gpg</id>
      <properties>
        <gpg.keyname>{key ID}</gpg.keyname>
        <gpg.passphrase>{secret}</gpg.passphrase>
      </properties>
    </profile>
  </profiles>
</settings>
```

Setup only needs to be done once.

See the [OSSRH Staging API guide](https://central.sonatype.org/publish/publish-portal-ossrh-staging-api/) and the [Central Publisher Portal guide](https://central.sonatype.org/publish/publish-portal-guide/) for more assistance.

## Which Repo?

Perform releases from a clone of the official `uPortal-Project/resource-server` repository, not from a fork. This avoids an extra configure-upstream step during `release:perform`.

## Pre-Release Setup

Before running the release, confirm the following — these are the uPortal-release learnings that apply here verbatim.

### 1. Distribution management points at Central Portal, not OSSRH

The legacy Sonatype OSSRH host `oss.sonatype.org` was sunset in June 2025. Every `<distributionManagement>` entry in this project (and any inherited from parent POMs) must point at the Central Publisher Portal's OSSRH Staging API compatibility service.

**Required URLs:**

- Release: `https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/`
- Snapshot: `https://central.sonatype.com/repository/maven-snapshots/`
- Server id: `central` (in `~/.m2/settings.xml`)

**Audit before release:**

```sh
grep -RIn --include=pom.xml -E 'oss\.sonatype\.org|sonatype-nexus-staging' .
```

Replace any stale URLs. As of this writing, `resource-server-webapp/pom.xml` overrides the inherited distribution management with the old `oss.sonatype.org` URL; this must be updated before the release will succeed. The JAR modules inherit from `org.jasig.portlet:uportal-portlet-parent` which in turn inherits from `org.sonatype.oss:oss-parent:9` — that ancient parent also carries the dead URLs, so either:

- (a) Release a new `uportal-portlet-parent` first that overrides the URLs, then consume it from this project, **or**
- (b) Add an explicit `<distributionManagement>` block in this project's top-level `pom.xml` with the Central Portal URLs so the inherited values are shadowed.

Option (b) is the fastest path for a single release; option (a) is the right long-term fix.

### 2. POM packaging must match the actual artifact

The Central Portal validation rejects deployments when the advertised `<packaging>` does not match the uploaded file extension. In uPortal, the Gradle `uploadArchives` block hardcoded `packaging 'jar'` and broke the WAR module; we had to make packaging dynamic.

In Maven, `<packaging>` is authoritative per-module, so the same bug does not occur here — but verify anyway:

```sh
grep -l '<packaging>war</packaging>' $(find . -name pom.xml -not -path '*/target/*')
```

`resource-server-webapp` is the only WAR module; confirm it still has `<packaging>war</packaging>`.

### 3. Community notice

For any non-snapshot release, email `uportal-dev` a couple of working days prior:
- Request any in-progress PRs be merged by a certain date/time.
- Request adopters test the tip of `master`.

### 4. Sync with upstream

Make sure the local `master` is fast-forwarded to `upstream/master` and pushed to `origin/master`. This project uses the `origin` / `upstream` fork layout.

```sh
git fetch upstream
git checkout master
git merge --ff-only upstream/master
git push origin master
```

## Cut Release

From a clean clone of the official repo:

```sh
cd resource-server/
mvn clean
mvn release:prepare
mvn release:perform
```

- `release:prepare` will prompt for a release version (e.g. `1.5.1`), the tag name, and the next snapshot (e.g. `1.5.2-SNAPSHOT`). The tag naming is controlled by `<tagNameFormat>resource-server-@{project.version}</tagNameFormat>` in the parent `pom.xml`.
- `release:perform` checks out the tag, builds, signs artifacts with GPG, and uploads to the repo configured in `<distributionManagement>`.

If `release:prepare` fails partway through, `mvn release:rollback` will undo the working-tree changes. If `release:perform` fails after upload, the staged deployment will still be visible in the Central Portal and you will need to drop it there manually.

## Verify and Publish from Central Publisher Portal

Because the release uses the legacy Maven-API-flavored upload path, the OSSRH Staging API does **not** automatically close the staging repository. After `release:perform` completes, you must manually trigger the upload to the Central Portal.

### Push staged artifacts to the portal

Run this from the **same machine** that ran the release — the Central Portal matches by source IP:

```sh
curl -X POST \
  "https://ossrh-staging-api.central.sonatype.com/manual/upload/defaultRepository/org.jasig.resourceserver" \
  -H "Authorization: Bearer $(echo -n '{ossrhUsername}:{ossrhPassword}' | base64)"
```

Replace `{ossrhUsername}` and `{ossrhPassword}` with your Central Portal user token credentials (the same ones in `~/.m2/settings.xml`).

### Review and publish

1. Log into <https://central.sonatype.com>.
2. Navigate to your deployments — the staged artifacts should now be visible.
3. Review the release for the expected artifacts (parent POM + all six submodules, including the WAR).
4. Verify the artifacts pass validation checks (signatures, POM requirements).
5. Publish the deployment to make it available on Maven Central.

If the deployment does not appear, you can search for open repositories:

```sh
curl -X GET \
  "https://ossrh-staging-api.central.sonatype.com/manual/search/repositories?ip=any&profile_id=org.jasig.resourceserver" \
  -H "Authorization: Bearer $(echo -n '{ossrhUsername}:{ossrhPassword}' | base64)"
```

## Create Release Notes

1. Inspect commits since the last tag, e.g.: `git log resource-server-1.5.0..resource-server-1.5.1 --no-merges`
2. Review the issue tracker and confirm referenced issues have been resolved.
3. Enter the release notes on the GitHub releases page in the `uPortal-Project/resource-server` repo.
    - Use the previous release notes as a guide.
    - Group commits by type (Fixes, Chores, Features, Dependency updates, etc.) with a header per group.

## Update Downstream Consumers

Open PRs (or file issues) in the downstream projects to consume the new version:

- `uPortal` — `gradle.properties` → `resourceServerVersion`
- `uPortal-start` — `gradle.properties` → `resourceServerVersion` (intentionally stays at `1.0.48` per the uPortal RELEASE.md; check whether this policy still applies) and `resourceServer13Version`

## Update Community

For any non-snapshot release, email an announcement to `uportal-dev`, `uportal-user`, and `jasig-announce`:
- Acknowledge contributors.
- Put the release into context for existing adopters.

## References

- Upstream uPortal release guide: `uPortal/docs/developer/other/RELEASE.md`
- <https://central.sonatype.org/publish/publish-portal-ossrh-staging-api/>
- <https://central.sonatype.org/publish/publish-portal-guide/>
- <https://maven.apache.org/maven-release/maven-release-plugin/>

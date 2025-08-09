# Performing a release

## General information for publishing to Maven Central

* https://central.sonatype.org/publish/publish-portal-guide/
* https://central.sonatype.org/publish/publish-portal-maven/

## Steps for (mostly) one-time setup

* `git clone https://github.com/com-github-javabdd/com.github.javabdd.git javabdd.git`
* cd `javabdd.git`
* Edit `.git/config`, adding:
  ```ini
  [user]
      name = FirstName LastName
      email = EmailAddress
  ```
  By filling in your `FirstName` and `LastName` and your GitHub `EmailAddress`.
* Create a GitHub personal access token, at https://github.com/settings/tokens.
  Use it as a password.
  It expires after some time, so create a new one if needed.
* Create a Sonatype publishing token, at https://central.sonatype.com/account.
  Log in and go to https://central.sonatype.com/account.
  Click *Generate User Token*.
  Follow the steps, and note the configuration snippet that is generated, which is to be used in a later step.
* Create a local GPG key.
* Configure GPG key and "central" server credentials in Maven settings.
  Create/edit `$HOME/.m2/settings.xml`, to have the following settings:
  ```xml
  <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                        http://maven.apache.org/xsd/settings-1.0.0.xsd">

    <profiles>
      <profile>
        <id>central</id>
        <activation>
          <activeByDefault>true</activeByDefault>
        </activation>
        <properties>
          <gpg.passphrase>GPG_KEY_PASSPHRASE</gpg.passphrase>
        </properties>
      </profile>
    </profiles>

    <servers>
      <server>
        <id>central</id>
        <username>SONATYPE_PUBLISHING_USERNAME</username>
        <password>SONATYPE_PUBLISHING_TOKEN</password>
      </server>
    </servers>
  </settings>
  ```
  Fill in your `GPG_KEY_PASSPHRASE`, `SONATYPE_PUBLISHING_USERNAME` and `SONATYPE_PUBLISHING_TOKEN`.

## Steps for each release

* Make sure `CHANGES.md` has an entry for the new release.
* `export JAVA_HOME=SOME_PATH` with `SOME_PATH` the path to the root folder of a JDK 17 installation.
* `export PATH=SOME_PATH/bin:SOME_PATH2/bin:$PATH` with `SOME_PATH` the path to the root folder of your GPG installation, and `SOME_PATH2` the path to the root folder of a JDK 17 installation.
* `./release-prepare` from your clone of the JavaBDD repo.
  When asked for a release version, type something like `9.0.0` and press *Enter*.
  When asked for an SCM tag/label, press *Enter*.
  When asked for the new development version, press *Enter*.
* `./release-perform` from the same directoy.
* If all was successfull, check the new commits that are made, and push them using `git push`.
* Also push the new tag using `git push --tags`.
* Manually create a new GitHub release, basing it on the new tag, setting the version as release title, and copying the changelog entry from `CHANGES.md` as the release notes text.
* Check that the new release is available at https://central.sonatype.com/artifact/com.github.com-github-javabdd/com.github.javabdd/versions.
* Check that the new release can be found at https://oss.sonatype.org/#nexus-search;quick~javabdd.

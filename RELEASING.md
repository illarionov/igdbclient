# Releasing

1. Change the version in `config/version.properties` to a non-SNAPSHOT version.
2. Update the `CHANGELOG.md` for the impending release.
3. Update the `README.md` with the new version.
4. `git commit -am "Prepare for release X.Y.Z."` (where X.Y.Z is the new version)
5. `git tag -a X.Y.Z -m "Version X.Y.Z"` (where X.Y.Z is the new version)
6. `. /gradlew clean build publishAllPublicationsToMavenCentralRepository`
    Or trigger publish workflow in GitHub Actions
7. `./gradlew closeAndReleaseRepository`
    Or visit [Sonatype Nexus](https://s01.oss.sonatype.org/) and promote the artifact.
8. Update the `config/version.properties` to the next SNAPSHOT version.
9. `git commit -am "Prepare next development version."`
10. `git push && git push --tags`

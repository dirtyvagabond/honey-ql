# Creates the eclipse project from the pom.
# If you don't like all the check-dependency-versions-over-the-web shenanigans,
# you can tack a -o to the end of this command, and Maven will run in offline mode.
mvn eclipse:clean eclipse:eclipse -DdownloadSources=true

# Installs some our team coding conventions (formatting, warnings, etc.)
cp ide_support/eclipse/*.prefs .settings/

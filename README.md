# gplay-downloader

A simple Google Play Store downloader based on [AuroraOSS GPlayApi](https://gitlab.com/AuroraOSS/gplayapi)

## Building
- `gradle build` will build the application and package it as a jar file without dependencies
- `gradle shadowJar` will build the application and package all of its dependencies into a single jar file

The output jar files can be found at `<project root>/app/build/libs/`

## Usage
1. Obtain an AASToken using [this app](https://github.com/whyorean/Authenticator)
2. Create a file named auth config text file with the following contents:
    - Type the email address you used to obtain the AASToken and the AASToken on one line, divided by a space character
3. Create a file with the app ids of the applications you want to download. Each app id must be on a separate line.
4. Run the app with `java -jar <jar file> -a <the file containing app ids> -c <the auth config file> -o <output directory>`

## License
gplay-downloader is licensed under the [GNU General Public License version 3](/LICENSE) or later.

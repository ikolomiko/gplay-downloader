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

## Citations
This project has been cited in the following academic papers:

### Haly : Automated evaluation of hardening techniques in Android and iOS apps
- **Author:** Wilco van Beijnum
- **Published as:** Master's Thesis, University of Twente
- **Year:** 2023
- [Link](https://purl.utwente.nl/essays/95578)

### SoK: Hardening Techniques in the Mobile Ecosystem — Are We There Yet?
- **Authors:** Magdalena Steinböck, Jens Troost, Wilco van Beijnum, Jan Seredynski, Herbert Bos, Martina Lindorfer, Andrea Continella
- **Presented in:** 2025 IEEE European Symposium on Security and Privacy (EuroS&P)
- **Year:** 2025
- [Link](https://download.vusec.net/papers/haly_eurosp25.pdf)

If you have used or referenced this project in your research, please consider citing it as well! An example BibTeX entry can be found below.

```bibtex
@software{ikolomiko_gplay_downloader,
  author = {İlker Avcı},
  title = {gplay-downloader},
  year = {2023},
  license = {GPL-3.0-or-later},
  url = {https://github.com/ikolomiko/gplay-downloader},
  note = {Accessed: 2025-08-10}  % Replace with the date you accessed it
}
```

## License
gplay-downloader is licensed under the [GNU General Public License version 3](/LICENSE) or later.

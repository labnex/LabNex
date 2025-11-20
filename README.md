[![License: GPL v3](https://raw.githubusercontent.com/labnex/LabNex/main/assets/license.svg)](https://www.gnu.org/licenses/gpl-3.0) [![Release](https://img.shields.io/github/v/release/labnex/LabNex?display_name=release&label=release)](https://github.com/labnex/LabNex/releases) [![Crowdin](https://badges.crowdin.net/labnex/localized.svg)](https://crowdin.com/project/labnex)

[<img alt="Become a Patreon" src="https://raw.githubusercontent.com/labnex/LabNex/main/assets/patreon.png" height="80"/>](https://www.patreon.com/mmarif)

# LabNex - Android app for GitLab

LabNex is an open-source Android app for GitLab. You can use it with GitLab.com or your self-hosted GitLab instance. It supports multiple accounts, allowing you to add as many instances as you need.  
LabNex is licensed under the GPLv3 License.

## Downloads

[<img alt='Get it on F-Droid' src='https://raw.githubusercontent.com/labnex/LabNex/main/assets/fdroid.png' height="80"/>](https://f-droid.org/en/packages/com.labnex.app/)
[<img alt='Get it on Google Play' src='https://raw.githubusercontent.com/labnex/LabNex/main/assets/google-play.png' height="80"/>](https://play.google.com/store/apps/details?id=com.labnex.app.premium)
[<img alt='Download builds and releases' src='https://raw.githubusercontent.com/labnex/LabNex/main/assets/apk-badge.png' height="82"/>](https://github.com/labnex/LabNex/releases)
[<img alt='Get it on OpenAPK' src='https://raw.githubusercontent.com/labnex/LabNex/main/assets/openapk.png' height="82"/>](https://www.openapk.net/labnex/com.labnex.app/)

## Features

- Multiple accounts support
- Projects and groups listing
- Biometric lock for security
- Issues, merge requests, and commits
- Project releases, milestones, and wiki
- Markdown support with code editor
- Diff viewer for code changes
- Localized in multiple languages

[Complete feature list →](https://github.com/labnex/LabNex/wiki/Features)

## Screenshots

[<img src="https://raw.githubusercontent.com/labnex/LabNex/main/metadata/en-US/images/phoneScreenshots/001.png" alt="Dashboard" width="200"/>](https://raw.githubusercontent.com/labnex/LabNex/main/metadata/en-US/images/phoneScreenshots/001.png) | [<img src="https://raw.githubusercontent.com/labnex/LabNex/main/metadata/en-US/images/phoneScreenshots/002.png" alt="Projects" width="200"/>](https://raw.githubusercontent.com/labnex/LabNex/main/metadata/en-US/images/phoneScreenshots/002.png) | [<img src="https://raw.githubusercontent.com/labnex/LabNex/main/metadata/en-US/images/phoneScreenshots/003.png" alt="Issues" width="200"/>](https://raw.githubusercontent.com/labnex/LabNex/main/metadata/en-US/images/phoneScreenshots/003.png) | [<img src="https://raw.githubusercontent.com/labnex/LabNex/main/metadata/en-US/images/phoneScreenshots/004.png" alt="Code" width="200"/>](https://raw.githubusercontent.com/labnex/LabNex/main/metadata/en-US/images/phoneScreenshots/004.png)
---|---|---|---

[More screenshots →](https://github.com/labnex/LabNex/tree/main/metadata/en-US/images/phoneScreenshots)

## Add a Custom URL Scheme

Starting with version **7.0.0**, LabNex supports a custom URL scheme. This feature allows you to seamlessly open links directly in LabNex for issues, merge requests, projects by using third-party apps like [URL Check](https://github.com/TrianguloY/URLCheck).

### How to Configure URL Check

1. Install the URL Check app from F-Droid or the Google Play Store.
2. Make sure it’s set as the default browser app in your Android settings. You can find this under Apps.
3. Open URL Check app and tap on **Module**.
4. Select **Pattern Checker** and then tap on **Json edit**.
5. Copy and paste the following JSON configuration into the editor. You can customize the `regex` parameter to add your own instances.
6. Save your changes.

JSON Configuration:
```
"LabNex": {
  "regex": "^https?://(?:[a-z0-9-]+\\.)*?(gitlab\\.com|framagit\\.org)(/.*)",
  "replacement": "labnex://$1$2"
}
```

## Development

- [Building from source](https://github.com/labnex/LabNex/wiki/Building)
- [Contributing guidelines](https://github.com/labnex/LabNex/wiki/Contributing)

## Frequently Asked Questions

[View complete FAQ →](https://github.com/labnex/LabNex/wiki/FAQ)

## Translation

Help translate LabNex on [Crowdin](https://crowdin.com/project/labnex). Request new languages [here](https://github.com/labnex/LabNex/issues).

## Links

- [Website](https://labnex.app/)
- [Privacy Policy](https://labnex.app/privacy)
- [Wiki](https://github.com/labnex/LabNex/wiki/)
- [FAQ](https://github.com/labnex/LabNex/wiki/FAQ)

## Acknowledgments

Thanks to all open-source libraries, contributors, and donors for your support.

- Libraries: [Open-source libraries used](https://github.com/labnex/LabNex/wiki/Open-source-libraries)
- Icon sets: [tabler/tabler-icons](https://github.com/tabler/tabler-icons)

## Social

- [Fediverse - mastodon.social/@mmarif](https://mastodon.social/@mmarif)
- [Bluesky](https://bsky.app/profile/mmarif.bsky.social)
- [X profile](https://x.com/mmarif08)

## Support LabNex

Developing and maintaining LabNex requires significant time and effort. Help us keep the project sustainable and free:

- **Donate**: Support development via [Patreon](https://www.patreon.com/mmarif) or purchase the [Play Store version](https://play.google.com/store/apps/details?id=com.labnex.app.premium)
- **Contribute**: Help build new features - see our [Contributing Guide](https://github.com/labnex/LabNex/wiki/Contributing)
- **Report Issues**: Improve the app by reporting bugs and suggestions

## Disclaimer

LabNex is not associated with GitLab or any other entity. It is a standalone, independent project.

*All trademarks and logos are the properties of their respective owners.*
# Publishing Guide — GitHub & JetBrains Marketplace

## Part 1: Create the GitHub Repository

### Step 1: Create the repository

1. Go to [github.com/new](https://github.com/new)
2. **Repository name:** `dart-data-toolkit`
3. **Description:** `Generate copyWith, toJson, fromJson, toString and barrel files for Dart`
4. **Visibility:** Public
5. **Initialize with:** None (the project is already set up)
6. Click "Create repository"

### Step 2: Push the local code

```bash
git remote add origin https://github.com/moisesmartins-dev/dart-data-toolkit.git
git branch -M main
git push -u origin main
```

### Step 3: Configure the repository

Go to **Settings → About** and add the following topics:

```
dart  flutter  intellij-plugin  android-studio  code-generation  barrel-file
```

---

## Part 2: Prepare for the JetBrains Marketplace

### Step 1: Generate the build

```bash
./gradlew clean buildPlugin
# Output: build/distributions/dart-data-plugin-1.0.0.zip
```

### Step 2: Test locally

```bash
./gradlew runIde
```

Verify:
- `Ctrl+Alt+D` — opens the method generation dialog
- `Ctrl+Alt+J` — opens the JSON to Dart dialog
- `Ctrl+Alt+B` — opens the barrel file dialog
- `Alt+Insert` — shows individual actions in the Generate menu

---

## Part 3: Publish on the JetBrains Marketplace

### Step 1: Create an account

1. Go to [plugins.jetbrains.com](https://plugins.jetbrains.com)
2. Sign in or create a JetBrains account
3. Navigate to **My Plugins → Add plugin**

### Step 2: Fill in the listing details

**Plugin name**
```
Dart Data Toolkit
```
> 1–4 words, no generic suffixes like "Plugin" or "Tool". ✅

**Summary (40 characters — shown on Marketplace preview cards)**
```
Dart code generator for Flutter devs
```

**Description**

Use the description already configured in `plugin.xml`. It follows Marketplace best practices:
- No marketing adjectives ("powerful", "lightweight", "simple")
- Structured as bullet points covering real benefits
- Includes keyboard shortcuts
- Mentions nullable and generic type support

**Tags**

Select at least one relevant tag. Suggestions:
- `Code editing`
- `Code tools`

> Do not select categories the plugin does not fit.

**Vendor links** (configured in `plugin.xml`)
- Website: `https://github.com/moisesmartins-dev`
- Source code: `https://github.com/moisesmartins-dev/dart-data-toolkit`
- Bug tracker: `https://github.com/moisesmartins-dev/dart-data-toolkit/issues`

**License:** MIT (required for publishing)

### Step 3: Screenshots

- Minimum resolution: **1200 × 760 px**
- Show the real IntelliJ/Android Studio interface with the plugin in use
- Suggested captures:
  1. Method selection dialog (`Ctrl+Alt+D`)
  2. Generated output in the editor (before and after)
  3. JSON to Dart dialog with JSON pasted in
  4. Barrel file dialog with files selected
- No desktop backgrounds, browser windows, or third-party branding

### Step 4: Upload and submit

1. Upload the file: `build/distributions/dart-data-plugin-1.0.0.zip`
2. Fill in all fields and click **Submit**
3. Wait for review: estimated 2–7 business days
4. You will receive an email with the result

---

## Part 4: Maintenance

### Create a tag and release

```bash
git tag -a v1.0.0 -m "Release 1.0.0"
git push origin v1.0.0
```

GitHub automatically creates a release from the tag.

### Update the version

In `build.gradle.kts`:
```kotlin
version = "1.1.0"
```

In `CHANGELOG.md` and `CHANGELOG.pt-BR.md`, add a new entry following the existing format.

Rebuild and re-upload the updated `.zip` in the Marketplace admin panel.

---

## Publishing Checklist

### GitHub
- [ ] Remote added and code pushed
- [ ] Repository topics configured
- [ ] LICENSE file present (MIT)
- [ ] README in English as the main file

### JetBrains Marketplace
- [ ] Plugin tested locally (`./gradlew runIde`)
- [ ] Build generated (`./gradlew buildPlugin`)
- [ ] Screenshots prepared (min. 1200×760 px)
- [ ] 40-character summary written
- [ ] Vendor links filled in (website, source code, bug tracker)
- [ ] License specified
- [ ] Tags selected

### After publishing
- [ ] Monitor reviews and respond to feedback on the Marketplace
- [ ] Enable Discussions on GitHub for community support

---

## Troubleshooting

**Build failing**
```bash
./gradlew clean build
./gradlew --version
java -version  # requires JDK 17+
```

**Plugin not installing in the IDE**
- Check compatibility range: `since-build="233"` to `until-build="261.*"`
- Confirm the `.zip` file is not corrupted
- Try installing via **File → Settings → Plugins → gear → Install from Disk**

---

## Useful Links

- [JetBrains Marketplace — Best Practices for Listing](https://plugins.jetbrains.com/docs/marketplace/best-practices-for-listing.html)
- [IntelliJ Platform Plugin Documentation](https://plugins.jetbrains.com/docs/intellij/)
- [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)
- [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)

---

> Also available in [Português (pt-BR)](https://github.com/moisesmartins-dev/dart-data-toolkit/blob/main/PUBLISHING.pt-BR.md)

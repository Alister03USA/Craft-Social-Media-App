# android_unit2_3

### What is this example

A Volley example that contains the following example requests:

1. String GET request
StringRequest can be used to fetch any kind of string data. The response can be json, xml, html,text.

2. JsonObject POST request
Use JsonObjectRequest with JsonObject as Post Request Body.


### Important Notes

- Use `10.0.2.2` instead of `localhost` IF the server program is running on the same host as the Android Enmulator
- If gradle breaks down, Title bar-> Tools -> AGP Upgrade Assistant
        This will help sync your files and fix the version. Refer to the table below for gradle compatibility.


- AndroidManifest.xml
    - add `<uses-permission android:name="android.permission.INTERNET" />` before `<application>`
    - add `android:usesCleartextTraffic="true"` inside `<application>`

- build.gradle (Module :app)
    - add `implementation 'com.android.volley:volley:1.2.1'` inside `dependencies{}`, then sync gradle.


### Version Tested
|Android Studio            | Android SDK | Gradle | Gradle Plugin | Gradle JDK | Emulator (works on all the below)               |
|--------------------------|-------------|--------|---------------|------------|-------------------------------------------------|
|Ladybug 2024.2.2 Patch 1  |   35.3.12   | 8.12   |    8.8.1      |   17 & 21  | Pixel 3a API 34, Pixel 5 API 34, Pixel 7 API 32 |



Document Last Modified by: Dhvani Mistry
Document Last Modified on: Feb 24 2025
# Firebase Project Swap Instructions

How to swap the Firebase project for SafeHer AR (e.g., moving from a
test project to a production project).

## Prerequisites

- A Firebase project on the **Spark (free)** plan
- Android Studio installed
- The SafeHer AR source code

## Steps

### 1. Create a New Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click **Add Project**
3. Name it (e.g., `safeher-ar-prod`)
4. Disable Google Analytics if not needed (saves quota)
5. Click **Create Project**

### 2. Register the Android App

1. In the Firebase project, click **Add App → Android**
2. Enter package name: `com.phantomcrowd`
3. Enter app nickname: `SafeHer AR`
4. Download `google-services.json`

### 3. Replace google-services.json

```
# Delete the old file
del app\google-services.json

# Copy the new file
copy <downloaded-path>\google-services.json app\google-services.json
```

### 4. Enable Firestore

1. In Firebase Console → **Build → Firestore Database**
2. Click **Create Database**
3. Choose **Start in test mode** (or apply security rules below)
4. Select a region closest to your users

### 5. Apply Security Rules

In Firestore → Rules, paste:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Issues: anyone can read/write (anonymous reporting)
    match /issues/{issueId} {
      allow read, write: if true;
    }
    // Surface anchors: same
    match /surface_anchors/{anchorId} {
      allow read, write: if true;
    }
    // Authority actions: read-only for app users
    match /authority_actions/{actionId} {
      allow read: if true;
      allow write: if request.auth != null;
    }
  }
}
```

> **Note:** For production, restrict writes to authenticated users
> and validate data schemas in rules.

### 6. Add Composite Indexes

See `work/docs/firestore-indexes.txt` for the full list of recommended
composite indexes.

### 7. Rebuild the App

```bash
./gradlew clean assembleDebug
```

The new `google-services.json` will be picked up automatically by the
Google Services Gradle plugin.

### 8. Verify

1. Install the debug APK
2. Open the app and post a test report
3. Check Firebase Console → Firestore to confirm the document appears

## Free Tier Limits (Spark Plan)

| Resource            | Daily Limit   |
|---------------------|---------------|
| Firestore reads     | 50,000/day    |
| Firestore writes    | 20,000/day    |
| Firestore deletes   | 20,000/day    |
| Storage             | 1 GiB total   |
| Network egress      | 10 GiB/month  |
| Authentication      | Unlimited     |

These limits are sufficient for a hackathon demo and small-scale usage.

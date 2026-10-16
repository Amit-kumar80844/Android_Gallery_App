\# Project: Android Gallery App with Live Server Upload — Cybersecurity Demonstration



Act as a senior Android developer. Build a complete, polished Android application for a college cybersecurity assignment. The app should function as a photo gallery, display existing images stored on the phone, and upload user-authorized images to a separate Python FastAPI backend.



The application will be tested on 2–3 Android devices during a classroom demonstration.



\## 1. Technology Stack



\- Kotlin

\- Android Studio

\- Jetpack Compose

\- Material 3

\- Android Photo Picker and MediaStore APIs

\- Retrofit or OkHttp for networking

\- Kotlin Coroutines for asynchronous operations

\- Coil for image loading



Use a modern Android architecture with clear separation between UI, data, networking, and business logic.



\## 2. Gallery Interface



Create a polished, responsive gallery application with:



\- A home screen displaying locally available photos as a thumbnail grid.

\- Image previews with full-screen viewing.

\- Image selection with visible selection indicators.

\- A selection counter showing how many photos are selected.

\- Sorting by newest, oldest, and filename where available.

\- Loading, empty, permission-denied, and error states.

\- A settings screen containing the backend URL and connection status.

\- A demonstration screen showing upload progress and upload history.



Use a professional interface with Material 3, smooth navigation, appropriate spacing, and support for different screen sizes.



\## 3. Photo Access



\- Use Android's system Photo Picker for explicit image selection.

\- Where broader gallery browsing is required, use appropriate MediaStore APIs and request only the necessary Android permissions.

\- Do not request camera permission because camera capture is not required.

\- Do not access unrelated files or attempt to bypass Android storage restrictions.

\- Display thumbnails and previews without uploading images automatically.

\- Make the distinction between local photos and server-uploaded photos clear.



\## 4. Image Upload



Implement an upload workflow that:



1\. Lets the participant select one or more existing photos.

2\. Shows the selected images and their count.

3\. Displays a confirmation screen explaining that the selected photos will be sent to the configured demonstration server.

4\. Starts uploading only after the participant presses the Upload button.

5\. Sends each image to `POST /api/images/upload` using the backend's required multipart format and device authentication.

6\. Shows per-image progress, success or failure status, and retry options.

7\. Displays the server response and returned image ID.

8\. Refreshes the upload history after successful uploads.

9\. Handles timeouts, server disconnections, unsupported formats, and large files.

10\. Prevents duplicate uploads caused by accidental repeated button presses.



Never silently upload the entire photo library or upload images in the background.



\## 5. Backend Integration



Integrate with the separately developed FastAPI backend.



Expected endpoints:



\- `GET /api/health`

\- `POST /api/demo/register`

\- `POST /api/images/upload`

\- `GET /api/images`

\- `GET /api/dashboard/stats`



Read the backend's actual API documentation before implementing requests. Match its request fields, authentication headers, response schemas, and error formats exactly. Do not invent incompatible API contracts.



Allow the backend base URL to be configured for local development and classroom testing. Explain the difference between an Android emulator's localhost address and the host computer's LAN address.



\## 6. Device Identification and Authentication



\- Register each demonstration device using a generated demo identifier.

\- Use the backend's supported authentication mechanism.

\- Store credentials securely using Android Keystore-backed storage where appropriate.

\- Never hardcode administrator credentials, API secrets, or private keys into the application.

\- Allow the participant to disconnect or unregister the demonstration device if the backend supports it.

\- Do not use invasive device fingerprinting.



\## 7. Cybersecurity Demonstration



Add a clearly labeled educational section that demonstrates the privacy risks of image exfiltration.



Include:

\- A diagram of the image transfer process.

\- A log of authorized upload attempts and their outcomes.

\- The number of photos successfully uploaded by this device.

\- A simulated covert-transfer scenario using dummy images and simulated events.

\- An explanation of how unexpected network traffic and excessive media permissions can indicate privacy risks.



Clearly distinguish simulated activity from actual uploads.



\## 8. Reliability and Testing



Implement:

\- ViewModel-based UI state management.

\- Repository and API service layers.

\- Runtime permission handling.

\- Network connectivity and API error handling.

\- Image upload validation and sensible size limits.

\- Unit tests for selection and upload state management.

\- API integration tests against a test backend.

\- Clear error messages without exposing tokens or sensitive paths.



Test on at least one Android emulator and document how to test on physical devices connected to the same local network.



\## 9. Deliverables



Generate a complete, runnable Android Studio project with:



\- All Kotlin source files.

\- Gradle configuration.

\- Android manifest.

\- Compose screens and navigation.

\- API client, models, repositories, and ViewModels.

\- Image selection and upload implementation.

\- Unit and integration tests.

\- A README containing setup, build, installation, and troubleshooting instructions.



Build the app incrementally: first the gallery, then photo selection, then backend connectivity, then uploads, and finally the cybersecurity demonstration screen.



Do not return pseudocode or incomplete placeholder methods. Verify the API integration and fix build errors before declaring the project complete.


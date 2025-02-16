# Fire Chat

## Instant Messaging Android Application

Communicate seamlessly with friends and family using this instant messaging application built for Android. Enjoy real-time conversations, secure user authentication, and flexible profile management.

### Built with Kotlin and Firebase

This demo application is developed using Kotlin for the Android platform and leverages Google Firebase for its backend services. It provides a smooth and efficient chat experience with modern features.

### Key Features

- **Instant Messaging**: Send and receive messages in real-time, ensuring quick and responsive communication.
- **Email Sign-in**: Secure user registration and login using email addresses and passwords.
- **Anonymous Sign-in**: Get started quickly with the app using anonymous accounts, allowing users to explore before full registration.
- **Profile Linking**: Seamlessly link your anonymous profile to an email and password at any time, retaining your chat history and preferences.
- **Android 12+ Support**: Optimized for devices running Android 12 and above, ensuring compatibility with the latest Android features and security standards.
- **Firebase Backend**: Powered by Google Firebase for a robust, scalable, and reliable backend infrastructure.

### Firebase Services Used

- **Firebase Auth**: Manages user authentication, including email and anonymous sign-in, and profile linking.
- **Firestore**: A NoSQL document database to store chat messages, user profiles, and other application data in real-time.
- **Firebase Storage**: Used for storing media files such as images and videos shared within chats. _(Backlog)_

### Screenshots
_soon!_
### Upcoming Features

| Feature          | Description                                   | Status/Expected |
|-------------------|-----------------------------------------------|-----------------|
| **Voice Calls**  | Enable real-time voice communication between users. | *Backlog* |
| **Video Calls**  | Add support for video calls for face-to-face chats. | *Backlog*        |
| **Group Chats**  | Allow users to create and join group conversations. | *Planned*        |
| **File Sharing** | Enable users to share files (documents, images, etc.) | *Backlog*        |
| **Message Reactions**| Let users react to messages with emojis.         | *Backlog*        |
| **Themes & Customization** | Allow users to customize the app's appearance.   | *Planned*        |
| **End-to-End Encryption** | Implement E2EE for enhanced message privacy.      | *Considering*    |
| **Profile settings** | Profile picture and more settings.      | *In Development*    |

*Status indicators are subject to change.*

### Getting Started

This project is built using Android Studio. To run the application:

1.  Clone this repository to your local machine.
2.  Open the project in Android Studio.
3.  Ensure you have the Firebase SDK configured in your project as per Firebase documentation.
4.  Build and run the 'app' module on an Android emulator or a physical device running Android 12 or higher.

### Project Structure

This is a single-module DEMO Android application project. Longterm development is not intended.

- `app`: Contains all the source code, resources, and build configurations for the Android application.

### Technologies Used

- Kotlin
- Android SDK (API Level 31+)
- Firebase
    - Firebase Authentication
    - Cloud Firestore
    - Firebase Storage
## License

This Android application is licensed under the Apache License, Version 2.0.

Copyright 2025 Arash Ebrahimzadeh

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

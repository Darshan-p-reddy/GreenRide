🚖 Online Cab Booking Application (User Side)
A Kotlin-based mobile application that enables users to seamlessly book cabs online from their desired pickup location to destination. This is a user-side module for a cab-hailing service.

✨ Key Features
📍 Location-Based Booking: Select your current location and desired destination on the map to book a cab.

🔐 Authentication System:
Sign Up and Login functionality.
Firebase Authentication integration for secure user management.

🧭 Navigation Module:
Provides route information between selected source and destination.
Displays estimated time and distance.

🗺️ Main Map Module:
Integrated with Google Maps SDK (free tier).
Displays real-time location using GPS.
Marker placement for pickup/drop locations.

📦 Modules Overview
👤 Login & Signup Module
Built using Kotlin and integrated with Firebase.
Supports email/password-based authentication.
Handles user session management.

🧭 Navigation Module
Computes and renders routes on the map.
Uses Google Directions API (or similar) to generate navigation paths.

🗺️ Main Map Module
Interactive map interface.
Marker dragging and location pinning.
Displays cab movement simulation (optional/extendable).

🎨 UI/UX Design
Designed using Figma with a clean and modern user interface.
Intuitive user flow for easy onboarding and ride booking.
Mobile-first design with responsive layouts.

🛠️ Technologies Used
Language: Kotlin (Android)
Backend: Firebase Authentication
Maps: Google Maps SDK (Free Tier API)
Design: Figma (UI/UX Prototypes)

📌 Notes
Google Maps usage is currently limited to the free version of the API.
Future scope includes driver-side module, payment gateway integration, and push notifications.

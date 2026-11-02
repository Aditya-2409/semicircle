# SEMICIRCLE — Futuristic Personal AI Assistant

SEMICIRCLE is a native Android personal AI assistant for the Realme X7 5G built with Kotlin and Jetpack Compose. It features a sci-fi orange-and-black aesthetic inspired by futuristic computer terminals, animated cybernetic eyes that track faces via camera vision, a reactive audio visualizer mouth, real-time voice conversations (Gemini Live / Speech / TTS), exact GPS and Tamil Nadu weather telemetry with a live animated weather background, an autonomous low-battery sad sound sentinel, remote ADB phone-to-phone control, universal QR code transfer for any displayed text/content, and a self-aware AI assistant that monitors the live status of all 45 connectors.

## User Review & Critical Decisions

> [!IMPORTANT]
> **Clarification of "Terminal" Concept & Confirmed Identity:**
> - **What "Terminal" Means in SEMICIRCLE**: In this application, "terminal" refers to the **visual and conceptual aesthetic** inspired by sci-fi command terminals (such as spacecraft and advanced robotics HUDs), featuring deep OLED blacks (`#090909`), glowing electric orange accents (`#FF8A24`), telemetry readouts, and soundwave graphics.
> - **Primary Identity**: The app is defined and labeled across the interface as a **Personal AI Assistant with a sci-fi theme**—providing natural voice interaction, camera face tracking, live weather, and device control, rather than a raw command-line text window.
> - **Multi-Layout Switcher**: Users can select between 4 layout styles:
>   1. *Cyber Assistant (Default)*: Full sci-fi interface with centered cybernetic face, telemetry cards, and quick actions.
>   2. *Tactical HUD*: Combat-inspired telemetry layout with dual diagnostics.
>   3. *Minimal Face*: Distraction-free robot face presence with floating controls.
>   4. *Dock Station*: Split-pane workstation layout optimized for desk stands and dock mounts.

## 1. Overview & Core Concept

- **What It Does**: SEMICIRCLE is your autonomous sci-fi personal assistant. It communicates through real-time voice and streaming subtitles, visually tracks your face position with cybernetic eyes, visualizes speech through an animated equalizer mouth, delivers hyper-local weather telemetry for Chennai, Tamil Nadu, and GPS coordinates, generates QR codes for any on-screen text or image, monitors system health with autonomous low-battery audio alerts, controls other phones via ADB, and possesses full awareness of its 45 connectors and their live ON/OFF states.
- **Visual Design**: High-contrast OLED near-black (`#090909`), card surfaces (`#151515`), sci-fi orange neon accents (`#FF8A24` and `#FFB15A`), geometric grid lines, and interactive cybernetic canvas shaders.

## 2. User Experience & Visual Design

### Key User Flows

1. **AI Home Screen**:
   - **Header Bar**:
     - App Layout Mode Selector (*Cyber Assistant*, *Tactical HUD*, *Minimal Face*, *Dock Station*).
     - *Full Unrestricted Access* toggle.
     - *Start on Boot* toggle.
     - *Permanent Wake Lock* indicator.
     - *Enter Full Screen Mode* button.
   - **Central Cybernetic Face**:
     - Dual glowing orange eyes tracking the user's face coordinates.
     - Real-time multi-bar reactive audio visualizer mouth.
     - *Start Voice Conversation* button (activates STT/Gemini/TTS with streaming subtitles).
   - **Universal Content QR Action**:
     - Dedicated button to convert current AI speech text, subtitle, or answer into an instant scannable QR code modal.
   - **Live Weather Telemetry Card**:
     - Current Tamil Nadu / GPS temperature, humidity, wind, UV index, and weather condition with link to 24h hourly and 7-day forecast.
   - **Quick Actions Grid**: Alarms, Alerter, Cameras, Notes, and AI Image Generator.

2. **Dedicated Immersive Full Screen Mode**:
   - Clean, full-bleed sci-fi assistant interface with only the AI face.
   - Face-tracking eyes and pulsating audio visualizer mouth.
   - Prominent physical-styled button: **ACTIVATE / INTERRUPT** to start talking or immediately cut off speech.
   - Live subtitles HUD.
   - **Downloadable QR Code Overlay**: Converts current AI response or generated image into an on-screen QR code for instant smartphone scanning.
   - Distinct **EXIT FULL SCREEN** button in the top right.

3. **Remote ADB Device Controller (Device Manager)**:
   - Configure target phone IP and TCP port (default `5555`).
   - Pairing workflow with pairing code and pairing port.
   - Remote control panel: Home, Back, Power, Volume Up/Down, Screenshot, and Shell Command Terminal.
   - Security gate with explicit user confirmation before executing remote commands.

4. **Self-Aware Feature Knowledge Engine**:
   - The AI assistant actively knows all 45 features and their live ON/OFF and connection status.
   - If the user asks e.g. *"Check my cameras"* or *"Play Spotify"* or *"Send an SMS"* and the connector is disabled, the AI replies: *"The [Feature] connector is currently turned OFF. You can enable it in the Connectors hub or toggle Full Access on the Home Screen"*.

5. **Live Weather & Tamil Nadu Telemetry**:
   - Live animated weather background reacting to actual conditions (Clear amber embers, Rain streaks, Storm lightning grid, Cloud mist).
   - Exact GPS coordinates or instant Tamil Nadu city selector (Chennai, Coimbatore, Madurai, Tiruchirappalli, Salem, Tirunelveli, Vellore, Kanyakumari).
   - 24-hour horizontal hourly forecast and 7-day high/low forecasts.
   - Celsius (°C) and Fahrenheit (°F) conversion switch.

6. **Autonomous Battery Sentinel & Sad Sound**:
   - Background monitor checks battery percentage.
   - If battery <= 20% and not charging, AND `faceDetected == false` and `isSpeakingOrListening == false`:
     - Plays synthesized sad descending minor tone.
     - Animates cybernetic eyes to a sad droop.
     - Logs low-power event to audit trail.

7. **AI Image Generator with Downloadable QR Code**:
   - Prompt input for generating AI artwork or visual blueprints.
   - Displays generated visual with download options and renders an instant on-device QR code for sharing to other devices.

8. **Connectors Catalogue & Management**:
   - Searchable catalogue of 45 connectors across AI, Weather, Phone, Google Services, Media, Cameras, Smart Home, Devices, and Developer categories.
   - Every card has 3 distinct controls:
     - `[ON/OFF Switch]`: Enables or disables connector.
     - `[Connect / Disconnect Button]`: Starts authorization or tears down session.
     - `[Configure Button]`: Sets credentials or endpoints.

9. **Settings & Substitute API Keys**:
   - Add and manage backup API keys (Primary, Backup 1, Backup 2) with automatic quota failover on HTTP 429.
   - Permanent wake lock toggle and screen brightness management.
   - Privacy permissions overview and data export.

## 3. Technical Architecture & System Integration

```
┌────────────────────────────────────────────────────────────────────────┐
│                    SEMICIRCLE AI Assistant UI                          │
│  ┌───────────────────────┐  ┌──────────────────────────────────────┐   │
│  │     AI Home Screen    │  │       Immersive Full Screen Mode     │   │
│  │ - 4 Layout Switchers  │  │ - Full-bleed cybernetic eyes         │   │
│  │ - Eyes + Mouth HUD    │  │ - Audio visualizer mouth             │   │
│  │ - Full Access Toggle  │  │ - Tactile Activate/Interrupt button  │   │
│  │ - Start on Boot Switch│  │ - Generated Content & Download QR    │   │
│  │ - Content -> QR Modal │  │ - Exit Full Screen Button            │   │
│  └───────────┬───────────┘  └──────────────────┬───────────────────┘   │
│              │                                 │                       │
│  ┌───────────┴───────────┐  ┌──────────────────┴───────────────────┐   │
│  │ Connectors Catalogue  │  │ Weather Screen (Chennai / Tamil Nadu)│   │
│  │ (45 Modular Units)    │  │ + Live Animated Background           │   │
│  └───────────┬───────────┘  └──────────────────┬───────────────────┘   │
│              │                                 │                       │
│  ┌───────────┴───────────┐  ┌──────────────────┴───────────────────┐   │
│  │ Image Gen + QR View   │  │ Device Manager (ADB Remote Phone,    │   │
│  │                       │  │ ONVIF Cameras, Bluetooth, Cast)      │   │
│  └───────────┬───────────┘  └──────────────────┬───────────────────┘   │
└──────────────┼─────────────────────────────────┼───────────────────────┘
               │                                 │
┌──────────────▼─────────────────────────────────▼───────────────────────┐
│                        Connector & System Core                         │
│  - ConnectorManager (45 Connectors + Master Switch + Full Access)      │
│  - AdbRemoteController (Remote Wi-Fi/TCP ADB Device Control)           │
│  - FeatureAwarenessEngine (Supplies live ON/OFF state to AI prompt)    │
│  - BatterySentinel (Low Battery + No Face + No Conversation Sad Sound) │
│  - WeatherRepository (Open-Meteo GPS / Tamil Nadu + Live BG States)    │
│  - VoiceAssistantManager (SpeechRecognizer + Gemini + TTS Interruption)│
│  - VisionFaceTracker (CameraX Face Position Extraction)                │
│  - UniversalQrCodeGenerator (Vector QR Matrix for Any Text / Image)    │
│  - ApiKeyManager (Quota Exhaustion Failover)                           │
└────────────────────────────────────────────────────────────────────────┘
```

## 4. Verification & Test Status

- **Compilation**: Verified via `compile_applet` with `BUILD SUCCESSFUL`.
- **Local Robolectric & Unit Tests**: Passing 100% via `:app:testDebugUnitTest`.
- **Resource & Icon Validation**: Custom SEMICIRCLE adaptive launcher icon configured with cybernetic visor emblem and OLED black/orange palette.

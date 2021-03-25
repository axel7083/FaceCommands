# FaceCommands

This application can displays gestures such as scrolling according to facial expression. In particular it allows to detect single eye blinking. By default, blinking with the left eye will scroll down, and blinking with the right eyes will scroll up. 

This application was design to use Tiktok without hands. Allowing to scroll without touching your screen.

# Under the hood

The application is using the [Mobile Vision API](https://developers.google.com/vision/android/getting-started) to detect facial expression, an [Accessibility service](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService) to display the gestures and the camera to get the image input.

TODO:
- [ ] Smiling interaction

# Event Journal App

## App Overview

As part of my Mobile Application Development module I developed an “Event Journal” app. This app acts as a digital journal where users can document their events / experiences (e.g. birthdays, holidays, parties) to look back on.

I developed this app using Kotlin and Android Studio by combining practical lab content from class, self-learning, and independent research.

## App Functionality

- A splash screen that displays the apps custom logo before proceeding to the main content.
- User authentication functionality to allow users to register and log into their accounts via email and password OR via Google Authentication.
- Users can add, view, edit, and delete (single OR all) events.
- When adding an event, users can input various details including event names, types,  dates, times, descriptions, budgets/ event cost information, and images.
- Users can search for events by event name.
- Users have the option to favourite events.
- Users can toggle/filter between viewing all events, viewing only their favourite events
- and/or viewing only past events.
- Users have the option to update their profile picture to their own photo.
- Integrated maps functionality, enabling users to view their event locations via a Google Maps API.
- Maps can be filtered to show all events, favourited events and/or past events.

## Persistence

All app data is persisted to Firebase. Firebase handles the apps user authentication, allowing
users to sign in with their email/password or Google account. Event data is stored in a Firebase
Realtime Database using a custom schema. This schema connects each event with its
corresponding user, making it easier to manage and find event information for each user.
Images that are uploaded to the app (i.e. event photos or profile pictures) are stored on Firebase
Storage.

## GitHub workflow

The GitHub workflow used throughout the labs and assignment was:

_Branch - Commit(s) - Push - Pull Request - Merge - Delete Branch_## User Interface

## User Interface

### Colour Theme

The colour theme of this app is white, black and different shades of blue.

### App Screens

#### Login/Sign-Up Screen

![Screenshot 2024-05-03 at 18 49 16](https://github.com/emmaroche/EventJournalApp/assets/78028777/b72fed6e-1130-422f-948a-ebf91dece6b8)

#### App Navigation

![Screenshot 2024-05-03 at 18 45 37](https://github.com/emmaroche/EventJournalApp/assets/78028777/4454a64d-5675-4984-b824-4c46e3287689)

#### Home Screen (with events added)

![Screenshot 2024-05-03 at 18 41 40](https://github.com/emmaroche/EventJournalApp/assets/78028777/873bf0b9-3779-4d63-9363-7566bcf942a2)

#### Add an Event Screen

![Screenshot 2024-05-03 at 18 42 13](https://github.com/emmaroche/EventJournalApp/assets/78028777/675262dc-c463-4877-af84-10328cfd646f)
![Screenshot 2024-05-03 at 18 42 23](https://github.com/emmaroche/EventJournalApp/assets/78028777/b8f7ff4d-c8b1-4887-9867-a0f3aa1c0802)

#### View Event Details

![Screenshot 2024-05-03 at 18 46 25](https://github.com/emmaroche/EventJournalApp/assets/78028777/96d67066-e19c-4f18-85cd-3cb78e4773f2)
![Screenshot 2024-05-03 at 18 47 08](https://github.com/emmaroche/EventJournalApp/assets/78028777/3d2a26f7-d836-427d-bfd6-f9fd739a2788)

#### Favourite Filter active 

![Screenshot 2024-05-03 at 18 43 25](https://github.com/emmaroche/EventJournalApp/assets/78028777/0d6c2cf5-d5fd-4245-a36c-61de45fe5211)

#### Past Events Filter active 

![Screenshot 2024-05-03 at 18 43 11](https://github.com/emmaroche/EventJournalApp/assets/78028777/7cd6eb10-ba8f-4dd6-ae54-f7be06a4b299)

#### Viewing Event on Map (there are also options to filter map view to display only past events or favourite events)

![Screenshot 2024-05-03 at 18 44 03](https://github.com/emmaroche/EventJournalApp/assets/78028777/9aa45486-2fd1-4720-8c89-67936464ddb7)

#### About us Screen

![Screenshot 2024-05-03 at 18 44 55](https://github.com/emmaroche/EventJournalApp/assets/78028777/811d24f5-b72f-40c6-b2bc-b5ba5b9ebb92)



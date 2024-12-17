# CheckBall

**The ultimate pickup basketball app for hoopers, by hoopers**

CheckBall is an app built with Jetpack Compose designed to help pickup basketball players find courts in their neighborhood or city, see who is playing where, and record what happens on the court.

## Features

- **Home Screen:**  
  Displays basketball courts in the user's vicinity on a Google Map view, with courts represented as basketball icons. The app automatically fetches courts based on the current map view, which users can navigate by dragging, pinching, zooming, etc.

- **Courts Available Tab:**  
  Provides a list view of courts in the area, allowing users to browse easily.

- **Court Page:**  
  When a court is selected, users can view detailed information including:
    - Address
    - Photos
    - Directions button to open the maps app
    - "I Got Next" button to indicate their presence at the court
    - "I'm Out" button for when they leave
    - Real-time list of current players at the court

- **History Screen:**  
  Allows users to log and view their pickup basketball match history with in-depth stats. Users can input details such as opponents, scores, and game stats, which are then presented in a clean match feed.

- **Chat Screen:**  
  Users can post messages on court-specific threads to communicate with other hoopers who may be going or are already present. Features include:
    - Adding likes to posts
    - Viewing profiles of post authors

- **Profile Screen:**  
  Users can update their player information, including:
    - Height
    - Weight
    - Position
    - Favorite court
    - Recent game stats
    - CheckBall badges earned

## User Login/Authentication

CheckBall utilizes **Firebase Authentication** to manage user sign-in and sign-up processes. Users can easily log in or create an account using their Google accounts, ensuring a secure and seamless authentication experience.

## Database

The app's data is managed using **Firestore**, a flexible, scalable NoSQL cloud database. Firestore handles various data models including:
- Users
- Parks (courts)
- Matches

This setup ensures real-time data synchronization and reliable data storage for all app functionalities.

## APIs

CheckBall integrates with the following APIs to enhance its functionality:

- **Google Maps API:**  
  Provides interactive map features, allowing users to navigate and view court locations seamlessly.

- **Google Places API:**  
  Fetches detailed information about local basketball courts, including their locations, photos, and other relevant details.

These integrations enable users to discover and interact with basketball courts effectively within the app.

## Technologies Used

- **Jetpack Compose:**  
  Modern toolkit for building native Android UI.

- **Firebase:**  
  Comprehensive app development platform for authentication and real-time database management.

- **Firestore:**  
  NoSQL cloud database for storing and syncing app data.

- **Google Maps & Places API:**  
  Essential for map functionalities and fetching detailed court information.




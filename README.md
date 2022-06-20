CAPSTONE App Design Project - README 
===

# WRK

## Table of Contents
1. [Overview](#Overview)
2. [Product Spec](#Product-Spec)
3. [Wireframes](#Wireframes)
4. [Schema](#Schema)

## Overview
### Description
An app to create, edit, and post workouts for yourself and others to see and use.

### App Evaluation
- **Category:** Fitness
- **Mobile:** Uniquely mobile as it uses location, GoogleMaps API and can send push notifications for when friends finish or post a workout.
- **Story:** Adds value to the fitness industry by allowing people of all levels to share their work and tips with others who may not know which exercises to do, how many sets to do, etc.
- **Market:** Targets the fitness community which is fairly large.
- **Habit:** Can be addictive/habit forming just like going to the gym. Track every time you workout, track your daily streak for working out. Can view other people's streaks.
- **Scope:** Fairly challenging as there are many moving parts. Includes working with a database to store workouts, GoogleMaps API, building/editing workouts, sharing workouts.

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**

* User can register for an account
* User can login
* User can view previous workouts
* User can build/edit workouts
* User can track workout in real time
* User can share workouts to feed
* User can find nearby gym

**Optional Nice-to-have Stories**

* User can create custom exercises
* User can click on workout and start/edit to make their own
* User can view extended details of each workout
* User can add friends
* User can send a workout to a friend
* User can search for full workouts
* User can post when personal records are hit
* User can view friends profile and their previous workouts

### 2. Screen Archetypes

* Login
    * User can login
* Register
    * User can register for an account
* Profile
    * User can view previous workouts
* Stream
    * User can share workouts to feed
* Creation
    * User can build/edit workouts
* Detail
    * User can track workout in real time
* Detail
    * User can find nearby gyms

### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Profile
* Workout Builder
* Community Feed


**Flow Navigation** (Screen to Screen)

* Login / Register
    * Community Feed
* Community Feed
    * *Future option - Details of workout* 
* Profile
    * Detail - View previous workouts
    * Creation - Workout Builder
    * Detail - Find nearby gyms
* Workout Builder
   * Detail - Real-time tracker
   * Profile - For if you want to save workout for later
* Real-Time Tracker
    * Stream - Community feed (after posting workout)

## Wireframes
<img src="https://raw.githubusercontent.com/JCGarzaa/WRK/d7343b9f13c272398cd894a465773cfadab5ca80/Wireframe.jpg" width=600>

## Schema 
### Models
#### Exercise

   | Property      | Type     | Description |
   | ------------- | -------- | ------------|
   | objectId      | String   | unique id for the user post (default field) |
   | bodyPart       | String   | Targeted body part |
   | name         | String   | name of exercise |
   | createdAt     | DateTime | date when post is created (default field) |
   | updatedAt     | DateTime | date when post is last updated (default field) |
   
#### WorkoutPerformed

   | Property      | Type     | Description |
   | ------------- | -------- | ------------|
   | objectId      | String   | unique id for the user post (default field) |
   | user    | Pointer   | Points to user that completed workout |
   | workout | Pointer   | Points to the workout template that was completed |
   | createdAt     | DateTime | date when post is created (default field) |
   | updatedAt     | DateTime | date when post is last updated (default field) |

#### WorkoutTemplate

   | Property      | Type     | Description |
   | ------------- | -------- | ------------|
   | objectId      | String   | unique id for the user post (default field) |
   | components       | Array   | array of WorkoutComponents |
   | title         | String   | title of workout |
   | components | Array       | Array of exercises in the workout
   | createdAt     | DateTime | date when post is created (default field) |
   | updatedAt     | DateTime | date when post is last updated (default field) |

#### WorkoutComponent

   | Property      | Type     | Description |
   | ------------- | -------- | ------------|
   | objectId      | String   | unique id for the user post (default field) |
   | exercise      | Pointer  | pointer to an individual exercise |
   | sets          | Number   | number of sets to complete for the exercise |
   | reps          | Number   | number of reps to complete per set |
   | weight        | Number   | amount of weight (lbs) for a set |
   | createdAt     | DateTime | date when post is created (default field) |
   | updatedAt     | DateTime | date when post is last updated (default field) |

#### User

   | Property      | Type     | Description |
   | ------------- | -------- | ------------|
   | objectId      | String   | unique id for the user post (default field) |
   | username      | String   | username for user login (default field) |
   | password      | String   | password for user login (default field) |
   | name          | String   | name of user |
   | email         | String   | email of user (default field) |
   | profilePic         | File     | profile picture for user |
   | streak       | Number   | day streak of workout in arow |
   | lastWD        | String   | date of the most recent workout |
   | createdAt     | DateTime | date when post is created (default field) |
   | updatedAt     | DateTime | date when post is last updated (default field) |

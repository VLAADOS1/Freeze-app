# API Documentation for AI Integration

**Base URL**: `http://127.0.0.1:8000`

This document details the request and response structures for the API. Use this to construct correct JSON bodies and parse responses.

---

## 1. AI (Public)

### Ask AI (Replicate Proxy)
**Endpoint**: `POST /ask_ai/`
*   **Description**: Generate text using DeepSeek-v3.1 (via Replicate). Public access.
*   **Request Body** (JSON):
    ```json
    {
        "prompt": "Tell me a joke",
        "system_prompt": "You are a funny bot" // Optional, default: "You are a helpful assistant"
    }
    ```
*   **Response** (JSON): Returns the completed Replicate prediction object. The generated text is in the `output` field (array of strings or string).

---

## 2. Authentication

**Endpoint**: `POST /token`
*   **Description**: Obtain a Bearer token for authenticated requests.
*   **Content-Type**: `application/x-www-form-urlencoded`
*   **Body Fields**:
    *   `username` (string, required): User's login.
    *   `password` (string, required): User's password.
*   **Response** (JSON):
    ```json
    {
        "access_token": "eyJhbGciOi...",
        "token_type": "bearer"
    }
    ```
*   **Usage**: Include in headers for all secured endpoints: `Authorization: Bearer <access_token>`

---

## 2. User Management

### Create User
**Endpoint**: `POST /users/`
*   **Description**: Register a new user.
*   **Required Role**: Public (or Admin, depending on policy).
*   **Request Body** (JSON):
    ```json
    {
        "username": "string (unique)",
        "password": "string (required)",
        "role": "user",  // or "admin"
        "telegram_id": "string (optional)",
        "name": "string (optional)",
        "mainname": "string (optional)",
        "income": 0.0,
        "weakness": "string (optional)",
        "goal_name": "string (optional)",
        "goal_amount": 0.0,
        "goal_date": "2023-12-31T23:59:59 (ISO 8601)",
        "saved_for_goal": 0.0,
        "two_factor_auth": false,
        "analytics_enabled": true,
        "communication_style": "string (optional)",
        "user_prompt": "string (optional)",
        "monthly_savings": 0.0,
        "self_ban": "string (optional)"
    }
    ```
*   **Response** (JSON): Returns the created User object (see "Get Current User" response).

### Get Current User (Me)
**Endpoint**: `GET /users/me`
*   **Header**: `Authorization: Bearer <token>`
*   **Response** (JSON):
    ```json
    {
        "username": "user1",
        "role": "user",
        "telegram_id": "12345",
        "name": "John Doe",
        "mainname": "Johnny",
        "income": 5000.0,
        "weakness": "Sweets",
        "goal_name": "Car",
        "goal_amount": 20000.0,
        "goal_date": "2024-01-01T00:00:00",
        "saved_for_goal": 1500.0,
        "two_factor_auth": false,
        "analytics_enabled": true,
        "communication_style": "Friendly",
        "user_prompt": "Be nice",
        "monthly_savings": 500.0,
        "self_ban": "Gambling",
        "id": 1,
        "registered_at": "2023-10-27T10:00:00"
    }
    ```

### Update User
**Endpoint**: `PUT /users/{user_id}`
*   **Header**: `Authorization: Bearer <token>`
*   **Request Body** (JSON): Send only the fields you want to update.
    ```json
    {
        "name": "New Name",
        "income": 6000.0,
        "password": "new_secure_password" // Optional
        // ... any other UserBase field
    }
    ```

### Delete User
**Endpoint**: `DELETE /users/{user_id}`
*   **Header**: `Authorization: Bearer <token>`

### Get All Users
**Endpoint**: `GET /users/`
*   **Header**: `Authorization: Bearer <token>`
*   **Description**: Retrieve a list of all registered users. **Admin only.**
*   **Response** (JSON): List of User objects (same format as "Get Current User").
*   **Status Codes**:
    *   `200`: Success.
    *   `401`: Unauthorized.
    *   `403`: Forbidden (Not an admin).
    *   `500`: Internal Server Error.

---

## 7. Error Handling

The API uses standard HTTP status codes to indicate the success or failure of a request.
When an error occurs, the response body will typically contain a JSON object with a `detail` field describing the error.

**Error Response Structure**:
```json
{
    "detail": "Error description here"
}
```

**Common Status Codes**:
*   `200 OK`: Request succeeded.
*   `201 Created`: Resource successfully created.
*   `400 Bad Request`: Invalid input (e.g., username already exists).
*   `401 Unauthorized`: Authentication credential is missing or invalid.
*   `403 Forbidden`: Authenticated user does not have access permissions.
*   `404 Not Found`: Resource not found.
*   `500 Internal Server Error`: An unexpected error occurred on the server.

---

## 3. Savings (Economy)

### Add Saving Entry
**Endpoint**: `POST /savings/`
*   **Header**: `Authorization: Bearer <token>`
*   **Description**: Log a saving action or a breakdown.
*   **Request Body** (JSON):
    ```json
    {
        "item_name": "Coffee",
        "date": "2023-10-27T10:30:00",
        "amount": 5.50,
        "is_breakdown": false // Set true if user failed/spent money they shouldn't have
    }
    ```
*   **Response** (JSON):
    ```json
    {
        "item_name": "Coffee",
        "date": "2023-10-27T10:30:00",
        "amount": 5.50,
        "is_breakdown": false,
        "id": 10,
        "user_id": 1
    }
    ```

### Get My Savings
**Endpoint**: `GET /savings/`
*   **Header**: `Authorization: Bearer <token>`
*   **Response** (JSON): List of Saving objects (as above).

---

## 4. Freeze Items (Self-Control)

### Add Freeze Item
**Endpoint**: `POST /freezes/`
*   **Header**: `Authorization: Bearer <token>`
*   **Description**: Freeze an urge or item for a duration.
*   **Request Body** (JSON):
    ```json
    {
        "item_name": "Video Games",
        "start_time": "2023-10-27T12:00:00",
        "duration_seconds": 3600, // Duration in seconds
        "amount": 99.99, // Cost of the item
        "is_frozen": true // or null/false
    }
    ```
*   **Response** (JSON):
    ```json
    {
        "item_name": "Video Games",
        "start_time": "2023-10-27T12:00:00",
        "duration_seconds": 3600,
        "amount": 99.99,
        "is_frozen": true,
        "id": 5,
        "user_id": 1
    }
    ```

### Get My Freezes
**Endpoint**: `GET /freezes/`
*   **Header**: `Authorization: Bearer <token>`
*   **Response** (JSON): List of FreezeItem objects (as above).

### Delete Freeze Item
**Endpoint**: `DELETE /freezes/{freeze_id}`
*   **Header**: `Authorization: Bearer <token>`
*   **Response** (JSON): `{"ok": true}`

## 5. Achievements

### List All Achievements
**Endpoint**: `GET /achievements/`
*   **Header**: `Authorization: Bearer <token>`
*   **Response** (JSON):
    ```json
    [
        {
            "name": "Saver Novice",
            "description": "Save $100",
            "photo_url": "http://...",
            "target_value": 100,
            "id": 1
        }
    ]
    ```

### Create Achievement (Admin Only)
**Endpoint**: `POST /achievements/`
*   **Header**: `Authorization: Bearer <token>`
*   **Request Body** (JSON):
    ```json
    {
        "name": "Saver Novice",
        "description": "Save $100",
        "photo_url": "image.png",
        "target_value": 100
    }
    ```

### Update My Achievement Progress
**Endpoint**: `POST /users/me/achievements/{achievement_id}`
*   **Header**: `Authorization: Bearer <token>`
*   **Query Parameter**: `current_value` (integer)
*   **Example URL**: `POST /users/me/achievements/1?current_value=50`
*   **Response** (JSON):
    ```json
    {
        "user_id": 1,
        "achievement_id": 1,
        "current_value": 50,
        "id": 1
    }
    ```

### Get My Achievements Status
**Endpoint**: `GET /users/me/achievements`
*   **Header**: `Authorization: Bearer <token>`
*   **Response** (JSON): List of UserAchievement objects showing your progress.

---

## 6. Groups & Challenges

### Create Group
**Endpoint**: `POST /groups/`
*   **Header**: `Authorization: Bearer <token>`
*   **Request Body** (JSON):
    ```json
    {
        "name": "Super Team", // Required
        "goal_name": "Vacation", // Optional
        "goal_target_amount": 1000.0, // Optional (default 0)
        "goal_date": "2024-06-01T00:00:00" // Optional (ISO 8601)
    }
    ```
*   **Response**: Returns created Group object with `id`.

### List All Groups
**Endpoint**: `GET /groups/`
*   **Header**: `Authorization: Bearer <token>`
*   **Response**: List of all groups.

### My Groups
**Endpoint**: `GET /groups/my`
*   **Header**: `Authorization: Bearer <token>`
*   **Response**: List of groups the current user is a member of.

### Get User's Groups
**Endpoint**: `GET /users/{user_id}/groups`
*   **Header**: `Authorization: Bearer <token>`
*   **Response**: List of groups the specified user is a member of.

### Get Group Details
**Endpoint**: `GET /groups/{group_id}`
*   **Header**: `Authorization: Bearer <token>`
*   **Response**: Group object.

### Update Group
**Endpoint**: `PUT /groups/{group_id}`
*   **Header**: `Authorization: Bearer <token>`
*   **Request Body** (JSON): Send only the fields you want to update.
    ```json
    {
        "name": "New Team Name", // Optional
        "goal_name": "New Goal", // Optional
        "goal_target_amount": 2000.0, // Optional
        "goal_date": "2024-12-01T00:00:00" // Optional
    }
    ```
*   **Response**: Updated Group object.

### Add Member to Group
**Endpoint**: `POST /groups/{group_id}/members/{user_id}`
*   **Header**: `Authorization: Bearer <token>`
*   **Description**: Add a user to the group by their User ID.
*   **Response**: `{"ok": true}` (or message if already member).

### Update Member Savings
**Endpoint**: `PUT /groups/{group_id}/members/{user_id}`
*   **Header**: `Authorization: Bearer <token>`
*   **Request Body** (JSON):
    ```json
    {
        "saved_amount": 500.0
    }
    ```
*   **Response**: `{"ok": true, "saved_amount": 500.0}`

### Save for Group Goal
**Endpoint**: `POST /groups/{group_id}/save?amount={amount}`
*   **Header**: `Authorization: Bearer <token>`
*   **Query Parameter**: `amount` (float, required). **Do not use JSON body.**
*   **Example URL**: `POST /groups/1/save?amount=50.0`
*   **Response**: `{"ok": true, "saved_amount": 50.0}`

### Get Group Members
**Endpoint**: `GET /groups/{group_id}/members`
*   **Header**: `Authorization: Bearer <token>`
*   **Response**: List of members with saved amounts.
    ```json
    [
        {
            "user_id": 1,
            "username": "user1",
            "name": "Name",
            "saved_for_group": 100.0
        }
    ]
    ```

### Create Group Challenge
**Endpoint**: `POST /groups/{group_id}/challenges/`
*   **Header**: `Authorization: Bearer <token>`
*   **Request Body** (JSON):
    ```json
    {
        "name": "No Sugar", // Required
        "end_date": "2023-11-01T00:00:00", // Optional (ISO 8601)
        "target_amount": 100.0 // Optional
    }
    ```
*   **Response**: Created Challenge object.
    ```json
    {
        "id": 1,
        "name": "No Sugar",
        "end_date": "2023-11-01T00:00:00",
        "target_amount": 100.0,
        "group_id": 1,
        "created_at": "2023-10-25T10:00:00"
    }
    ```

### Get Group Challenges
**Endpoint**: `GET /groups/{group_id}/challenges`
*   **Header**: `Authorization: Bearer <token>`
*   **Response**: List of challenges for the group.

### Delete Challenge
**Endpoint**: `DELETE /challenges/{challenge_id}`
*   **Header**: `Authorization: Bearer <token>`
*   **Response**: `{"ok": true}`

### Update Challenge Progress
**Endpoint**: `POST /challenges/{challenge_id}/progress?amount={amount}`
*   **Query Parameter**: `amount` (float, required). **Do not use JSON body.**
*   **Example URL**: `POST /challenges/10/progress?amount=5.0`
*   **Response**: `{"ok": true, "current_amount": 5.0}`

### Get Group Challenges
**Endpoint**: `GET /groups/{group_id}/challenges`
*   **Response**: List of challenges for the group.


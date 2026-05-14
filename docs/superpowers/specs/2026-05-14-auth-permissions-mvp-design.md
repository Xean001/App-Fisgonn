# FISGON Auth And Permissions MVP Design

## Goal

Deliver the Lead Mobile Developer and UX/Security scope for the MVP with the smallest functional implementation:

- RF01: keep normal login/register, but issue JWTs that use a rotating UUID per authenticated session instead of exposing the stable `userId` as the main token identity.
- RNF01: generate a new session UUID on every successful login/register.
- RNF05: add a simple Android permission flow for camera, fine location, and background location.
- Navigation: define a clear Compose flow from authentication into permissions and then Home.

## Decisions

- No anonymous guest mode for this MVP. The user authenticates with the existing email/password flow.
- A new `sessionUuid` is generated once per successful login/register.
- Token renewal and refresh tokens are out of scope for this MVP.
- Android permissions are requested after authentication and before Home.
- Permission denial does not block Home. The user may continue with visible limited-functionality warnings.

## Backend Design

Add an `auth_sessions` persistence model:

- `session_uuid`: UUID, primary public session identifier.
- `user_id`: internal reference to the authenticated user.
- `created_at`: session creation timestamp.
- `expires_at`: JWT/session expiration timestamp.

On `/auth/register` and `/auth/login` success:

1. Authenticate or create the user as the backend already does.
2. Generate `sessionUuid = UUID.randomUUID()`.
3. Compute `expiresAt` from the existing JWT TTL.
4. Store `sessionUuid -> userId` in `auth_sessions`.
5. Create the JWT with `sessionUuid` as the identity claim.
6. Return the existing `AuthResponse` shape: `user`, `token`, `expiresAt`.

The mobile contract stays stable. The app still receives the same response fields, but the JWT no longer exposes the stable user id as its principal claim.

## Mobile Auth Design

The existing `AuthRepository`, `LoginViewModel`, and `RegisterViewModel` can remain mostly unchanged because the response contract does not change.

The mobile app should treat the JWT as an opaque string. It should not parse the token for identity.

For MVP, token persistence is optional and not required for this scope. The session can remain in memory until app restart.

## Android Permissions Design

Declare these permissions in `AndroidManifest.xml`:

- `android.permission.CAMERA`
- `android.permission.ACCESS_FINE_LOCATION`
- `android.permission.ACCESS_BACKGROUND_LOCATION`

Add an Android-specific permission controller that exposes common-state data to Compose:

- camera permission status
- fine location permission status
- background location permission status
- request camera permission
- request fine location permission
- request background location permission

Background location should only be requested after fine location is granted.

## UI Flow

Navigation becomes:

```text
Login/Register -> Permissions -> Home
```

Add `AppScreen.Permissions(user)` between authentication and Home.

`PermissionsScreen` shows:

- a row/status for Camera
- a row/status for Fine Location
- a row/status for Background Location
- request buttons for missing permissions
- a Continue button that always lets the user enter Home
- a warning when one or more permissions are denied or missing

This keeps the MVP demonstrable without trapping the user in Android permission edge cases.

## Error Handling

- If login/register fails, keep the existing auth error handling.
- If a permission is denied, show a limited-functionality warning.
- If background location cannot be requested because fine location is missing, disable that action and explain it in the UI.
- If Android returns permanently denied behavior, keep the MVP simple: show denied status and let the user continue.

## Testing And Verification

Backend:

- Register returns a valid JWT and creates an auth session.
- Login returns a valid JWT and creates a new auth session each time.
- Repeated logins for the same user produce different `sessionUuid` values.
- Existing `AuthResponse` fields still deserialize on mobile.

Android/mobile:

- App builds with the new permissions in the manifest.
- Login/register success navigates to Permissions.
- Continue navigates to Home even when permissions are missing.
- Camera and fine location permission buttons trigger Android permission dialogs.
- Background location request is only available after fine location is granted.

## Out Of Scope

- Guest login.
- Refresh tokens.
- Automatic session restoration after app restart.
- Google Sign-In native implementation.
- Blocking Home when permissions are denied.
- Full Settings deep-link flow for permanently denied permissions.

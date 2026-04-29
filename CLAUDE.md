# FISGÓN — App de Seguridad Activa

Proyecto **Kotlin Multiplatform** (KMP) con **Compose Multiplatform** para Android e iOS.

## Stack principal

| Herramienta | Versión |
|---|---|
| Kotlin | 2.3.20 |
| Compose Multiplatform | 1.10.3 |
| Material3 | 1.10.0-alpha05 |
| Lifecycle / ViewModel | 2.10.0 |

## Estructura del proyecto

```
composeApp/src/
├── commonMain/kotlin/com/example/fisgon/
│   ├── domain/                         ← Capa de dominio (puro Kotlin, sin Android)
│   │   ├── entity/LoginCredentials.kt  ← Datos que viajan entre capas
│   │   ├── repository/AuthRepository.kt← Contrato (interfaz) de autenticación
│   │   └── usecase/LoginUseCase.kt     ← Regla de negocio del login
│   ├── data/                           ← Capa de datos
│   │   └── repository/AuthRepositoryImpl.kt ← Implementación real (o mock)
│   ├── presentation/                   ← Capa de presentación
│   │   └── login/
│   │       ├── LoginUiState.kt         ← Estado inmutable de la pantalla
│   │       ├── LoginViewModel.kt       ← Lógica de la UI
│   │       └── LoginScreen.kt          ← Pantalla Compose
│   └── App.kt                          ← Punto de entrada composable
├── androidMain/                        ← Código exclusivo Android
└── iosMain/                            ← Código exclusivo iOS
```

## Arquitectura: Clean Architecture

```
UI (LoginScreen)
    ↓ observa
ViewModel (LoginViewModel)
    ↓ ejecuta
UseCase (LoginUseCase)
    ↓ llama
Repository interface (AuthRepository)
    ↑ implementado por
AuthRepositoryImpl (capa de datos)
```

**Regla de dependencias:** Las capas internas nunca importan capas externas.
- `domain` no sabe nada de `data` ni de `presentation`
- `presentation` habla con `domain` a través del UseCase y la interfaz del repositorio

## Cómo correr

```bash
# Android
./gradlew :composeApp:assembleDebug

# iOS — abrir iosApp/iosApp.xcodeproj en Xcode
```

## Pantalla de Login

- Fondo oscuro marino (`#090E1C`)
- Escudo dibujado con Canvas + ícono de ojo (teal `#00C9A0`)
- Campos: email y contraseña con toggle de visibilidad
- Botón principal: "Iniciar Sesión"
- Botón social: "Iniciar con Google"

## Próximos pasos sugeridos

- [ ] Conectar `AuthRepositoryImpl` a un backend real (Firebase Auth, API REST, etc.)
- [ ] Implementar Google Sign-In nativo (requiere código platform-specific en androidMain/iosMain)
- [ ] Agregar pantalla de Home después del login exitoso (navegación)
- [ ] Agregar pantalla de "Olvidé mi contraseña"

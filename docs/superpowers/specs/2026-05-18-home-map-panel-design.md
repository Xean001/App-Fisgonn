# Home map + panel inferior (taxi) — diseño

## Objetivo
Hacer que el mapa sea el elemento principal de la pantalla Home, manteniendo un panel inferior fijo tipo “app de taxi” con el formulario de reporte y un botón SOS de pánico anclado en la esquina superior derecha del panel.

## Alcance
- Pantalla Home (UI).
- Reacomodo de layout: mapa dominante y panel inferior fijo.
- Mantener lógica existente de pánico y reportes.
- Agregar campo de descripcion (si no existe) al formulario de reporte.

## Fuera de alcance
- Cambios de backend.
- Nuevas pantallas o navegación.
- Cambios de permisos o geolocalizacion.

## Layout y jerarquia visual
- El mapa ocupa aproximadamente 70% del alto de la pantalla.
- El panel inferior ocupa aproximadamente 30% del alto y se mantiene siempre visible.
- El header/estado GPS se renderiza como overlay sobre el mapa para ganar altura util.
- El boton SOS (96.dp) se posiciona en la esquina superior derecha del panel, con padding suficiente para no tapar campos.

## Panel inferior
Contenido en orden vertical:
1) Etiqueta “REPORTE RAPIDO” y estado de conteo.
2) Selector de categoria.
3) Campo de titulo.
4) Campo de descripcion (multilinea).
5) Boton “Enviar reporte”.

Notas de UI:
- Estilo card oscuro con bordes suaves, consistente con el tema existente.
- El boton SOS se superpone dentro del panel via `Box` con `align(TopEnd)`.

## Componentes involucrados
- `HomeScreen`: reestructurar layout principal (mapa + panel inferior).
- `MapStatusOverlay`: se mantiene pero se posiciona sobre el mapa.
- Formulario de reporte: agregar descripcion si no existe.

## Data flow
- `HomeViewModel` sigue manejando estado de formulario (categoria, titulo, descripcion).
- `PanicViewModel` sigue manejando SOS y ubicacion.
- El mapa lee ubicacion desde `panicState`.
- El panel usa `state` para inputs.

## Errores y estados
- Dialogos existentes se mantienen (SOS enviado, sin ubicacion, alerta cercana).
- Si la descripcion es obligatoria, mostrar error inline en el panel (solo UI).

## Pruebas
Manual:
- Verificar que el mapa domina la pantalla (~70%).
- Verificar que el panel inferior es fijo y visible.
- Verificar posicion y tamaño del boton SOS en la esquina superior derecha del panel.
- Verificar que categoria, titulo y descripcion se ven y se pueden editar.
- Probar enviar reporte con y sin GPS.

## Criterios de aceptacion
- El mapa es el elemento principal y ocupa ~70% de la altura.
- El panel inferior muestra categoria, titulo y descripcion.
- El boton SOS se mantiene en la esquina superior derecha del panel, mismo tamaño actual.
- El header y estado GPS se muestran como overlay sobre el mapa.


-- V8: Localiza categorías visibles para usuarios de Perú.

UPDATE categories
   SET name = CASE name
       WHEN 'Robbery' THEN 'Robo'
       WHEN 'Harassment' THEN 'Acoso'
       WHEN 'No lighting' THEN 'Falta de alumbrado'
       WHEN 'Accident' THEN 'Accidente'
       WHEN 'Danger zone' THEN 'Zona peligrosa'
       WHEN 'Other' THEN 'Otro'
       ELSE name
   END
 WHERE name IN ('Robbery', 'Harassment', 'No lighting', 'Accident', 'Danger zone', 'Other');

-- V7: description puede ser nula y sin límite de longitud (lo maneja el cliente)
ALTER TABLE reports ALTER COLUMN description DROP NOT NULL;
ALTER TABLE reports DROP CONSTRAINT IF EXISTS reports_description_len_chk;

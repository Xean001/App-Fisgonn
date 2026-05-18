-- V6: Agrega title a reports y asegura la FK a categories
ALTER TABLE reports ADD COLUMN IF NOT EXISTS title TEXT;
ALTER TABLE reports ADD COLUMN IF NOT EXISTS category_id UUID REFERENCES categories(id) ON DELETE SET NULL;

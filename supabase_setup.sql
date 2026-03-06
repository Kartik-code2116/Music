-- ============================================================
-- Supabase Table: saved_music
-- Run this SQL in your Supabase SQL Editor to create the table
-- Dashboard > SQL Editor > New Query > Paste & Run
-- ============================================================

CREATE TABLE IF NOT EXISTS saved_music (
    id          BIGSERIAL PRIMARY KEY,
    track_id    BIGINT NOT NULL,
    title       TEXT NOT NULL,
    artist_name TEXT NOT NULL,
    album_title TEXT,
    cover_url   TEXT,
    preview_url TEXT NOT NULL,
    duration    INTEGER DEFAULT 0,
    is_favorite BOOLEAN DEFAULT FALSE,
    created_at  TIMESTAMPTZ DEFAULT NOW(),
    
    -- Ensure no duplicate track_id entries
    CONSTRAINT unique_track_id UNIQUE (track_id)
);

-- Enable Row Level Security (RLS) - optional but recommended
-- For now, allow all operations (public access) since we're using anon key
ALTER TABLE saved_music ENABLE ROW LEVEL SECURITY;

-- Policy: Allow anyone to read
CREATE POLICY "Allow public read" ON saved_music
    FOR SELECT USING (true);

-- Policy: Allow anyone to insert
CREATE POLICY "Allow public insert" ON saved_music
    FOR INSERT WITH CHECK (true);

-- Policy: Allow anyone to update
CREATE POLICY "Allow public update" ON saved_music
    FOR UPDATE USING (true) WITH CHECK (true);

-- Policy: Allow anyone to delete
CREATE POLICY "Allow public delete" ON saved_music
    FOR DELETE USING (true);

-- Create index on track_id for faster lookups
CREATE INDEX idx_saved_music_track_id ON saved_music (track_id);

-- Create index on is_favorite for filtering favorites
CREATE INDEX idx_saved_music_favorite ON saved_music (is_favorite);

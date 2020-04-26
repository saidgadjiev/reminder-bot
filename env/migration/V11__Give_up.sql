ALTER TABLE challenge_participant ADD COLUMN state INT NOT NULL DEFAULT 0;
UPDATE challenge_participant SET state = 1 WHERE invitation_accepted;
ALTER TABLE challenge_participant DROP COLUMN invitation_accepted;
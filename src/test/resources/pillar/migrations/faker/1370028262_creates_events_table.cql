-- description: creates events table
-- comment: was 137002362, changed to 1370023000 = 1970-01-16 21:33:43.0
-- authoredAtDate: 1970-01-16 21:33:43
-- up:

CREATE TABLE events (
  batch_id text,
  occurred_at uuid,
  event_type text,
  payload blob,
  PRIMARY KEY (batch_id, occurred_at, event_type)
)
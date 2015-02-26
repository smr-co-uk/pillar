-- description: creates events table with stages
-- comment: Fri Jan 16 21:33:43 GMT 1970
-- authoredAt: 1370023265
-- up:

-- stage: 1
CREATE TABLE events1 (
  batch_id text,
  occurred_at uuid,
  event_type text,
  payload blob,
  PRIMARY KEY (batch_id, occurred_at, event_type)
)

-- stage: 2
CREATE TABLE events2 (
  batch_id text,
  occurred_at uuid,
  event_type text,
  payload blob,
  PRIMARY KEY (batch_id, occurred_at, event_type)
)

-- stage: 3
CREATE TABLE events3 (
  batch_id text,
  event_type text,
  payload blob,
  PRIMARY KEY (batch_id)
)


-- down:
-- stage: 1
DROP TABLE events1

-- stage: 2
DROP TABLE events2

-- stage: 3
DROP TABLE events3
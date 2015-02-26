-- description: inserts events
-- authoredAtDate: 2014-10-31 13:00:00
-- up:

begin batch
insert into events3 (batch_id, event_type) values ('001', '001');
insert into events3 (batch_id, event_type) values ('002', '002');
apply batch

-- down:

truncate table events3
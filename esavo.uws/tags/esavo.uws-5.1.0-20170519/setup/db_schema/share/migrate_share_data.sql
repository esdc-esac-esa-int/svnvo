DELETE FROM share.resource_visibility;
DELETE FROM share.share_relation;
DELETE FROM share.resource;
DELETE FROM share.user_group;
DELETE FROM share.group;
DELETE FROM share.user;

-- Populate user
INSERT INTO share.user SELECT DISTINCT(owner_id) from share_schema.share;
INSERT INTO share.user SELECT DISTINCT(creator) from share_schema.groups where creator not in (select * from share.user);
INSERT INTO share.user SELECT DISTINCT(user_id) from share_schema.user_groups where user_id not in (select * from share.user);


-- Populate group
INSERT INTO share.group SELECT group_id, creator, title, description from share_schema.groups;

-- Populate user_group
INSERT INTO share.user_group SELECT user_id, group_id from share_schema.user_groups;


-- Populate shared_resource
INSERT INTO share.resource SELECT resource_id, owner_id, resource_type, title, description from share_schema.share;


-- Populate share_relation
INSERT INTO share.share_relation SELECT resource_id, group_id, share_mode from share_schema.share_groups

-- Populate resource_visibility

-- All resources visible for the members of a group, unless the group is the public group
insert into share.resource_visibility 
	select u.user_id, res.resource_id 
		from share.user u, share.user_group ug, share.group g, share.share_relation rel, share.resource res
		where u.user_id=ug.user_id
		and ug.group_id=g.group_id
		and g.group_id=rel.group_id
		and rel.resource_id=res.resource_id
		--and g.title not like 'Public group'

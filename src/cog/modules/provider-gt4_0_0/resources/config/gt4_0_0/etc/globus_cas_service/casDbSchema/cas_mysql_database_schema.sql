-- Schema for CAS Server backend database for MySQL.
-- createdb casDatabase

-- Trust Anchor
-- The nickname field is just to identify the record with the database
-- The trust anchor is uniquely identified by auth_method and auth_data
create table trust_anchor_table
(
	trust_anchor_nickname varchar(50) not null,
	authentication_method varchar(25) not null,
	authentication_data varchar(25) not null,
	constraint trust_unique unique(authentication_method, authentication_data),
	constraint trust_primary primary key(trust_anchor_nickname)
);

-- Users
-- The nickname field is just to identify the record with the database
-- The user is uniquely identified by subject_name and trust_anchor_name
create table user_table
(
	user_nickname varchar(50) not null,
	subject_name varchar(25) not null,
	trust_anchor_nickname varchar(25),
	constraint user_unique unique(subject_name, trust_anchor_nickname),
	constraint user_primary primary key(user_nickname),
	constraint user_reference foreign key(trust_anchor_nickname) references trust_anchor_table
);

create table user_group_table
(
	user_group_name varchar(50) not null,
	constraint user_gp_primary primary key(user_group_name)
);

create table user_group_entry
(
	user_group_name varchar(50) not null,
	user_nickname varchar(50) not null,
	constraint usergp_primary primary key(user_group_name, user_nickname),
	constraint usergp_gp_reference foreign key(user_group_name) references user_group_table,
	constraint usergp_user_reference foreign key(user_nickname) references user_table
);

-- Actions

create table service_type
(
	service_type_name varchar(50) not null,
	constraint service_primary primary key(service_type_name)
);

create table service_type_action
(
	service_action_id int AUTO_INCREMENT,
	service_type_name varchar(50),
	action_name varchar(50) not null,
	constraint sa_primary primary key(service_action_id),
	constraint sa_unique unique(service_type_name, action_name),
	constraint sa_service_reference foreign key(service_type_name) references service_type
);

create table service_action_group
(
	service_action_group_name varchar(50) not null,
	constraint sa_gp_primary primary key(service_action_group_name)
);

create table service_action_group_entry 
(
	service_action_group_name varchar(50) not null,
	service_action_id int not null,
	constraint sagp_primary primary key(service_action_group_name, service_action_id),
	constraint sagp_gp_reference foreign key(service_action_group_name) references service_action_group,
	constraint sagp_sa_reference foreign key(service_action_id) references service_type_action
);

-- Resources and actions
create table namespace_table
(
	namespace_nickname varchar(50) not null,
	basename varchar(50) not null,
	comparisonAlg varchar(50) not null,
	constraint ns_primary primary key(namespace_nickname),
	constraint ns_unique unique(basename, comparisonAlg)
);

create table object_table 
(
	object_id int AUTO_INCREMENT,
	object_name varchar(50) not null,
	namespace_nickname varchar(50),
	constraint obj_primary primary key(object_id),
	constraint obj_unique unique(object_name, namespace_nickname),
	constraint objgp_ns_reference foreign key(namespace_nickname) references namespace_table
);
		
create table object_group_table
(
	object_group_name varchar(50) not null,
	constraint obj_gp_primary primary key(object_group_name)
);

-- object_specification object_id(convert to varchar(50)), user_name, user_group_name, 
-- service_type_name
-- namespace_nick, trustAnchor_nick
-- object_spec_desc object, user, userGroup, serviceType, namespace, trustAnchor
create table object_group_entry
(
	object_spec_desc varchar(50) not null,
	object_specification varchar(50) not null,
	object_group_name varchar(50) not null,
	constraint objgp_primary primary key(object_spec_desc, object_specification, object_group_name),
	constraint objgp_reference foreign key(object_group_name) references object_group_table
);

-- Policy table

-- action specification can be service_action_id(convert to text or 
-- service_action_gp_name
-- action_spec_desc serviceAction, serviceActionGroup
-- userGroupname (permissions can be only on usergroup and not on indivudual 
-- users
-- object_specification object_id(convert to text), object_group_name, 
-- user_name, user_group_name, service_type_name, namespaceNick, trustAnchorNick
-- object_spec_desc object, objectGroup, user, userGroup, serviceType, 
-- namespace, trustAnchor
create table policy_table
(
	policy_id int AUTO_INCREMENT,
	user_group_name varchar(50) not null,
	action_specification varchar(50) not null,
	action_spec_desc varchar(50) not null,
	object_specification varchar(50) not null,
	object_spec_desc varchar(50) not null,
	constraint policy_primary primary key(policy_id),
	constraint policy_unique unique(user_group_name, action_specification, action_spec_desc, object_specification, object_spec_desc)
);



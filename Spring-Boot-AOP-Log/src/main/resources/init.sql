create table sys_log
(
	id varchar(50) not null,
	username varchar(50) not null comment '用户名',
	operation varchar(50) not null comment '用户操作',
	time int(50) not null comment '响应时间',
	method varchar(255) not null comment '请求方法',
	params varchar(255) not null comment '请求参数',
	ip varchar(64) not null comment 'IP地址',
	createtime date null comment '创建时间'
)ENGINE=InnoDB  DEFAULT CHARSET=utf8;


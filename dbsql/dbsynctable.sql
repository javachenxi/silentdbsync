prompt PL/SQL Developer import file
prompt Created on 2018年1月22日 by Administrator
set feedback off
set define off
prompt Creating PT_DBSYNC_CONFPOOL...
create table PT_DBSYNC_CONFPOOL
(
  poolname                   VARCHAR2(50) not null,
  jdbcurl                    VARCHAR2(200) not null,
  driverclass                VARCHAR2(200) not null,
  dbuser                     VARCHAR2(50) not null,
  dbpwd                      VARCHAR2(50) not null,
  initialpoolsize            NUMBER(4) not null,
  minpoolsize                NUMBER(4) not null,
  maxpoolsize                NUMBER(4) not null,
  acquireincrement           NUMBER(4),
  maxstatements              NUMBER(4),
  maxstatementsperconnection NUMBER(4),
  maxidletime                NUMBER(8),
  dbcharset                  VARCHAR2(50),
  dbtype                     VARCHAR2(50) not null,
  appcharset                 VARCHAR2(50)
)
;
comment on table PT_DBSYNC_CONFPOOL
  is '数据库连接池配置';
comment on column PT_DBSYNC_CONFPOOL.acquireincrement
  is '当连接池中的连接耗尽的时候c3p0一次同时获取的连接数';
comment on column PT_DBSYNC_CONFPOOL.maxstatements
  is '控制数据源内加载的PreparedStatements数量';
comment on column PT_DBSYNC_CONFPOOL.maxstatementsperconnection
  is '连接池内单个连接所拥有的最大缓存statements数';
comment on column PT_DBSYNC_CONFPOOL.maxidletime
  is '最大空闲时间';
comment on column PT_DBSYNC_CONFPOOL.dbcharset
  is '数据库编码';
comment on column PT_DBSYNC_CONFPOOL.dbtype
  is '数据库类型（OracleDB,OracleDB14,PostgreSQLDB,DB2）';
comment on column PT_DBSYNC_CONFPOOL.appcharset
  is 'app编码';
alter table PT_DBSYNC_CONFPOOL
  add constraint PK_POOLNAME primary key (POOLNAME);

prompt Creating PT_DBSYNC_CONFTABLE...
create table PT_DBSYNC_CONFTABLE
(
  task_id        NUMBER not null,
  source_table   VARCHAR2(100),
  source_column  VARCHAR2(100),
  source_tabdesc VARCHAR2(200),
  target_table   VARCHAR2(100),
  target_column  VARCHAR2(100),
  target_tabdesc VARCHAR2(200),
  relate_column  VARCHAR2(200),
  depend_table   VARCHAR2(200),
  incer_token    NUMBER(1) default 0 not null,
  pkey_token     NUMBER(1) default 0 not null,
  source_dbname  VARCHAR2(50),
  target_dbname  VARCHAR2(50),
  target_coltype NUMBER(8) default 0,
  column_order   NUMBER(4) default 0
)
;
comment on table PT_DBSYNC_CONFTABLE
  is '同步表的配置信息';
comment on column PT_DBSYNC_CONFTABLE.task_id
  is '任务ID与任务表主键关联';
comment on column PT_DBSYNC_CONFTABLE.source_table
  is '源表名';
comment on column PT_DBSYNC_CONFTABLE.source_column
  is '源表字段名';
comment on column PT_DBSYNC_CONFTABLE.source_tabdesc
  is '源表描述';
comment on column PT_DBSYNC_CONFTABLE.target_table
  is '目标表名';
comment on column PT_DBSYNC_CONFTABLE.target_column
  is '目标表字段名';
comment on column PT_DBSYNC_CONFTABLE.target_tabdesc
  is '目标表描述';
comment on column PT_DBSYNC_CONFTABLE.relate_column
  is '关联字段';
comment on column PT_DBSYNC_CONFTABLE.depend_table
  is '依赖字段';
comment on column PT_DBSYNC_CONFTABLE.incer_token
  is '增量标记(0-不是，1-是)';
comment on column PT_DBSYNC_CONFTABLE.pkey_token
  is '主键标记(0-不是，1-是)';
comment on column PT_DBSYNC_CONFTABLE.source_dbname
  is '源端数据库';
comment on column PT_DBSYNC_CONFTABLE.target_dbname
  is '目标端数据库';
comment on column PT_DBSYNC_CONFTABLE.target_coltype
  is 'JDBC类型SQLTYPE';
comment on column PT_DBSYNC_CONFTABLE.column_order
  is '列顺序';

prompt Creating PT_DBSYNC_CONFTABLESQL...
create table PT_DBSYNC_CONFTABLESQL
(
  task_id              NUMBER not null,
  source_table         VARCHAR2(100) not null,
  target_table         VARCHAR2(100) not null,
  source_dbname        VARCHAR2(100) not null,
  target_dbname        VARCHAR2(100) not null,
  src_selectsql        VARCHAR2(4000),
  src_selectorderbysql VARCHAR2(4000),
  src_shortselectsql   VARCHAR2(4000),
  tag_deletesql        VARCHAR2(4000),
  tag_deletebyidsql    VARCHAR2(4000),
  tag_insertsql        VARCHAR2(4000),
  tag_minsertsql       VARCHAR2(4000),
  tag_updatesql        VARCHAR2(4000),
  create_date          DATE default sysdate not null,
  tag_selectorderbysql VARCHAR2(4000)
)
;
comment on column PT_DBSYNC_CONFTABLESQL.task_id
  is '任务ID与任务表主键关联';
comment on column PT_DBSYNC_CONFTABLESQL.source_table
  is '源表名';
comment on column PT_DBSYNC_CONFTABLESQL.target_table
  is '目标表名';
comment on column PT_DBSYNC_CONFTABLESQL.source_dbname
  is '源端数据库';
comment on column PT_DBSYNC_CONFTABLESQL.target_dbname
  is '目标端数据库';
comment on column PT_DBSYNC_CONFTABLESQL.src_selectsql
  is '源端抽取SQL';
comment on column PT_DBSYNC_CONFTABLESQL.src_selectorderbysql
  is '源端抽取SQL(按主键排序)';
comment on column PT_DBSYNC_CONFTABLESQL.src_shortselectsql
  is '源端抽取SQL(增量时全表抽取)';
comment on column PT_DBSYNC_CONFTABLESQL.tag_deletesql
  is '目标端删除所有记录SQL';
comment on column PT_DBSYNC_CONFTABLESQL.tag_deletebyidsql
  is '目标端删除记录SQL根据ID';
comment on column PT_DBSYNC_CONFTABLESQL.tag_insertsql
  is '目标端新增记录';
comment on column PT_DBSYNC_CONFTABLESQL.tag_minsertsql
  is '目标端新增或更新记录';
comment on column PT_DBSYNC_CONFTABLESQL.tag_updatesql
  is '目标端更新记录';
comment on column PT_DBSYNC_CONFTABLESQL.create_date
  is '新建时间';
comment on column PT_DBSYNC_CONFTABLESQL.tag_selectorderbysql
  is '目标端抽取SQL(按主键排序)';
alter table PT_DBSYNC_CONFTABLESQL
  add constraint PK_DBSYNC_CONFSQL primary key (TASK_ID, SOURCE_TABLE, TARGET_TABLE, SOURCE_DBNAME, TARGET_DBNAME);

prompt Creating PT_DBSYNC_CONFTASK...
create table PT_DBSYNC_CONFTASK
(
  task_id        NUMBER not null,
  task_name      VARCHAR2(100),
  task_type      NUMBER(1),
  sync_cycle     NUMBER(8),
  sync_lasttime  DATE,
  sync_lastvalue VARCHAR2(4000),
  yxbz           CHAR(1) not null,
  inst_count     NUMBER default 0 not null,
  task_created   DATE default SYSDATE,
  roll_lastvalue NUMBER
)
;
comment on table PT_DBSYNC_CONFTASK
  is '数据库同步任务配置表';
comment on column PT_DBSYNC_CONFTASK.task_id
  is '任务ID';
comment on column PT_DBSYNC_CONFTASK.task_name
  is '同步业务名称';
comment on column PT_DBSYNC_CONFTASK.task_type
  is '同步业务类型(1-全量,2-增量)';
comment on column PT_DBSYNC_CONFTASK.sync_cycle
  is '同步周期(1-年,2-月,3-周,4-天,大于10按秒计算)';
comment on column PT_DBSYNC_CONFTASK.sync_lasttime
  is '最后同步时间';
comment on column PT_DBSYNC_CONFTASK.sync_lastvalue
  is '最后同步增量字段值(JSON)';
comment on column PT_DBSYNC_CONFTASK.yxbz
  is '有效标志(Y-有效，N-无效)';
comment on column PT_DBSYNC_CONFTASK.inst_count
  is '实例计数器';
comment on column PT_DBSYNC_CONFTASK.task_created
  is '任务创建日期';
comment on column PT_DBSYNC_CONFTASK.roll_lastvalue
  is '增量同步时，回退的值（时间类型-减去N分钟，数值-减去N,字符串-尾部截取N个字符）';
alter table PT_DBSYNC_CONFTASK
  add constraint PT_DBSYNC_CONFTASK_PK primary key (TASK_ID);

prompt Creating PT_DBSYNC_SCHEDULE...
create table PT_DBSYNC_SCHEDULE
(
  task_inst     NUMBER not null,
  task_id       NUMBER not null,
  task_name     VARCHAR2(1000) not null,
  task_type     NUMBER(4) not null,
  task_param    VARCHAR2(1000),
  generate_time DATE not null,
  plan_time     DATE not null,
  begin_time    DATE,
  end_time      DATE,
  total_waste   NUMBER,
  current_msg   VARCHAR2(1000),
  finish_rate   NUMBER,
  errormsg      VARCHAR2(1000),
  status        NUMBER(4),
  server_flag   VARCHAR2(100)
)
;
comment on column PT_DBSYNC_SCHEDULE.task_inst
  is '任务实例ID';
comment on column PT_DBSYNC_SCHEDULE.task_id
  is '任务模板ID';
comment on column PT_DBSYNC_SCHEDULE.task_name
  is '任务名称';
comment on column PT_DBSYNC_SCHEDULE.task_type
  is '任务类型';
comment on column PT_DBSYNC_SCHEDULE.task_param
  is '任务参数';
comment on column PT_DBSYNC_SCHEDULE.generate_time
  is '创建时间';
comment on column PT_DBSYNC_SCHEDULE.plan_time
  is '计划执行时间';
comment on column PT_DBSYNC_SCHEDULE.begin_time
  is '执行开始时间';
comment on column PT_DBSYNC_SCHEDULE.end_time
  is '执行结束时间';
comment on column PT_DBSYNC_SCHEDULE.total_waste
  is '整体耗时';
comment on column PT_DBSYNC_SCHEDULE.current_msg
  is '当前消息';
comment on column PT_DBSYNC_SCHEDULE.finish_rate
  is '完成比例';
comment on column PT_DBSYNC_SCHEDULE.errormsg
  is '错误信息';
comment on column PT_DBSYNC_SCHEDULE.status
  is '状态(0:未处理 1:处理成功 2:停止 3:暂停 4:处理失败 98:处理成功但是有异常 99:处理中)';
comment on column PT_DBSYNC_SCHEDULE.server_flag
  is '执行服务器标识';
alter table PT_DBSYNC_SCHEDULE
  add constraint PK_TASKINST primary key (TASK_INST);

prompt Creating PT_DBSYNC_TASKINSTLOG...
create table PT_DBSYNC_TASKINSTLOG
(
  taskinst_id    VARCHAR2(100) not null,
  task_id        NUMBER not null,
  sync_status    NUMBER(1) not null,
  sync_datasize  VARCHAR2(500),
  sync_info      VARCHAR2(4000),
  sync_date      DATE,
  sync_lastvalue VARCHAR2(4000),
  synclog_id     NUMBER not null,
  sync_allsize   NUMBER
)
;
comment on table PT_DBSYNC_TASKINSTLOG
  is '数据同步日志表';
comment on column PT_DBSYNC_TASKINSTLOG.taskinst_id
  is '任务实例ID';
comment on column PT_DBSYNC_TASKINSTLOG.task_id
  is '任务ID';
comment on column PT_DBSYNC_TASKINSTLOG.sync_status
  is '同步状态（1-成功，2-失败）';
comment on column PT_DBSYNC_TASKINSTLOG.sync_datasize
  is '本次同步的数据量JSON';
comment on column PT_DBSYNC_TASKINSTLOG.sync_info
  is '同步的信息';
comment on column PT_DBSYNC_TASKINSTLOG.sync_date
  is '同步的日期';
comment on column PT_DBSYNC_TASKINSTLOG.sync_lastvalue
  is '同步最后增量值';
comment on column PT_DBSYNC_TASKINSTLOG.synclog_id
  is '同步日志ID';
comment on column PT_DBSYNC_TASKINSTLOG.sync_allsize
  is '一次同步总量';

prompt Disabling triggers for PT_DBSYNC_CONFPOOL...
alter table PT_DBSYNC_CONFPOOL disable all triggers;
prompt Disabling triggers for PT_DBSYNC_CONFTABLE...
alter table PT_DBSYNC_CONFTABLE disable all triggers;
prompt Disabling triggers for PT_DBSYNC_CONFTABLESQL...
alter table PT_DBSYNC_CONFTABLESQL disable all triggers;
prompt Disabling triggers for PT_DBSYNC_CONFTASK...
alter table PT_DBSYNC_CONFTASK disable all triggers;
prompt Disabling triggers for PT_DBSYNC_SCHEDULE...
alter table PT_DBSYNC_SCHEDULE disable all triggers;
prompt Disabling triggers for PT_DBSYNC_TASKINSTLOG...
alter table PT_DBSYNC_TASKINSTLOG disable all triggers;
prompt Loading PT_DBSYNC_CONFPOOL...
prompt Table is empty
prompt Loading PT_DBSYNC_CONFTABLE...
prompt Table is empty
prompt Loading PT_DBSYNC_CONFTABLESQL...
prompt Table is empty
prompt Loading PT_DBSYNC_CONFTASK...
prompt Table is empty
prompt Loading PT_DBSYNC_SCHEDULE...
prompt Table is empty
prompt Loading PT_DBSYNC_TASKINSTLOG...
prompt Table is empty
prompt Enabling triggers for PT_DBSYNC_CONFPOOL...
alter table PT_DBSYNC_CONFPOOL enable all triggers;
prompt Enabling triggers for PT_DBSYNC_CONFTABLE...
alter table PT_DBSYNC_CONFTABLE enable all triggers;
prompt Enabling triggers for PT_DBSYNC_CONFTABLESQL...
alter table PT_DBSYNC_CONFTABLESQL enable all triggers;
prompt Enabling triggers for PT_DBSYNC_CONFTASK...
alter table PT_DBSYNC_CONFTASK enable all triggers;
prompt Enabling triggers for PT_DBSYNC_SCHEDULE...
alter table PT_DBSYNC_SCHEDULE enable all triggers;
prompt Enabling triggers for PT_DBSYNC_TASKINSTLOG...
alter table PT_DBSYNC_TASKINSTLOG enable all triggers;
set feedback on
set define on
prompt Done.

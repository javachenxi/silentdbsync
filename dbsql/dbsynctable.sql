prompt PL/SQL Developer import file
prompt Created on 2018��1��22�� by Administrator
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
  is '���ݿ����ӳ�����';
comment on column PT_DBSYNC_CONFPOOL.acquireincrement
  is '�����ӳ��е����Ӻľ���ʱ��c3p0һ��ͬʱ��ȡ��������';
comment on column PT_DBSYNC_CONFPOOL.maxstatements
  is '��������Դ�ڼ��ص�PreparedStatements����';
comment on column PT_DBSYNC_CONFPOOL.maxstatementsperconnection
  is '���ӳ��ڵ���������ӵ�е���󻺴�statements��';
comment on column PT_DBSYNC_CONFPOOL.maxidletime
  is '������ʱ��';
comment on column PT_DBSYNC_CONFPOOL.dbcharset
  is '���ݿ����';
comment on column PT_DBSYNC_CONFPOOL.dbtype
  is '���ݿ����ͣ�OracleDB,OracleDB14,PostgreSQLDB,DB2��';
comment on column PT_DBSYNC_CONFPOOL.appcharset
  is 'app����';
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
  is 'ͬ�����������Ϣ';
comment on column PT_DBSYNC_CONFTABLE.task_id
  is '����ID���������������';
comment on column PT_DBSYNC_CONFTABLE.source_table
  is 'Դ����';
comment on column PT_DBSYNC_CONFTABLE.source_column
  is 'Դ���ֶ���';
comment on column PT_DBSYNC_CONFTABLE.source_tabdesc
  is 'Դ������';
comment on column PT_DBSYNC_CONFTABLE.target_table
  is 'Ŀ�����';
comment on column PT_DBSYNC_CONFTABLE.target_column
  is 'Ŀ����ֶ���';
comment on column PT_DBSYNC_CONFTABLE.target_tabdesc
  is 'Ŀ�������';
comment on column PT_DBSYNC_CONFTABLE.relate_column
  is '�����ֶ�';
comment on column PT_DBSYNC_CONFTABLE.depend_table
  is '�����ֶ�';
comment on column PT_DBSYNC_CONFTABLE.incer_token
  is '�������(0-���ǣ�1-��)';
comment on column PT_DBSYNC_CONFTABLE.pkey_token
  is '�������(0-���ǣ�1-��)';
comment on column PT_DBSYNC_CONFTABLE.source_dbname
  is 'Դ�����ݿ�';
comment on column PT_DBSYNC_CONFTABLE.target_dbname
  is 'Ŀ������ݿ�';
comment on column PT_DBSYNC_CONFTABLE.target_coltype
  is 'JDBC����SQLTYPE';
comment on column PT_DBSYNC_CONFTABLE.column_order
  is '��˳��';

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
  is '����ID���������������';
comment on column PT_DBSYNC_CONFTABLESQL.source_table
  is 'Դ����';
comment on column PT_DBSYNC_CONFTABLESQL.target_table
  is 'Ŀ�����';
comment on column PT_DBSYNC_CONFTABLESQL.source_dbname
  is 'Դ�����ݿ�';
comment on column PT_DBSYNC_CONFTABLESQL.target_dbname
  is 'Ŀ������ݿ�';
comment on column PT_DBSYNC_CONFTABLESQL.src_selectsql
  is 'Դ�˳�ȡSQL';
comment on column PT_DBSYNC_CONFTABLESQL.src_selectorderbysql
  is 'Դ�˳�ȡSQL(����������)';
comment on column PT_DBSYNC_CONFTABLESQL.src_shortselectsql
  is 'Դ�˳�ȡSQL(����ʱȫ���ȡ)';
comment on column PT_DBSYNC_CONFTABLESQL.tag_deletesql
  is 'Ŀ���ɾ�����м�¼SQL';
comment on column PT_DBSYNC_CONFTABLESQL.tag_deletebyidsql
  is 'Ŀ���ɾ����¼SQL����ID';
comment on column PT_DBSYNC_CONFTABLESQL.tag_insertsql
  is 'Ŀ���������¼';
comment on column PT_DBSYNC_CONFTABLESQL.tag_minsertsql
  is 'Ŀ�����������¼�¼';
comment on column PT_DBSYNC_CONFTABLESQL.tag_updatesql
  is 'Ŀ��˸��¼�¼';
comment on column PT_DBSYNC_CONFTABLESQL.create_date
  is '�½�ʱ��';
comment on column PT_DBSYNC_CONFTABLESQL.tag_selectorderbysql
  is 'Ŀ��˳�ȡSQL(����������)';
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
  is '���ݿ�ͬ���������ñ�';
comment on column PT_DBSYNC_CONFTASK.task_id
  is '����ID';
comment on column PT_DBSYNC_CONFTASK.task_name
  is 'ͬ��ҵ������';
comment on column PT_DBSYNC_CONFTASK.task_type
  is 'ͬ��ҵ������(1-ȫ��,2-����)';
comment on column PT_DBSYNC_CONFTASK.sync_cycle
  is 'ͬ������(1-��,2-��,3-��,4-��,����10�������)';
comment on column PT_DBSYNC_CONFTASK.sync_lasttime
  is '���ͬ��ʱ��';
comment on column PT_DBSYNC_CONFTASK.sync_lastvalue
  is '���ͬ�������ֶ�ֵ(JSON)';
comment on column PT_DBSYNC_CONFTASK.yxbz
  is '��Ч��־(Y-��Ч��N-��Ч)';
comment on column PT_DBSYNC_CONFTASK.inst_count
  is 'ʵ��������';
comment on column PT_DBSYNC_CONFTASK.task_created
  is '���񴴽�����';
comment on column PT_DBSYNC_CONFTASK.roll_lastvalue
  is '����ͬ��ʱ�����˵�ֵ��ʱ������-��ȥN���ӣ���ֵ-��ȥN,�ַ���-β����ȡN���ַ���';
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
  is '����ʵ��ID';
comment on column PT_DBSYNC_SCHEDULE.task_id
  is '����ģ��ID';
comment on column PT_DBSYNC_SCHEDULE.task_name
  is '��������';
comment on column PT_DBSYNC_SCHEDULE.task_type
  is '��������';
comment on column PT_DBSYNC_SCHEDULE.task_param
  is '�������';
comment on column PT_DBSYNC_SCHEDULE.generate_time
  is '����ʱ��';
comment on column PT_DBSYNC_SCHEDULE.plan_time
  is '�ƻ�ִ��ʱ��';
comment on column PT_DBSYNC_SCHEDULE.begin_time
  is 'ִ�п�ʼʱ��';
comment on column PT_DBSYNC_SCHEDULE.end_time
  is 'ִ�н���ʱ��';
comment on column PT_DBSYNC_SCHEDULE.total_waste
  is '�����ʱ';
comment on column PT_DBSYNC_SCHEDULE.current_msg
  is '��ǰ��Ϣ';
comment on column PT_DBSYNC_SCHEDULE.finish_rate
  is '��ɱ���';
comment on column PT_DBSYNC_SCHEDULE.errormsg
  is '������Ϣ';
comment on column PT_DBSYNC_SCHEDULE.status
  is '״̬(0:δ���� 1:����ɹ� 2:ֹͣ 3:��ͣ 4:����ʧ�� 98:����ɹ��������쳣 99:������)';
comment on column PT_DBSYNC_SCHEDULE.server_flag
  is 'ִ�з�������ʶ';
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
  is '����ͬ����־��';
comment on column PT_DBSYNC_TASKINSTLOG.taskinst_id
  is '����ʵ��ID';
comment on column PT_DBSYNC_TASKINSTLOG.task_id
  is '����ID';
comment on column PT_DBSYNC_TASKINSTLOG.sync_status
  is 'ͬ��״̬��1-�ɹ���2-ʧ�ܣ�';
comment on column PT_DBSYNC_TASKINSTLOG.sync_datasize
  is '����ͬ����������JSON';
comment on column PT_DBSYNC_TASKINSTLOG.sync_info
  is 'ͬ������Ϣ';
comment on column PT_DBSYNC_TASKINSTLOG.sync_date
  is 'ͬ��������';
comment on column PT_DBSYNC_TASKINSTLOG.sync_lastvalue
  is 'ͬ���������ֵ';
comment on column PT_DBSYNC_TASKINSTLOG.synclog_id
  is 'ͬ����־ID';
comment on column PT_DBSYNC_TASKINSTLOG.sync_allsize
  is 'һ��ͬ������';

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

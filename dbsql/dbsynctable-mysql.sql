

create table PT_DBSYNC_CONFPOOL
(
  poolname                   varchar(50) not null ,
  jdbcurl                    varchar(200) not null ,
  driverclass                varchar(200) not null ,
  dbuser                     varchar(50) not null ,
  dbpwd                      varchar(50) not null ,
  initialpoolsize            tinyint(4) not null ,
  minpoolsize                tinyint(4) not null ,
  maxpoolsize                tinyint(4) not null ,
  acquireincrement           tinyint(4) COMMENT '当连接池中的连接耗尽的时候c3p0一次同时获取的连接数',
  maxstatements              tinyint(4) COMMENT '控制数据源内加载的PreparedStatements数量',
  maxstatementsperconnection tinyint(4) COMMENT '连接池内单个连接所拥有的最大缓存statements数',
  maxidletime                int(8) COMMENT '最大空闲时间',
  dbcharset                  varchar(50) COMMENT '数据库编码',
  dbtype                     varchar(50) not null COMMENT '数据库类型（OracleDB,OracleDB14,PostgreSQLDB,DB2）',
  appcharset                 varchar(50) COMMENT 'app编码',
  PRIMARY KEY (poolname)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;


create table PT_DBSYNC_CONFTABLE
(
  task_id        int not null,
  source_table   varchar(100),
  source_column  varchar(100),
  source_tabdesc varchar(200),
  target_table   varchar(100),
  target_column  varchar(100),
  target_tabdesc varchar(200),
  relate_column  varchar(200),
  depend_table   varchar(200),
  incer_token    tinyint(4) default 0 not null,
  pkey_token     tinyint(4) default 0 not null,
  source_dbname  varchar(50),
  target_dbname  varchar(50),
  target_coltype int default 0,
  column_order   tinyint(4) default 0
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;


create table PT_DBSYNC_CONFTABLESQL
(
  task_id              int not null,
  source_table         varchar(100) not null,
  target_table         varchar(100) not null,
  source_dbname        varchar(100) not null,
  target_dbname        varchar(100) not null,
  src_selectsql        TEXT,
  src_selectorderbysql TEXT,
  src_shortselectsql   TEXT,
  tag_deletesql        TEXT,
  tag_deletebyidsql    TEXT,
  tag_insertsql        TEXT,
  tag_minsertsql       TEXT,
  tag_updatesql        TEXT,
  create_date          datetime default now() not null,
  tag_selectorderbysql TEXT,
  primary key (task_id, source_table, target_table, source_dbname, target_dbname)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

create table PT_DBSYNC_CONFTASK
(
  task_id        int not null,
  task_name      varchar(100),
  task_type      tinyint(4),
  sync_cycle     int,
  sync_lasttime  datetime,
  sync_lastvalue TEXT,
  yxbz           CHAR(1) not null,
  inst_count     int default 0 not null,
  task_created   datetime default now(),
  roll_lastvalue int,
  primary key (task_id)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

create table PT_DBSYNC_SCHEDULE
(
  task_inst     int not null,
  task_id       int not null,
  task_name     TINYTEXT not null,
  task_type     tinyint(4) not null,
  task_param    TEXT,
  generate_time datetime not null,
  plan_time     datetime not null,
  begin_time    datetime,
  end_time      datetime,
  total_waste   int,
  current_msg   TEXT,
  finish_rate   int,
  errormsg      TEXT,
  status        tinyint(4),
  server_flag   varchar(100),
  primary key (task_inst)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

create table PT_DBSYNC_TASKINSTLOG
(
  taskinst_id    varchar(100) not null,
  task_id        int not null,
  sync_status    tinyint(4) not null,
  sync_datasize  TEXT,
  sync_info      TEXT,
  sync_date      datetime,
  sync_lastvalue TEXT,
  synclog_id     int not null,
  sync_allsize   int
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
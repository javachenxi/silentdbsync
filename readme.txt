1.概要
     silentdbsync 工程是基于JDBC的各种数据库之间同步数据，程序的元数据是基于Oracle、Mysql数据库的。

2.快速启动
     第一步，配置需要同步的数据库的连接池，按pt_dbsync_confpool表中的字段插入数据，数据库连接池是C3P0。
     第二步，配置同步任务，按pt_dbsync_conftask表的结构插入数据，其中task_type字段同步业务类型(1-全量,2-增量,3-校验)。
	 第三步，配置同步任务中的表与字段，按pt_dbsync_conftable表的结构初始化数据，其中task_id与pt_dbsync_conftask表的task_id对应；
	         其中source_dbname、target_dbname是与pt_dbsync_confpool表中的poolname对应；dbtype 字段的值是JDBC中Types类中的属性值
			 比如（Types.VARCHAR 就是取值 12）。
			 
3.建议	
     第三步手工配置比较繁琐，可以使用存储过程初始化，代码中有居于Mysql数据库的存储过程（文件:dbsynctable-init-procedure-mysql.sql）。
	 
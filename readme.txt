1.��Ҫ
     silentdbsync �����ǻ���JDBC�ĸ������ݿ�֮��ͬ�����ݣ������Ԫ�����ǻ���Oracle��Mysql���ݿ�ġ�

2.��������
     ��һ����������Ҫͬ�������ݿ�����ӳأ���pt_dbsync_confpool���е��ֶβ������ݣ����ݿ����ӳ���C3P0��
     �ڶ���������ͬ�����񣬰�pt_dbsync_conftask��Ľṹ�������ݣ�����task_type�ֶ�ͬ��ҵ������(1-ȫ��,2-����,3-У��)��
	 ������������ͬ�������еı����ֶΣ���pt_dbsync_conftable��Ľṹ��ʼ�����ݣ�����task_id��pt_dbsync_conftask���task_id��Ӧ��
	         ����source_dbname��target_dbname����pt_dbsync_confpool���е�poolname��Ӧ��dbtype �ֶε�ֵ��JDBC��Types���е�����ֵ
			 ���磨Types.VARCHAR ����ȡֵ 12����
			 
3.����	
     �������ֹ����ñȽϷ���������ʹ�ô洢���̳�ʼ�����������о���Mysql���ݿ�Ĵ洢���̣��ļ�:dbsynctable-init-procedure-mysql.sql����
	 
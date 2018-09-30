DELIMITER $$

USE `dbsync`$$

DROP PROCEDURE IF EXISTS `init_dbsync_conftable`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `init_dbsync_conftable`(IN task_id INT,IN source_dbname VARCHAR(50),
    IN target_dbname VARCHAR(50),IN source_table VARCHAR(50),IN target_table VARCHAR(50), IN incer_column VARCHAR(50))
BEGIN
	   DECLARE tablename VARCHAR(50);
	   DECLARE columnname VARCHAR(50);
	   DECLARE datatype VARCHAR(50);
	   DECLARE columnkey VARCHAR(50);
	   DECLARE incertoken TINYINT DEFAULT 0;
	   DECLARE pkeytoken TINYINT DEFAULT 0;
	   DECLARE targetcoltype INTEGER DEFAULT 12;
	   DECLARE columnorder TINYINT DEFAULT 1;

	   DECLARE done INT DEFAULT FALSE;
	   DECLARE cursor_column CURSOR FOR
	     SELECT table_name, column_name, data_type, column_key
	     FROM information_schema.columns t WHERE t.table_name=source_table;
	   DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

	   OPEN cursor_column;
	   read_loop: LOOP
              FETCH cursor_column INTO tablename, columnname, datatype, columnkey;

              IF done THEN
                 LEAVE read_loop;
              END IF;
              -- call xlxiottest.proc_out_print(CONCAT('dbsync', tablename));
              SET incertoken = CASE WHEN incer_column IS NOT NULL AND incer_column=columnname THEN 1 ELSE 0 END;
              SET pkeytoken = CASE WHEN columnkey IS NOT NULL AND columnkey<>'' THEN 1 ELSE 0 END;
              SET targetcoltype = CASE WHEN datatype='int' THEN 4 WHEN datatype='tinyint' THEN -6
                   WHEN datatype='date' THEN 91 WHEN datatype='datetime' THEN 93
                   WHEN datatype='double' THEN 8 ELSE 12 END;
              SET columnorder = columnorder+5;

              INSERT INTO pt_dbsync_conftable(task_id, source_table, source_column, target_table, target_column,
              incer_token,pkey_token,source_dbname,target_dbname,target_coltype, column_order)
              VALUES(task_id, tablename, columnname, target_table, columnname, incertoken, pkeytoken,source_dbname, target_dbname,
              targetcoltype, columnorder);

           END LOOP;
           CLOSE cursor_column;

	END$$

DELIMITER ;
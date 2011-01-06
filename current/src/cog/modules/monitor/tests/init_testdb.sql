USE test;

DROP TABLE IF EXISTS gftprecords;

CREATE TABLE gftprecords(
    id BIGINT NOT NULL AUTO_INCREMENT,
    component_code SMALLINT NOT NULL,
    version_code SMALLINT NOT NULL,
    send_time DATETIME,
    ip_version TINYINT,
    ip_address TINYTEXT NOT NULL,
    otherside_ip TINYTEXT,
    gftp_version VARCHAR(20),
    stor_or_retr TINYINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    num_bytes BIGINT NOT NULL,
    num_stripes BIGINT,
    num_streams BIGINT,
    buffer_size BIGINT,
    block_size BIGINT,
    ftp_return_code BIGINT,
    sequence_num BIGINT,
    PRIMARY KEY (id)
);
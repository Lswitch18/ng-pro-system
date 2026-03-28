-- RADIUS SQL Schema for NG-PRO Billing System
-- SQLite compatible

-- Check if user exists and is active
-- Returns: username, password, plan_id, status

-- Radcheck table - stores user credentials and attributes
CREATE TABLE IF NOT EXISTS radcheck (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(64) NOT NULL DEFAULT '',
    attribute VARCHAR(64) NOT NULL DEFAULT '',
    op CHAR(2) NOT NULL DEFAULT '=',
    value VARCHAR(253) NOT NULL DEFAULT ''
);

CREATE INDEX IF NOT EXISTS radcheck_username ON radcheck(username);

-- Radreply table - reply attributes returned to NAS
CREATE TABLE IF NOT EXISTS radreply (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(64) NOT NULL DEFAULT '',
    attribute VARCHAR(64) NOT NULL DEFAULT '',
    op CHAR(2) NOT NULL DEFAULT '=',
    value VARCHAR(253) NOT NULL DEFAULT ''
);

CREATE INDEX IF NOT EXISTS radreply_username ON radreply(username);

-- Radusergroup table - user to group mapping
CREATE TABLE IF NOT EXISTS radusergroup (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(64) NOT NULL DEFAULT '',
    groupname VARCHAR(64) NOT NULL DEFAULT '',
    priority INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS radusergroup_username ON radusergroup(username);

-- Radgroupreply table - group reply attributes
CREATE TABLE IF NOT EXISTS radgroupreply (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    groupname VARCHAR(64) NOT NULL DEFAULT '',
    attribute VARCHAR(64) NOT NULL DEFAULT '',
    op CHAR(2) NOT NULL DEFAULT '=',
    value VARCHAR(253) NOT NULL DEFAULT ''
);

-- Radacct table - accounting records
CREATE TABLE IF NOT EXISTS radacct (
    radacctid INTEGER PRIMARY KEY AUTOINCREMENT,
    acctsessionid VARCHAR(64) NOT NULL DEFAULT '',
    acctuniqueid VARCHAR(32) NOT NULL DEFAULT '',
    username VARCHAR(64) NOT NULL DEFAULT '',
    realm VARCHAR(64) DEFAULT '',
    nasipaddress VARCHAR(15) NOT NULL DEFAULT '',
    nasportid VARCHAR(15) DEFAULT NULL,
    nasporttype VARCHAR(32) DEFAULT NULL,
    acctstarttime DATETIME DEFAULT NULL,
    acctstoptime DATETIME DEFAULT NULL,
    acctsessiontime INTEGER UNSIGNED DEFAULT NULL,
    acctauthentic VARCHAR(32) DEFAULT NULL,
    connectinfo_start VARCHAR(50) DEFAULT NULL,
    connectinfo_stop VARCHAR(50) DEFAULT NULL,
    acctinputoctets BIGINT DEFAULT NULL,
    acctoutputoctets BIGINT DEFAULT NULL,
    calledstationid VARCHAR(50) DEFAULT '',
    callingstationid VARCHAR(50) DEFAULT '',
    acctterminatecause VARCHAR(32) DEFAULT '',
    servicetype VARCHAR(32) DEFAULT NULL,
    framedprotocol VARCHAR(32) DEFAULT NULL,
    framedipaddress VARCHAR(15) NOT NULL DEFAULT '',
    framedipv6address VARCHAR(45) DEFAULT '',
    framedipv6prefix VARCHAR(45) DEFAULT '',
    framedinterfaceid VARCHAR(44) DEFAULT '',
    delegatedipv6prefix VARCHAR(45) DEFAULT ''
);

CREATE INDEX IF NOT EXISTS radacct_username ON radacct(username);
CREATE INDEX IF NOT EXISTS radacct_acctuniqueid ON radacct(acctuniqueid);
CREATE INDEX IF NOT EXISTS radacct_acctsessionid ON radacct(acctsessionid);
CREATE INDEX IF NOT EXISTS radacct_acctstarttime ON radacct(acctstarttime);
CREATE INDEX IF NOT EXISTS radacct_acctstoptime ON radacct(acctstoptime);
CREATE INDEX IF NOT EXISTS radacct_nasipaddress ON radacct(nasipaddress);

-- Radpostauth table - post-authentication logging
CREATE TABLE IF NOT EXISTS radpostauth (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(64) NOT NULL DEFAULT '',
    pass VARCHAR(64) NOT NULL DEFAULT '',
    reply VARCHAR(32) NOT NULL DEFAULT '',
    authdate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- NAS table - Network Access Servers
CREATE TABLE IF NOT EXISTS nas (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nasname VARCHAR(128) NOT NULL,
    shortname VARCHAR(32) DEFAULT NULL,
    type VARCHAR(30) DEFAULT 'other',
    ports INTEGER DEFAULT NULL,
    secret VARCHAR(60) NOT NULL DEFAULT 'secret',
    server VARCHAR(64) DEFAULT NULL,
    community VARCHAR(50) DEFAULT NULL,
    description VARCHAR(200) DEFAULT NULL
);

-- Insert default NAS entries
INSERT INTO nas (nasname, shortname, type, secret, description) VALUES 
    ('127.0.0.1', 'localhost', 'other', 'testing123', 'Localhost test'),
    ('10.0.0.1', 'mikrotik-main', 'mikrotik', 'ngpro_secret_key', 'Main Mikrotik Router'),
    ('10.0.0.2', 'ubiquiti-ap1', 'ubiquiti', 'ngpro_secret_key', 'Ubiquiti Access Point 1');

-- Insert test user
INSERT INTO radcheck (username, attribute, op, value) VALUES 
    ('testuser', 'Cleartext-Password', ':=', 'testpass');

-- Insert test reply attributes
INSERT INTO radreply (username, attribute, op, value) VALUES 
    ('testuser', 'Framed-IP-Address', '=', '10.0.0.100'),
    ('testuser', 'Framed-Netmask', '=', '255.255.255.0'),
    ('testuser', 'Framed-Route', '=', '0.0.0.0/0'),
    ('testuser', 'Session-Timeout', '=', '86400');

-- Insert user group
INSERT INTO radusergroup (username, groupname, priority) VALUES 
    ('testuser', 'pppoe-users', 1);

-- Insert group reply attributes
INSERT INTO radgroupreply (groupname, attribute, op, value) VALUES 
    ('pppoe-users', 'Framed-Protocol', '=', 'PPP'),
    ('pppoe-users', 'Service-Type', '=', 'Framed-User');

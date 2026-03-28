const dgram = require('dgram');
const crypto = require('crypto');
const express = require('express');
const cors = require('cors');

const app = express();
app.use(cors());
app.use(express.json());

const NAS_CONFIG = {
    nasId: 'ngpro-nas-01',
    nasIp: '10.0.0.10',
    nasSecret: 'testing123',
    radiusServer: process.env.RADIUS_SERVER || '10.0.0.10',
    radiusPort: 1812,
    accountingPort: 1813
};

const sessions = new Map();
const clients = new Map();

function createRADIUSAccessRequest(username, password, attributes = {}) {
    const code = 1; // Access-Request
    const id = Math.floor(Math.random() * 256);
    const length = 20 + Buffer.byteLength(username) + Buffer.byteLength(password);
    
    const authenticator = crypto.randomBytes(16);
    
    const buffer = Buffer.alloc(length);
    buffer.writeUInt8(code, 0);
    buffer.writeUInt8(id, 1);
    buffer.writeUInt16BE(length, 2);
    authenticator.copy(buffer, 4);
    
    let offset = 20;
    
    buffer.write('User-Name', offset);
    buffer.writeUInt8(1, offset + 2);
    buffer.writeUInt8(username.length, offset + 3);
    Buffer.from(username).copy(buffer, offset + 4);
    offset += 4 + username.length;
    
    buffer.write('User-Password', offset);
    buffer.writeUInt8(2, offset + 2);
    buffer.writeUInt8(password.length, offset + 3);
    Buffer.from(password).copy(buffer, offset + 4);
    offset += 4 + password.length;
    
    buffer.write('NAS-IP-Address', offset);
    buffer.writeUInt8(4, offset + 2);
    buffer.writeUInt8(4, offset + 3);
    buffer.writeUInt32BE(ipToInt(NAS_CONFIG.nasIp), offset + 4);
    offset += 8;
    
    buffer.write('NAS-Identifier', offset);
    buffer.writeUInt8(32, offset + 2);
    buffer.writeUInt8(NAS_CONFIG.nasId.length, offset + 3);
    Buffer.from(NAS_CONFIG.nasId).copy(buffer, offset + 4);
    
    return { buffer, id, authenticator };
}

function createRADIUSAccountingRequest(username, sessionId, statusType, attributes = {}) {
    const code = 4; // Accounting-Request
    const id = Math.floor(Math.random() * 256);
    const length = 20 + Buffer.byteLength(username) + Buffer.byteLength(sessionId);
    
    const authenticator = crypto.randomBytes(16);
    
    const buffer = Buffer.alloc(length);
    buffer.writeUInt8(code, 0);
    buffer.writeUInt8(id, 1);
    buffer.writeUInt16BE(length, 2);
    authenticator.copy(buffer, 4);
    
    let offset = 20;
    
    buffer.write('Acct-Status-Type', offset);
    buffer.writeUInt8(40, offset + 2);
    buffer.writeUInt8(4, offset + 3);
    buffer.writeUInt32BE(statusType, offset + 4);
    offset += 8;
    
    buffer.write('Acct-Session-Id', offset);
    buffer.writeUInt8(44, offset + 2);
    buffer.writeUInt8(sessionId.length, offset + 3);
    Buffer.from(sessionId).copy(buffer, offset + 4);
    offset += 4 + sessionId.length;
    
    buffer.write('User-Name', offset);
    buffer.writeUInt8(1, offset + 2);
    buffer.writeUInt8(username.length, offset + 3);
    Buffer.from(username).copy(buffer, offset + 4);
    offset += 4 + username.length;
    
    buffer.write('NAS-IP-Address', offset);
    buffer.writeUInt8(4, offset + 2);
    buffer.writeUInt8(4, offset + 3);
    buffer.writeUInt32BE(ipToInt(NAS_CONFIG.nasIp), offset + 4);
    
    return { buffer, id };
}

function ipToInt(ip) {
    return ip.split('.').reduce((acc, octet) => (acc << 8) + parseInt(octet), 0) >>> 0;
}

function intToIp(int) {
    return [
        (int >>> 24) & 255,
        (int >>> 16) & 255,
        (int >>> 8) & 255,
        int & 255
    ].join('.');
}

async function sendRADIUSAuth(username, password) {
    return new Promise((resolve, reject) => {
        const client = dgram.createSocket('udp4');
        const { buffer, id } = createRADIUSAccessRequest(username, password);
        
        const timeout = setTimeout(() => {
            client.close();
            reject(new Error('RADIUS timeout'));
        }, 5000);
        
        client.on('message', (msg) => {
            clearTimeout(timeout);
            const responseCode = msg.readUInt8(0);
            
            if (responseCode === 2) {
                console.log(`[NAS] Access-Accept for ${username}`);
                client.close();
                resolve({ success: true, message: 'Authentication successful' });
            } else if (responseCode === 3) {
                console.log(`[NAS] Access-Reject for ${username}`);
                client.close();
                resolve({ success: false, message: 'Authentication failed' });
            } else {
                client.close();
                resolve({ success: false, message: 'Unknown response' });
            }
        });
        
        client.on('error', (err) => {
            clearTimeout(timeout);
            client.close();
            reject(err);
        });
        
        client.send(buffer, NAS_CONFIG.radiusPort, NAS_CONFIG.radiusServer, (err) => {
            if (err) {
                clearTimeout(timeout);
                client.close();
                reject(err);
            }
        });
    });
}

async function sendRADIUSAccounting(username, sessionId, statusType) {
    return new Promise((resolve, reject) => {
        const client = dgram.createSocket('udp4');
        const { buffer } = createRADIUSAccountingRequest(username, sessionId, statusType);
        
        client.send(buffer, NAS_CONFIG.accountingPort, NAS_CONFIG.radiusServer, (err) => {
            client.close();
            if (err) reject(err);
            else resolve({ success: true });
        });
    });
}

// API Routes
app.get('/api/nas/status', (req, res) => {
    res.json({
        nasId: NAS_CONFIG.nasId,
        nasIp: NAS_CONFIG.nasIp,
        radiusServer: NAS_CONFIG.radiusServer,
        online: true,
        activeSessions: sessions.size,
        clients: Array.from(clients.values())
    });
});

app.get('/api/nas/sessions', (req, res) => {
    const sessionsList = Array.from(sessions.values());
    res.json({ sessions: sessionsList });
});

app.post('/api/nas/connect', async (req, res) => {
    const { username, password } = req.body;
    
    if (!username || !password) {
        return res.status(400).json({ error: 'Username and password required' });
    }
    
    try {
        console.log(`[NAS] Connection request from ${username}`);
        
        const authResult = await sendRADIUSAuth(username, password);
        
        if (authResult.success) {
            const sessionId = crypto.randomBytes(8).toString('hex');
            const assignedIp = `10.0.${Math.floor(Math.random() * 254) + 1}.${Math.floor(Math.random() * 254) + 1}`;
            
            const session = {
                sessionId,
                username,
                ip: assignedIp,
                nasIp: NAS_CONFIG.nasIp,
                startTime: new Date().toISOString(),
                status: 'active'
            };
            
            sessions.set(sessionId, session);
            clients.set(username, session);
            
            await sendRADIUSAccounting(username, sessionId, 1);
            
            console.log(`[NAS] ${username} connected with IP ${assignedIp}`);
            
            res.json({
                success: true,
                sessionId,
                ip: assignedIp,
                message: 'Connected successfully'
            });
        } else {
            res.status(401).json({
                success: false,
                message: 'Authentication failed'
            });
        }
    } catch (error) {
        console.error('[NAS] Error:', error);
        res.status(500).json({
            success: false,
            message: 'RADIUS server unavailable'
        });
    }
});

app.post('/api/nas/disconnect', async (req, res) => {
    const { username, sessionId } = req.body;
    
    let session = null;
    
    if (sessionId && sessions.has(sessionId)) {
        session = sessions.get(sessionId);
    } else if (username && clients.has(username)) {
        session = clients.get(username);
    }
    
    if (!session) {
        return res.status(404).json({ error: 'Session not found' });
    }
    
    try {
        await sendRADIUSAccounting(session.username, session.sessionId, 2);
        
        sessions.delete(session.sessionId);
        clients.delete(session.username);
        
        console.log(`[NAS] ${session.username} disconnected`);
        
        res.json({
            success: true,
            message: 'Disconnected successfully'
        });
    } catch (error) {
        sessions.delete(session.sessionId);
        clients.delete(session.username);
        
        res.json({
            success: true,
            message: 'Disconnected (accounting failed)'
        });
    }
});

app.post('/api/nas/test-auth', async (req, res) => {
    const { username, password } = req.body;
    
    if (!username || !password) {
        return res.status(400).json({ error: 'Username and password required' });
    }
    
    try {
        const result = await sendRADIUSAuth(username, password);
        res.json(result);
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
});

app.post('/api/nas/kick', async (req, res) => {
    const { username } = req.body;
    
    if (!username || !clients.has(username)) {
        return res.status(404).json({ error: 'User not connected' });
    }
    
    const session = clients.get(username);
    
    await sendRADIUSAccounting(username, session.sessionId, 3);
    
    sessions.delete(session.sessionId);
    clients.delete(username);
    
    console.log(`[NAS] ${username} kicked`);
    
    res.json({ success: true, message: 'User kicked' });
});

const PORT = process.env.PORT || 4003;

app.listen(PORT, () => {
    console.log(`[NAS] Simulator running on port ${PORT}`);
    console.log(`[NAS] RADIUS Server: ${NAS_CONFIG.radiusServer}:${NAS_CONFIG.radiusPort}`);
    console.log(`[NAS] Accounting: ${NAS_CONFIG.radiusServer}:${NAS_CONFIG.accountingPort}`);
});

require('dotenv').config();
const venom = require('venom-bot');
const express = require('express');
const cors = require('cors');

const app = express();
app.use(cors());
app.use(express.json());

let client = null;
let botStatus = 'disconnected';

async function startBot() {
    try {
        console.log('[WHATSAPP] Initializing bot...');
        
        client = await venom.create({
            session: 'ngpro-bot',
            multidevice: true,
            headless: true,
            puppeteer: {
                args: ['--no-sandbox', '--disable-setuid-sandbox']
            }
        });

        botStatus = 'connected';
        console.log('[WHATSAPP] Bot connected successfully!');

        client.onMessage((message) => {
            console.log('[WHATSAPP] Message received:', message.body);
            
            if (message.isGroupMsg === false) {
                const response = getAutoResponse(message.body);
                if (response) {
                    client.sendText(message.from, response);
                }
            }
        });

    } catch (error) {
        botStatus = 'error';
        console.error('[WHATSAPP] Error initializing bot:', error);
    }
}

function getAutoResponse(message) {
    const lowerMsg = message.toLowerCase();
    
    if (lowerMsg.includes('oi') || lowerMsg.includes('ola') || lowerMsg.includes('hello')) {
        return 'Olá! Bem-vindo ao NG-PRO Enterprise! 🏢\n\nComo podemos ajudá-lo hoje?';
    }
    if (lowerMsg.includes('cobranca') || lowerMsg.includes('fatura') || lowerMsg.includes('boleto')) {
        return 'Para informações sobre cobranças e faturas, por favor acesso o nosso sistema em http://localhost:3001 ou entre em contato com nosso suporte.';
    }
    if (lowerMsg.includes('suporte') || lowerMsg.includes('ajuda')) {
        return 'Nosso horário de atendimento é de segunda a sexta, das 08h às 18h. Entre em contato pelo email: suporte@ngpro.com.br';
    }
    
    return 'Obrigado pela mensagem! Um de nossos atendentes entrará em contato em breve.';
}

app.get('/api/health', (req, res) => {
    res.json({ status: botStatus, timestamp: new Date().toISOString() });
});

app.get('/api/status', (req, res) => {
    res.json({ 
        status: botStatus,
        connected: botStatus === 'connected'
    });
});

app.post('/api/send', async (req, res) => {
    const { phone, message } = req.body;
    
    if (!phone || !message) {
        return res.status(400).json({ error: 'Phone and message are required' });
    }

    if (botStatus !== 'connected') {
        return res.status(503).json({ error: 'WhatsApp bot is not connected' });
    }

    try {
        const formattedPhone = phone.includes('@c.us') ? phone : `${phone}@c.us`;
        await client.sendText(formattedPhone, message);
        
        console.log(`[WHATSAPP] Message sent to ${phone}`);
        res.json({ success: true, phone, message });
    } catch (error) {
        console.error('[WHATSAPP] Error sending message:', error);
        res.status(500).json({ error: error.message });
    }
});

app.post('/api/send-bulk', async (req, res) => {
    const { recipients } = req.body;
    
    if (!recipients || !Array.isArray(recipients)) {
        return res.status(400).json({ error: 'Recipients array is required' });
    }

    if (botStatus !== 'connected') {
        return res.status(503).json({ error: 'WhatsApp bot is not connected' });
    }

    const results = [];
    
    for (const recipient of recipients) {
        try {
            const { phone, message } = recipient;
            if (phone && message) {
                const formattedPhone = phone.includes('@c.us') ? phone : `${phone}@c.us`;
                await client.sendText(formattedPhone, message);
                results.push({ phone, success: true });
                console.log(`[WHATSAPP] Message sent to ${phone}`);
            }
        } catch (error) {
            results.push({ phone: recipient.phone, success: false, error: error.message });
        }
    }

    res.json({ results, sent: results.filter(r => r.success).length });
});

const PORT = process.env.PORT || 4002;

app.listen(PORT, () => {
    console.log(`[WHATSAPP] API server running on port ${PORT}`);
    startBot();
});

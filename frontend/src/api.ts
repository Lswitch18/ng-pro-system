const API_URL = '/api';

function getToken() {
    return localStorage.getItem('ng_token');
}

async function request(path: string, options: RequestInit = {}) {
    const token = getToken();
    const url = `${API_URL}${path}`;
    console.log(`[API REQUEST] Fetching: ${url}`, options);
    try {
        const res = await fetch(url, {
            ...options,
            headers: {
                'Content-Type': 'application/json',
                ...(token ? { Authorization: `Bearer ${token}` } : {}),
                ...options.headers,
            },
        });
        console.log(`[API RESPONSE] Status: ${res.status} OK: ${res.ok}`);
        if (!res.ok) {
            const errorText = await res.text().catch(() => 'no body');
            console.error(`[API ERROR] Path: ${path} | Status: ${res.status} | Body: ${errorText}`);
            throw new Error(`API error ${res.status}: ${errorText}`);
        }
        return res.json();
    } catch (err) {
        console.error(`[API FETCH FAILED] Url: ${url}`, err);
        throw err;
    }
}

export const api = {
    login: (username: string, password: string) =>
        request('/auth/login', { method: 'POST', body: JSON.stringify({ username, password }) }),

    me: () => request('/auth/me'),

    getStats: () => request('/dashboard/stats'),

    getCustomers: () => request('/customers'),
    createCustomer: (data: object) =>
        request('/customers', { method: 'POST', body: JSON.stringify(data) }),
    updateCustomer: (id: number, data: object) =>
        request(`/customers/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    updateCustomerStatus: (id: number, status: string) =>
        request(`/customers/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status }) }),
    deleteCustomer: (id: number) =>
        request(`/customers/${id}`, { method: 'DELETE' }),

    getPlans: () => request('/plans'),

    getInvoices: () => request('/invoices'),
    getInvoicesByCustomer: (id: number) => request(`/invoices/customer/${id}`),
    payInvoice: (id: number) =>
        request(`/invoices/${id}/pay`, { method: 'POST' }),
    runBilling: () =>
        request('/invoices/run-billing', { method: 'POST' }),
    runDunning: () =>
        request('/invoices/run-dunning', { method: 'POST' }),

    getOverdueCustomers: (status?: string) => 
        request('/invoices/overdue-customers' + (status ? `?status=${status}` : '')),
    
    sendOverdueNotifications: (customerIds: number[], action: string = 'email') =>
        request('/invoices/send-overdue-notifications', { 
            method: 'POST', 
            body: JSON.stringify({ customerIds, action }) 
        }),
    
    suspendOverdueCustomers: (customerIds: number[]) =>
        request('/invoices/suspend-overdue', { 
            method: 'POST', 
            body: JSON.stringify({ customerIds }) 
        }),

    getWhatsAppStatus: () => request('/whatsapp/status'),
    
    sendWhatsAppMessage: (phone: string, message: string) =>
        request('/whatsapp/send', { 
            method: 'POST', 
            body: JSON.stringify({ phone, message }) 
        }),
    
    sendBulkWhatsApp: (recipients: {phone: string, amount?: string}[], template: string = 'default') =>
        request('/whatsapp/send-bulk', { 
            method: 'POST', 
            body: JSON.stringify({ recipients, template }) 
        }),
};

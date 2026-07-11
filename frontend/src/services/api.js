const BASE_URL = 'http://localhost:9000/AUTHENTICATION';

const getHeaders = () => {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {})
    };
};

export const login = async (credentials) => {
    const response = await fetch(`${BASE_URL}/api/user/login`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(credentials)
    });
    if (!response.ok) throw new Error('Login failed');
    return response.json();
};

export const getSelfInfo = async () => {
    const response = await fetch(`${BASE_URL}/get/self/info`, {
        method: 'GET',
        headers: getHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch self info');
    return response.json();
};

export const changeDetails = async (id, details) => {
    const response = await fetch(`${BASE_URL}/change/details?id=${id}`, {
        method: 'PUT',
        headers: getHeaders(),
        body: JSON.stringify(details)
    });
    if (!response.ok) throw new Error('Failed to change details');
    return response.text();
};

// Admin Endpoints
export const createProjectManager = async (managerData) => {
    const response = await fetch(`${BASE_URL}/create/managers`, {
        method: 'POST',
        headers: getHeaders(),
        body: JSON.stringify(managerData)
    });
    if (!response.ok) throw new Error('Failed to create project manager');
    return response.text();
};

export const getProjectManagersInfo = async () => {
    const response = await fetch(`${BASE_URL}/get/manager/info`, {
        method: 'GET',
        headers: getHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch project managers info');
    return response.json();
};


export const changeProjectManagerValidity = async (id, validity) => {
    const response = await fetch(`${BASE_URL}/change/validity/manager?id=${id}&validity=${validity}`, {
        method: 'PUT',
        headers: getHeaders(),
    });
    if (!response.ok) throw new Error('Failed to change validity');
    return response.text();
};

const INGESTOR_URL = 'http://localhost:9000/INGESTOR';

export const getAdminManagerStats = async () => {
    const token = localStorage.getItem('token');
    const response = await fetch(`${INGESTOR_URL}/api/reports/admin/manager-stats`, {
        method: 'GET',
        headers: {
            ...(token ? { Authorization: `Bearer ${token}` } : {})
        }
    });
    if (!response.ok) throw new Error('Failed to fetch manager stats');
    return response.json();
};

export const createProject = async (formData) => {
    const token = localStorage.getItem('token');
    const response = await fetch(`${INGESTOR_URL}/enter/project/details`, {
        method: 'POST',
        headers: {
            ...(token ? { Authorization: `Bearer ${token}` } : {})
        },
        body: formData
    });
    if (!response.ok) throw new Error('Failed to create project');
    return response.text();
};
export const getProjects = async () => {
    const token = localStorage.getItem('token');
    const response = await fetch(`${INGESTOR_URL}/get/project/info`, {
        method: 'GET',
        headers: {
            ...(token ? { Authorization: `Bearer ${token}` } : {})
        }
    });
    if (!response.ok) throw new Error('Failed to fetch projects');
    return response.json();
};

export const changeProjectStatus = async (id, status) => {
    const token = localStorage.getItem('token');
    const response = await fetch(`${INGESTOR_URL}/change/status?id=${id}&status=${status}`, {
        method: 'PUT',
        headers: {
            ...(token ? { Authorization: `Bearer ${token}` } : {})
        }
    });
    if (!response.ok) throw new Error('Failed to change status');
    return response.text();
};

export const updateIgnoredDomains = async (hash, ignoredDomains) => {
    const token = localStorage.getItem('token');
    const response = await fetch(`${INGESTOR_URL}/change/ignored-domains?hash=${hash}&ignoredDomains=${encodeURIComponent(ignoredDomains)}`, {
        method: 'PUT',
        headers: {
            ...(token ? { Authorization: `Bearer ${token}` } : {})
        }
    });
    if (!response.ok) throw new Error('Failed to update ignored domains');
    return response.text();
};

export const changeProjectBaseline = async (id, file) => {
    const token = localStorage.getItem('token');
    const formData = new FormData();
    formData.append('baselineFile', file);
    
    const response = await fetch(`${INGESTOR_URL}/change/baseline?id=${id}`, {
        method: 'PUT',
        headers: {
            ...(token ? { Authorization: `Bearer ${token}` } : {})
        },
        body: formData
    });
    if (!response.ok) throw new Error('Failed to change baseline');
    return response.text();
};

export const getIncidents = async () => {
    const response = await fetch(`${INGESTOR_URL}/api/incidents`, {
        method: 'GET',
        headers: getHeaders()
    });
    if (!response.ok) throw new Error('Failed to fetch incidents');
    return response.json();
};

export const injectTestEvent = async (events) => {
    const response = await fetch(`${INGESTOR_URL}/api/events/ingest`, {
        method: 'POST',
        headers: getHeaders(),
        body: JSON.stringify(events)
    });
    if (!response.ok) throw new Error('Injection failed');
    return response.json();
};

// ── Report / Project Incident API ────────────────────────────────────────────

export const getProjectIncidents = async (projectHash) => {
    const response = await fetch(`${INGESTOR_URL}/api/reports/project/${projectHash}`, {
        method: 'GET',
        headers: getHeaders()
    });
    if (!response.ok) throw new Error('Failed to fetch project incidents');
    return response.json();
};

export const updateIncidentStatus = async (incidentId, status) => {
    const response = await fetch(`${INGESTOR_URL}/api/reports/incident/${incidentId}/status?status=${status}`, {
        method: 'PUT',
        headers: getHeaders()
    });
    if (!response.ok) throw new Error('Failed to update incident status');
    return response.text();
};

export const generateReport = async (projectHash) => {
    const response = await fetch(`${INGESTOR_URL}/api/reports/project/${projectHash}/generate`, {
        method: 'GET',
        headers: getHeaders()
    });
    if (!response.ok) throw new Error('Failed to generate report');
    return response.json();
};

export const clearProjectIncidents = async (projectHash) => {
    const response = await fetch(`${INGESTOR_URL}/api/incidents/project/${projectHash}/clear`, {
        method: 'DELETE',
        headers: getHeaders()
    });
    if (!response.ok) throw new Error('Failed to clear incidents');
    return response.text();
};

export const ingestFile = async (file, projectHash) => {
    const token = localStorage.getItem('token');
    const formData = new FormData();
    formData.append('file', file);
    formData.append('projectHash', projectHash);
    const response = await fetch(`${INGESTOR_URL}/api/events/ingest/file`, {
        method: 'POST',
        headers: { ...(token ? { Authorization: `Bearer ${token}` } : {}) },
        body: formData
    });
    if (!response.ok) throw new Error('File injection failed');
    return response.json();
};

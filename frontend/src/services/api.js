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
        headers: getHeaders(),
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
export const createAnalyst = async (analystData) => {
    const response = await fetch(`${BASE_URL}/create/analysts`, {
        method: 'POST',
        headers: getHeaders(),
        body: JSON.stringify(analystData)
    });
    if (!response.ok) throw new Error('Failed to create analyst');
    return response.text();
};

export const getAnalystsInfo = async () => {
    const response = await fetch(`${BASE_URL}/get/ananlyst/info`, {
        method: 'GET',
        headers: getHeaders(),
    });
    if (!response.ok) throw new Error('Failed to fetch analysts info');
    return response.json();
};

export const changeAnalystValidity = async (id, validity) => {
    const response = await fetch(`${BASE_URL}/change/validity/analyst?id=${id}&validity=${validity}`, {
        method: 'PUT',
        headers: getHeaders(),
    });
    if (!response.ok) throw new Error('Failed to change validity');
    return response.text();
};

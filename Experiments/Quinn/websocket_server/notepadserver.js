// server.js
const WebSocket = require('ws');

// Create WebSocket server on port 8080
const wss = new WebSocket.Server({ port: 9090 });

console.log("✅ WebSocket server running on ws://localhost:9090");

wss.on('connection', (ws, req) => {
    const ip = req.socket.remoteAddress;
    console.log(`💬 New client connected: ${ip}`);

    ws.on('message', (message) => {
        console.log(`📩 Received message: ${message}`);
        ws.send(`Echo: ${message}`);
    });

    ws.on('close', () => {
        console.log(`❌ Client disconnected: ${ip}`);
    });
});

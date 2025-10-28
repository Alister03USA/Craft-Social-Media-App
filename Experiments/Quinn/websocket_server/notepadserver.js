const WebSocket = require('ws');

// Listen on port 9090
const wss = new WebSocket.Server({ port: 9090, path: '/ws/notifications/Quinn' });

wss.on('connection', function connection(ws, req) {
  console.log('Client connected!');

  // Send a test notification every 5 seconds
  const interval = setInterval(() => {
    const message = JSON.stringify({
      title: "Hello from Node.js",
      message: "This is a test notification"
    });

    ws.send(message);

    // Log to terminal
    console.log(`Notification sent to client: ${message} at ${new Date().toLocaleTimeString()}`);
  }, 5000);

  ws.on('close', () => {
    console.log('Client disconnected');
    clearInterval(interval);
  });

  ws.on('message', (msg) => {
    console.log('Received from client:', msg);
  });
});

console.log('WebSocket server running on ws://localhost:9090/ws/notifications/Quinn');

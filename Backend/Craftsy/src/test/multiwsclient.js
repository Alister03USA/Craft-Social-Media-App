const users = {};
const containers = document.getElementById("usersContainer");

function connectAll() {
    const wsserver = document.getElementById("wsserver").value;
    const usernames = document.getElementById("usernames").value.split(",").map(u => u.trim());

    usernames.forEach(username => {
        if (!username) return;

        // Create a block for each user
        const block = document.createElement("div");
        block.className = "user-block";
        block.innerHTML = `<h3>${username}</h3><textarea id="log-${username}" readonly></textarea>`;
        containers.appendChild(block);

        // Connect WebSocket
        const ws = new WebSocket(wsserver + username);
        users[username] = ws;

        ws.onopen = () => log(username, "Connected to " + wsserver + username);
        ws.onmessage = (event) => log(username, "Notification: " + event.data);
        ws.onerror = (err) => log(username, "WebSocket error: " + err);
        ws.onclose = () => log(username, "Disconnected from server");
    });
}

function log(username, message) {
    const logBox = document.getElementById("log-" + username);
    if (logBox) {
        logBox.value += message + "\n";
        logBox.scrollTop = logBox.scrollHeight;
    }
}

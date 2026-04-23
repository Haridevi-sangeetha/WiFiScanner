from flask import Flask, request, jsonify
from detector import analyze_network

app = Flask(__name__)

@app.route("/")
def home():
    return "WiFi Threat Detector Running"

@app.route("/analyze", methods=["POST"])
def analyze():

    data = request.get_json(force=True)

    ssid = data["ssid"]
    signal = int(data["signal"])
    security = data["security"]

    network = {
        "ssid": ssid,
        "signal": signal,
        "security": security
    }

    result = analyze_network(network)

    # FULL CLEAN JSON RESPONSE
    return jsonify({
        "ssid": ssid,
        "signal": signal,
        "risk": result
    })

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)

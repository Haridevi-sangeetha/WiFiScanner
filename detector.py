from model import train_model

model = train_model()

def analyze_network(network):

    ssid = network["ssid"]
    signal = int(network["signal"])
    security = network["security"]

    # RULE 1
    if "Open" in security:
        return "DANGEROUS"

    # RULE 2
    if "Free" in ssid or "Public" in ssid:
        return "SUSPICIOUS"

    # AI INPUT
    is_secure = 1 if "WPA" in security else 0

    prediction = model.predict([[is_secure, signal]])

    result = prediction[0]

    # FINAL SIGNAL SAFETY CHECK
    if signal >= -50:
        return "SAFE"
    elif signal >= -70:
        return "MEDIUM"
    else:
        return "DANGEROUS"
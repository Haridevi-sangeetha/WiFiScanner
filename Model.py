from sklearn.tree import DecisionTreeClassifier

def train_model():

    # REAL WiFi dBm range (-30 to -90)
    X = [
        [1, -30],
        [1, -45],
        [1, -55],
        [0, -60],
        [0, -70],
        [1, -40],
        [0, -80]
    ]

    y = [
        "SAFE",
        "SAFE",
        "SAFE",
        "MEDIUM",
        "DANGEROUS",
        "SAFE",
        "DANGEROUS"
    ]

    model = DecisionTreeClassifier()
    model.fit(X, y)

    return model
from flask import Flask, request, jsonify
import joblib
import numpy as np

app = Flask(__name__)
model = joblib.load('model/bike_model.pkl')

@app.route('/predict', methods=['POST'])
def predict():
    data = request.json
    features = [[
        data['season'], data['hr'], data['holiday'],
        data['weekday'], data['workingday'], data['weathersit'],
        data['temp'], data['atemp'], data['hum'], data['windspeed']
    ]]
    prediction = model.predict(features)[0]
    return jsonify({'predicted_rentals': round(float(prediction), 2)})

if __name__ == '__main__':
    app.run(port=5001)
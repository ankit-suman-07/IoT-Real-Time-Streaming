from flask import Flask, render_template
from flask_socketio import SocketIO
from kafka import KafkaConsumer
import json
import threading

app = Flask(__name__)
socketio = SocketIO(app, cors_allowed_origins="*")


def kafka_listener():
    consumer = KafkaConsumer(
        'predictions',
        bootstrap_servers='localhost:9092',
        auto_offset_reset='latest',
        value_deserializer=lambda m: json.loads(m.decode('utf-8'))
    )

    for message in consumer:
        data = message.value

        try:
            print("RAW MESSAGE:", data)

            # Safely extract input
            input_data = data.get('input', {})
            prediction_data = data.get('prediction', {})

            hour = input_data.get('hr', None)
            predicted = prediction_data.get('predicted_rentals')

            # Handle alternate formats (in case backend changes)
            if predicted is None:
                predicted = prediction_data.get('prediction')

            # Skip bad messages instead of crashing
            if hour is None or predicted is None:
                print("Skipping malformed message")
                continue

            socketio.emit('prediction', {
                'hour': hour,
                'predicted': predicted
            })

        except Exception as e:
            print("Error processing Kafka message:", e)
            print("Raw data:", data)


@app.route('/')
def index():
    return render_template('index.html')


if __name__ == '__main__':
    thread = threading.Thread(target=kafka_listener)
    thread.daemon = True
    thread.start()

    socketio.run(app, port=5002, debug=False)
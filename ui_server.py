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
        hour = data['input']['hr']
        predicted = data['prediction']['predicted_rentals']
        socketio.emit('prediction', {
            'hour': hour,
            'predicted': predicted
        })

@app.route('/')
def index():
    return render_template('index.html')

if __name__ == '__main__':
    thread = threading.Thread(target=kafka_listener)
    thread.daemon = True
    thread.start()
    socketio.run(app, port=5002, debug=False)
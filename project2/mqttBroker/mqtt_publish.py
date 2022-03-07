from email.policy import strict
import sys
from tokenize import String

import paho.mqtt.client as mqtt


# The callback for when the client receives a CONNACK response from the server.
def on_connect(client, userdata, flags, rc):
    print("Connected with result code " + str(rc))
    # Subscribing in on_connect() means that if we lose the connection and
    # reconnect then subscriptions will be renewed.
    client.subscribe("$SYS/#")


# The callback for when a PUBLISH message is received from the server.
def on_message(client, userdata, msg):
    print(msg.topic + " " + str(msg.payload))
    print("CONNECTED")



group = sys.argv[1]
code = sys.argv[2]
temp = int(sys.argv[3])

client = mqtt.Client(client_id="test")

if client.connect("localhost", 1883, 60) != 0:
    print("Could not connect to MQTT Broker!")
    sys.exit(-1)

message = client.publish(f'temperature/{group}/{code}', payload=temp, qos=1)
print(message)

client.disconnect()

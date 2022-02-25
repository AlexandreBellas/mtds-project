import sys

import paho.mqtt.client as mqtt


# The callback for when a PUBLISH message is received from the server.
def on_message(client, userdata, msg):
    print(msg.topic + " " + str(msg.payload))


client = mqtt.Client()
client.on_message = on_message

if client.connect("localhost", 1883, 60) != 0:
    print("Could not connect to MQTT Broker!")
    sys.exit(-1)

client.subscribe("test/status")

try:
    print("PRESS CTRL+C to exit...")
    client.loop_forever()
except:
    print("Disconnecting from Broker")
client.disconnect()

import sys
import time

import numpy as np
import paho.mqtt.client as mqtt
import json
import pandas


# The callback for when the client receives a CONNACK response from the server.
def on_connect(client, userdata, flags, rc):
    print("Connected with result code " + str(rc))
    # Subscribing in on_connect() means that if we lose the connection and
    # reconnect then subscriptions will be renewed.
    client.subscribe("$SYS/#")


def mqtt_publish(data):
    client = mqtt.Client()
    if client.connect("localhost", 1883, 60) != 0:
        print("Could not connect to MQTT Broker!")
        sys.exit(-1)

    # test = pandas.DataFrame(["asd,asd "])
    qwe = data.head(10000)
    print(len(data))
    list_df = np.array_split(data, 100)
    for df_slice in list_df:
        time.sleep(0.2)
        result = df_slice.to_json(orient="records")
        try:
            response = client.publish("noise/noise-simulation", payload=result, qos=1)
            print(response)
            print("SEND")
        except:
            print("NOT WORKING")

    client.disconnect()

import pickle
import sys
import numpy as np
import paho.mqtt.client as mqtt
import json
import pandas
import requests


# The callback for when the client receives a CONNACK response from the server.
def on_connect(client, userdata, flags, rc):
    print("Connected with result code " + str(rc))
    # Subscribing in on_connect() means that if we lose the connection and
    # reconnect then subscriptions will be renewed.
    client.subscribe("$SYS/#")


def mqtt_publish(data):
    test = pandas.DataFrame(["asd,asd "])
    result = data.to_json()
    print(type(result))
    try:
        newHeaders = {'Content-type': 'application/json', 'Accept': 'text/plain'}
        response = requests.post("http://localhost:1880/noise", data=result, headers=newHeaders)
        print(response)
        print("SEND")
    except:
        print("EEORR")

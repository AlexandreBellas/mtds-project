const { Kafka } = require('kafkajs')
require('dotenv').config()

const kafka = new Kafka({
  clientId: process.env.CLIENT_ID,
  brokers: [process.env.BROKER_NAME],
  retry: {
    maxRetryTime: 1000,
    retries: 10
  }
})

module.exports = kafka
